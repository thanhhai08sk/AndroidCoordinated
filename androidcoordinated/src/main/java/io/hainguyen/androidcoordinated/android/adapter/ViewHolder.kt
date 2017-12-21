package org.de_studio.diary.android.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.jakewharton.rxrelay2.PublishRelay
import io.hainguyen.androidcoordinated.utils.inflateView
import io.hainguyen.androidcoordinated.utils.onClick
import io.hainguyen.androidcoordinated.utils.onLongClick
import io.reactivex.Observable



/**
 * Created by HaiNguyen on 10/13/17.
 */


abstract class BaseViewHolder<R: Any>(itemView: View, observeItemClick: Boolean, observeItemLongClick: Boolean): RecyclerView.ViewHolder(itemView) {
    protected val context = itemView.context!!
    private val itemActionRL = PublishRelay.create<ItemAction<R>>()

    init {
        if (observeItemClick) itemView.onClick { fireAction(ItemAction.Click(this.adapterPosition)) }
        if (observeItemLongClick) itemView.onLongClick { fireAction(ItemAction.LongClick(this.adapterPosition)) }
    }

    fun bindView(item: AdapterItem<R>) {
        bindItemData(item)
        handleTopView(item)
    }

    private fun handleTopView(item: AdapterItem<*>) {
        itemView as ViewGroup
        (item.topView?.apply {
            if (itemView.childCount == 1 || itemView.getChildAt(0).id != this.viewId) {
                val topView = itemView.context.inflateView(this.layoutRes, itemView)
                itemView.addView(topView, 0)
                this.bindView(topView)
            }
        }
                ?: if (itemView.childCount > 1) itemView.removeViewAt(0))
    }

    protected fun fireAction(action: ItemAction<R>) {
        itemActionRL.accept(action)
    }

    abstract fun bindItemData(item: AdapterItem<R>)
    fun onItemActionObservable(): Observable<ItemAction<R>> = itemActionRL
}

class HeaderViewHolder(container: FrameLayout) : BaseViewHolder<Any>(container, false, false) {
    override fun bindItemData(item: AdapterItem<*>) {
        itemView as FrameLayout
        item as HeaderInterface
        itemView.removeAllViews()
        if (item.view.parent != null) {
            (item.view.parent as ViewGroup).removeView(item.view)
        }
        itemView.addView(item.view)
    }
}
