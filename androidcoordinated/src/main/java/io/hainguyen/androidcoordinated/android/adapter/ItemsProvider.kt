package org.de_studio.diary.android.adapter

import android.view.View
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.de_studio.diary.feature.adapter.entries.EntriesAdapterItem
import org.de_studio.diary.screen.action.ItemsUpdated
import timber.log.Timber

/**
 * Created by HaiNguyen on 9/29/17.
 */
interface ItemsProvider<out T: AdapterItem<R>, R: Any, I: ItemTypeProvider<R>> {
    val size: Int
    fun setData(data: List<R>)
    fun getItem(index: Int): T
    fun getItemsUpdateObservable(): Observable<ItemsUpdated>
    fun addHeader(view: View)
    fun removeHeader(view: View)
    fun removeAllHeader()
    fun dataUpdated(itemsUpdated: ItemsUpdated)
    fun selectItem(item: R)
    fun deselectItem(item: R)
    fun deselectAll()
    fun getSelectedCount(): Int
    fun isSelected(item: R): Boolean
    fun getItemType(item: R): ItemType
}


abstract class BaseItemsProvider< out T: AdapterItem<R>,   R: Any, I: ItemTypeProvider<R>>(protected var itemsData: List<R>, private val itemTypeProvider: I): ItemsProvider<T, R, I> {
    private val headers = arrayListOf<HeaderInterface>()
    protected val itemsUpdateEventRL = PublishRelay.create<ItemsUpdated>()!!
    private val selectedItems = HashSet<R>()

    override val size: Int
        get() = itemsData.size + headers.size

    override fun setData(data: List<R>) {
        this.itemsData = data
        //                        ItemsUpdated.Changed(headers.size, Int.MAX_VALUE)

//        itemsUpdateEventRL.accept(
//                if (headers.isEmpty()) ItemsUpdated.Refresh else ItemsUpdated.Changed(headers.size, data.size)
//        )
        itemsUpdateEventRL.accept(ItemsUpdated.Refresh)
    }

    override fun getItemsUpdateObservable(): Observable<ItemsUpdated> = itemsUpdateEventRL

    override fun addHeader(view: View) {
        Timber.e("addHeader")
        headers.add(EntriesAdapterItem.Header(view))
        itemsUpdateEventRL.accept(ItemsUpdated.Insert(headers.size -1))
    }

    override fun removeHeader(view: View) {
        headers.indexOfFirst { it.view == view }
                .takeUnless { it == -1 }
                ?.apply {
                    headers.removeAt(this)
                    itemsUpdateEventRL.accept(ItemsUpdated.Removed(this))
                }
    }

    override fun removeAllHeader() {
        itemsUpdateEventRL.accept(ItemsUpdated.Removed(0, headers.size))
        headers.clear()
    }

    override fun dataUpdated(itemsUpdated: ItemsUpdated) {
        Timber.e("dataUpdated ${itemsUpdated.javaClass.simpleName}")
        itemsUpdateEventRL.accept(
                when (itemsUpdated) {
                    is ItemsUpdated.Changed -> itemsUpdated.copy(startIndex = itemsUpdated.startIndex + headers.size)
                    is ItemsUpdated.Insert -> itemsUpdated.copy(startIndex = itemsUpdated.startIndex + headers.size)
                    is ItemsUpdated.Removed -> itemsUpdated.copy(startIndex = itemsUpdated.startIndex + headers.size)
                    ItemsUpdated.Refresh -> itemsUpdated
                }
        )
    }

    override fun getItem(index: Int): T {
        return if (index < headers.size) {
            headers[index] as T
        } else {
            val item = itemsData[calcEntryRelativeIndex(index)]
            convertItemDataToAdapterItem(item, index)
        }
    }

    override fun selectItem(item: R) {
        selectedItems.add(item)
        itemsUpdateEventRL.accept(ItemsUpdated.Changed(getAbsoluteIndexForItemData(item)))
    }

    override fun deselectItem(item: R) {
        selectedItems.remove(item)
        itemsUpdateEventRL.accept(ItemsUpdated.Changed(getAbsoluteIndexForItemData(item)))
    }

    override fun deselectAll() {
        selectedItems.clear()
        itemsUpdateEventRL.accept(ItemsUpdated.Refresh)
    }

    override fun isSelected(item: R):Boolean {
        if (selectedItems.isEmpty()) return false
        return selectedItems.contains(item)
    }

    override fun getSelectedCount(): Int = selectedItems.size

    override fun getItemType(item: R): ItemType = itemTypeProvider.fromItem(item)

    private fun getAbsoluteIndexForItemData(item: R) = itemsData.indexOf(item) + headers.size

    abstract fun convertItemDataToAdapterItem(item: R, index: Int): T

    private fun calcEntryRelativeIndex(index: Int): Int = index - headers.size
    private fun toAbsoluteIndex(index: Int): Int = index + headers.size
}

class SimpleAdapterItemsProvider<R : Any>(itemsData: List<R> = listOf()) : BaseItemsProvider<SimpleAdapterItem<R>, R, SingleItemTypeProvider<Any?>>(itemsData, SingleItemTypeProvider()) {
    override fun convertItemDataToAdapterItem(item: R, index: Int): SimpleAdapterItem<R> =
            SimpleAdapterItem(item, isSelected(item))
}