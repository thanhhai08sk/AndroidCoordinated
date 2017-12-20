package org.de_studio.diary.android.adapter

import android.view.View


/**
 * Created by HaiNguyen on 9/29/17.
 */
open class AdapterItem<out R: Any>(val itemType: ItemType, val itemData: R?, val selected: Boolean = false, val topView: TopView? = null)
class SimpleAdapterItem<out R: Any>(itemData: R, selected: Boolean = false): AdapterItem<R>(SingleItemType(), itemData, selected, null)

interface HeaderInterface {
    val view: View
}

