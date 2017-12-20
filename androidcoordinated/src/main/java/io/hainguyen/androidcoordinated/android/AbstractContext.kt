package org.de_studio.diary.android

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment

/**
 * Created by HaiNguyen on 10/6/17.
 */
sealed class AbstractContext{
    abstract val activity: Activity
    abstract fun startActivityForResult(intent: Intent, requestCode: Int)

    class ActivityContext(val from: Activity) : AbstractContext() {
        override val activity: Activity = from
        override fun startActivityForResult(intent: Intent, requestCode: Int) {
            from.startActivityForResult(intent, requestCode)
        }
    }

    class FragmentContext(val fragment: Fragment) : AbstractContext() {
        override val activity: Activity get() = fragment.activity
        override fun startActivityForResult(intent: Intent, requestCode: Int) {
            fragment.startActivityForResult(intent, requestCode)
        }
    }
    companion object {
        fun from(activity: Activity): AbstractContext = ActivityContext(activity)
        fun from(fragment: Fragment): AbstractContext = FragmentContext(fragment)
    }
}