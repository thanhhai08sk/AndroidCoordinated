package org.de_studio.diary.android.adapter

/**
 * Created by HaiNguyen on 10/13/17.
 */

interface ItemTypeProvider<in R: Any> {
    fun fromInt(intValue: Int): ItemType
    fun fromItem(item: R): ItemType
}

class SingleItemTypeProvider<T> : ItemTypeProvider<Any> {
    override fun fromInt(intValue: Int): ItemType = SingleItemType()
    override fun fromItem(item: Any): ItemType = SingleItemType()
}