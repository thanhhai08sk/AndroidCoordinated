package io.hainguyen.androidcoordinated.android

import android.Manifest
import android.content.Context
import com.jakewharton.rxrelay2.PublishRelay
import io.hainguyen.androidcoordinated.utils.hasPermission
import io.reactivex.Single

/**
 * Created by HaiNguyen on 9/9/17.
 */
class PermissionHelper(private val context: Context, private val appEventRL: PublishRelay<AppEvent>) {
    sealed class PermissionRequestResult(val success: Boolean){
        object Granted : PermissionRequestResult(true)
        object Denied : PermissionRequestResult(false)
    }

    fun hasLocationPermission(): Boolean =
            context.hasPermission(LocationPermission)
    fun hasStoragePermission(): Boolean =
            context.hasPermission(StoragePermission)

    fun requestLocation(): Single<PermissionRequestResult> {
        return Single.create { resultEmitter ->
            if (hasLocationPermission()) resultEmitter.onSuccess(PermissionRequestResult.Granted)
            else {
                appEventRL
                        .ofType(PermissionResult::class.java)
                        .filter { it.permission is LocationPermission }
                        .firstOrError()
                        .subscribe { result ->
                            resultEmitter.onSuccess(
                                    if (result.isOk()) PermissionRequestResult.Granted
                                    else PermissionRequestResult.Denied
                            )
                        }
                appEventRL.accept(RequestPermission(LocationPermission))
            }
        }
    }

    fun requestStorage(): Single<PermissionRequestResult> {
        return Single.create { emitter ->
            if (hasStoragePermission()) emitter.onSuccess(PermissionRequestResult.Granted)
            else {
                appEventRL
                        .ofType(PermissionResult::class.java)
                        .filter { it.permission is StoragePermission }
                        .firstOrError()
                        .subscribe { result ->
                            emitter.onSuccess(
                                    if (result.isOk()) PermissionRequestResult.Granted
                                    else PermissionRequestResult.Denied
                            )
                        }
                appEventRL.accept(RequestPermission(StoragePermission))
            }
        }
    }
}

sealed class Permission(val requestCode: Int, val permissionString: String){
    companion object {
        fun fromString(permissionString: String): Permission = when (permissionString) {
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> StoragePermission
            Manifest.permission.ACCESS_FINE_LOCATION -> LocationPermission
            else -> TODO()
        }
    }

    enum class PermissionRequest(val code: Int, val string: String) {
        STORAGE(1, Manifest.permission.WRITE_EXTERNAL_STORAGE ),
        LOCATION(2, Manifest.permission.ACCESS_FINE_LOCATION)
    }
}

class None: Permission(0, "")
object StoragePermission : Permission(PermissionRequest.STORAGE.code, PermissionRequest.STORAGE.string)
object LocationPermission : Permission(PermissionRequest.LOCATION.code, PermissionRequest.LOCATION.string)

