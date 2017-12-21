package io.hainguyen.androidcoordinated.android

import android.content.pm.PackageManager

/**
 * Created by HaiNguyen on 9/9/17.
 */
open class AppEvent
class RequestPermission(val permission: Permission): AppEvent()
class PermissionResult(val permission: Permission, val granted: Int) : AppEvent() {
    fun isOk() = granted == PackageManager.PERMISSION_GRANTED
}
