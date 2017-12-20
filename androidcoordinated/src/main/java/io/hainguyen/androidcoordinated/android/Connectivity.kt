package org.de_studio.diary.android

import android.content.Context
import org.de_studio.diary.utils.extensionFunction.isWifi

/**
 * Created by HaiNguyen on 9/1/17.
 */
class Connectivity(val preference: Preference, val app: Context, val isAnonymous: Boolean) {
    fun canUploadPhoto(): Boolean =
            (app.isWifi() || !preference.isWifiUploadOnly()) && !isAnonymous
}