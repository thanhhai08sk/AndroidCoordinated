package org.de_studio.diary.android.adapter

import android.view.View

/**
 * Created by HaiNguyen on 10/13/17.
 */
sealed class TopView(val layoutRes: Int, val viewId: Int){
    abstract fun bindView(view: View)
}