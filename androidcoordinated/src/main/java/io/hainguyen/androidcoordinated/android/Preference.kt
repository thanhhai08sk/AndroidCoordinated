package org.de_studio.diary.android

import android.content.Context
import android.content.SharedPreferences
import io.reactivex.Observable
import org.de_studio.diary.utils.Cons
import org.de_studio.diary.utils.extensionFunction.currentTime
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by HaiNguyen on 9/1/17.
 */
interface Preference {
    fun onChangeObservable(): Observable<String>
    fun getAppStartTime(): Long
    fun updateAppStartTime() = setLong(APP_START_TIME, currentTime())
    fun isGuideEditEntryDone(): Boolean
    fun setGuideEditEntryDone()
    fun islockEnabled(): Boolean
    fun setLockEnable()
    fun lockTimeout(): Long
    fun setLockTimeout(timeout: Long)
    fun getLastSeen(): Long
    fun updateLastSeen()
    fun enableUnlockByFingerPrint()
    fun disableUnlockByFingerPrint()
    fun isUnlockByFingerPrintEnabled(): Boolean
    fun isWifiUploadOnly(): Boolean
    fun setUserUID(uid: String)
    fun isConvertPlaceToPlacesCompleted(): Boolean
    fun setConvertPlaceToPlacesCompleted()
    fun setUserAsPremium()
    fun setUserAsFree()
    fun isAnonymous(): Boolean
    fun setUserAsAnonymous()
    fun setUserAsNotAnonymous()
    fun isProUser(): Boolean
    fun updateLastTimeSync()
    fun getLastTimeSync(): Long
    fun getUserUID(): String
    fun setUserName(name: String)
    fun getUserName(): String?
    fun getUserEmail(): String
    fun setString(key: String, value: String)
    fun getLong(key: String, defaultValue: Long): Long
    fun setLong(key: String, value: Long)
    fun getInt(key: String, defaultValue: Int): Int
    fun setInt(key: String, value: Int)
    fun getString(key: String, defaultValue: String?): String?
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun setBoolean(key: String, value: Boolean)


    companion object {
        const val CONVERT_PLACE_TO_PLACES_SUCCESS = "placesConverted"
        const val IS_WIFI_ONLY = "wifi_only"
        const val IS_ANONYMOUS = "is_anonymous"
        const val LAST_TIME_SYNC = "lastSync"
        const val IS_WIFI_ONLY_DEFAULT = true
        const val GUIDE_EDIT_ENTRY_DONE = "guideEditEntry"
        const val LOCK_ENABLED = "useLock"
        const val LOCK_TIMEOUT = "lockTimeout"
        val LOCK_TIMEOUT_DEFAULT = TimeUnit.MINUTES.toMillis(5)
        const val LAST_SEEN_APP = "lastSeen"
        const val APP_START_TIME = "appStartTime"
        const val UNLOCK_BY_FINGER_PRINT = "use_fingerprint"
        const val IS_PREMIUM_USER = "premium_user"
    }
}

class PreferenceImpl(app: Context) : Preference{
    val sharedPref = app.getSharedPreferences(Cons.SHARED_PREFERENCE , Context.MODE_PRIVATE)
    override fun onChangeObservable(): Observable<String> {
        return Observable
                .create { emitter ->
                    SharedPreferences
                            .OnSharedPreferenceChangeListener { _, key -> emitter.onNext(key) }
                            .apply {
                                sharedPref.registerOnSharedPreferenceChangeListener(this)
                                emitter.setCancellable { sharedPref.unregisterOnSharedPreferenceChangeListener(this) }
                            }
                }
    }

    override fun getAppStartTime(): Long = getLong(Preference.APP_START_TIME, currentTime())
    override fun updateAppStartTime() = setLong(Preference.APP_START_TIME, currentTime())
    override fun isGuideEditEntryDone(): Boolean =
            getBoolean(Preference.GUIDE_EDIT_ENTRY_DONE, false)
    override fun setGuideEditEntryDone() {
        setBoolean(Preference.GUIDE_EDIT_ENTRY_DONE, true)
    }

    override fun islockEnabled(): Boolean = getBoolean(Preference.LOCK_ENABLED, false)
    override fun setLockEnable() = setBoolean(Preference.LOCK_ENABLED, true)
    override fun lockTimeout(): Long =
            getLong(Preference.LOCK_TIMEOUT, Preference.LOCK_TIMEOUT_DEFAULT)
    override fun setLockTimeout(timeout: Long) = setLong(Preference.LOCK_TIMEOUT, timeout)
    override fun getLastSeen(): Long = getLong(Preference.LAST_SEEN_APP, 0)
    override fun updateLastSeen() = setLong(Preference.LAST_SEEN_APP, currentTime())

    override fun enableUnlockByFingerPrint() = setBoolean(Preference.UNLOCK_BY_FINGER_PRINT, true)
    override fun disableUnlockByFingerPrint() = setBoolean(Preference.UNLOCK_BY_FINGER_PRINT, false)
    override fun isUnlockByFingerPrintEnabled(): Boolean = getBoolean(Preference.UNLOCK_BY_FINGER_PRINT, false)


    override fun isWifiUploadOnly(): Boolean =
            sharedPref.getBoolean(Preference.IS_WIFI_ONLY, Preference.IS_WIFI_ONLY_DEFAULT)

    override fun setUserUID(uid: String) {
        setString(Cons.KEY_CURRENT_USER_UID, uid)
    }

    override fun isConvertPlaceToPlacesCompleted(): Boolean =
            sharedPref.getBoolean(Preference.CONVERT_PLACE_TO_PLACES_SUCCESS, false)

    override fun setConvertPlaceToPlacesCompleted() {
        setBoolean(Preference.CONVERT_PLACE_TO_PLACES_SUCCESS, true)
    }

    override fun setUserAsPremium(){
        Timber.e("setUserAsPremium ")
        setBoolean(Preference.IS_PREMIUM_USER, true)
    }

    override fun setUserAsFree() {
        Timber.e("setUserAsFree ")
        setBoolean(Preference.IS_PREMIUM_USER, false)
    }

    override fun isAnonymous(): Boolean = getBoolean(Preference.IS_ANONYMOUS, false)

    override fun setUserAsAnonymous() = setBoolean(Preference.IS_ANONYMOUS, true)

    override fun setUserAsNotAnonymous() = setBoolean(Preference.IS_ANONYMOUS, false)

    override fun isProUser(): Boolean = sharedPref.getBoolean(Cons.PREMIUM_USER_KEY, false)

    override fun updateLastTimeSync() {
        setLong(Preference.LAST_TIME_SYNC, currentTime())
    }

    override fun getLastTimeSync(): Long = sharedPref.getLong(Preference.LAST_TIME_SYNC, 0)

    override fun getUserUID(): String =
            getString(Cons.KEY_CURRENT_USER_UID, "")!!

    override fun setUserName(name: String) {
        setString(Cons.KEY_CURRENT_USER_NAME, name)
    }
    override fun getUserName(): String? = getString(Cons.KEY_CURRENT_USER_NAME, null)

    override fun getUserEmail(): String = getString(Cons.KEY_CURRENT_USER_EMAIL, "")!!

    override fun setString(key: String, value: String) {
        sharedPref.edit().putString(key, value).apply()
    }

    override fun getLong(key: String, defaultValue: Long): Long =
            sharedPref.getLong(key, defaultValue)

    override fun setLong(key: String, value: Long) {
        sharedPref.edit().putLong(key, value).apply()
    }

    override fun getInt(key: String, defaultValue: Int): Int =
            sharedPref.getInt(key, defaultValue)

    override fun setInt(key: String, value: Int) {
        sharedPref.edit().putInt(key, value).apply()
    }

    override fun getString(key: String, defaultValue: String?): String? =
            sharedPref.getString(key, defaultValue)

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
            sharedPref.getBoolean(key, defaultValue)

    override fun setBoolean(key: String, value: Boolean) {
        sharedPref.edit().putBoolean(key, value).apply()
    }
}