package org.de_studio.diary.android.crashReporter

import com.crashlytics.android.Crashlytics

/**
 * Created by HaiNguyen on 11/7/17.
 */
object CrashReporter {
    fun logException(e: Throwable) {
        Crashlytics.logException(e)
    }

    fun log(message: String) {
        Crashlytics.log(message)
    }
}