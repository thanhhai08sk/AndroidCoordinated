package org.de_studio.diary.android

import android.content.pm.PackageManager
import org.de_studio.diary.business.NewEntryInfo

/**
 * Created by HaiNguyen on 9/9/17.
 */
open class AppEvent
class RequestPermission(val permission: Permission): AppEvent()
class PermissionResult(val permission: Permission, val granted: Int) : AppEvent() {
    fun isOk() = granted == PackageManager.PERMISSION_GRANTED
}
class EntryDeleted(val entryId: String): AppEvent()
class NewEntry(val newEntryInfo: NewEntryInfo): AppEvent()
object ShowFab: AppEvent()
object HideFab: AppEvent()
object WelcomeNewUser: AppEvent()
object SyncAllFinished: AppEvent()
object PremiumUserConfirmed: AppEvent()
