package org.de_studio.diary.android.adapter

/**
 * Created by HaiNguyen on 10/13/17.
 */
interface ItemType{
    val intValue: Int
}

class SingleItemType : ItemType {
    override val intValue: Int = 0
}