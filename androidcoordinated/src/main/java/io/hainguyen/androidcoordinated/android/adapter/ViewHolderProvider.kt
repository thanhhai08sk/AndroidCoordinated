package org.de_studio.diary.android.adapter

import android.view.ViewGroup

/**
 * Created by HaiNguyen on 10/13/17.
 */

interface ViewHolderProvider<R:Any > {
    fun fromItemType(itemType: ItemType, parent: ViewGroup) : BaseViewHolder<R>
}

open class SingleViewHolderProvider<R : Any, out VH : BaseViewHolder<R>>(private val viewHolderMaker: (ViewGroup) -> VH) : ViewHolderProvider<R> {
    override fun fromItemType(itemType: ItemType, parent: ViewGroup): BaseViewHolder<R> = viewHolderMaker.invoke(parent)
}