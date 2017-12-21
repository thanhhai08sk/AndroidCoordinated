package io.hainguyen.androidcoordinated.coordinated

/**
 * Created by HaiNguyen on 11/7/17.
 */
interface CrashReporter {
    fun logException(e: Throwable)
    fun log(message: String)
}