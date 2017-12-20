package org.de_studio.diary.android.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.de_studio.diary.android.crashReporter.CrashReporter
import org.de_studio.diary.data.UnsupportedException
import org.de_studio.diary.screen.action.ItemsUpdated

import timber.log.Timber

/**
 * Created by HaiNguyen on 9/29/17.
 */
abstract class BaseAdapter< R: Any, in I : ItemsProvider<AdapterItem<R>, R, T>, out H : ViewHolderProvider<R>, T : ItemTypeProvider<R>>(
        private var itemsProvider: I,
        private val viewHolderProvider: H,
        private val itemTypeProvider: T
) : RecyclerView.Adapter<BaseViewHolder<R>>() {
    private val disposables = CompositeDisposable()
    private val itemActionRL = PublishRelay.create<ItemAction<R>>()

    init {
        observeItemsChangedEvent()
    }

    fun updateItemProvider(itemsProvider: I) {
        this.itemsProvider = itemsProvider
        observeItemsChangedEvent()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<R> {
        return viewHolderProvider
                .fromItemType(itemTypeProvider.fromInt(viewType), parent)
                .apply {
                    registerForActionOnViewHolder(this)
                    disposables.addAll(
                            this.onItemActionObservable()
                                    .subscribe { itemAction ->
                                        if (itemAction.itemIndex >= 0) {
                                            itemsProvider.getItem(itemAction.itemIndex).itemData?.apply {
                                                itemAction.item = this
                                                itemActionRL.accept(itemAction)
                                            }
                                        }else CrashReporter.log("itemIndex = -1 in ${this.javaClass.simpleName}, size = ${itemsProvider.size}")
                                    }
                    )
                }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<R>, position: Int) {
        holder.bindView(itemsProvider.getItem(position))
    }

    override fun getItemViewType(position: Int): Int =
            itemsProvider.getItem(position).itemType.intValue

    override fun getItemCount(): Int = itemsProvider.size

    protected fun getItemAt(index: Int): R = itemsProvider.getItem(index).itemData as R

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        disposables.clear()
    }

    fun onItemAction(): Observable<ItemAction<R>> = itemActionRL

    protected fun addToAutoDispose(disposable: Disposable) {
        disposables.add(disposable)
    }

    protected open fun registerForActionOnViewHolder(viewHolder: BaseViewHolder<R>) {

    }

    private fun observeItemsChangedEvent() {
        disposables.clear()
        disposables.add(
                itemsProvider.getItemsUpdateObservable()
                        .subscribe {
                            when (it) {
                                is ItemsUpdated.Insert -> notifyItemRangeInserted(it.startIndex, it.length)
                                is ItemsUpdated.Removed -> notifyItemRangeRemoved(it.startIndex, it.length)
                                is ItemsUpdated.Changed -> notifyItemRangeChanged(it.startIndex, it.length)
                                is ItemsUpdated.Refresh -> {
                                    Timber.e("${this.javaClass.simpleName} observeItemsChangedEvent refresh")
                                    notifyDataSetChanged()
                                }
                                else -> UnsupportedException(" ${this.javaClass.simpleName} observeItemsChangedEvent")
                            }
                        }
        )
    }
}


open class SimpleAdapter<R: Any, out VH: BaseViewHolder<R>>(
        itemsProvider: SimpleAdapterItemsProvider<R>,
        singleViewHolderProvider: SingleViewHolderProvider<R, VH>
)
    : BaseAdapter<R, SimpleAdapterItemsProvider<R>, SingleViewHolderProvider<R, VH>, SingleItemTypeProvider<Any?>>(
        itemsProvider, singleViewHolderProvider, SingleItemTypeProvider()
)