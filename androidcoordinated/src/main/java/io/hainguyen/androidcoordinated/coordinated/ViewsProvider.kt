package io.hainguyen.androidcoordinated.coordinated

import android.support.v4.app.FragmentActivity
import android.view.View

/**
 * Created by HaiNguyen on 10/4/17.
 */
interface ViewsProvider {
    val view: View
    val activity: FragmentActivity
}

open class BaseViewsProvider(
        override val view: View,
        override val activity: FragmentActivity
): ViewsProvider