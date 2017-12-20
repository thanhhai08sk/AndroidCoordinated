package org.de_studio.diary.android.process

import android.app.Notification
import org.de_studio.diary.R
import org.de_studio.diary.base.architecture.Result
import org.de_studio.diary.business.importAndBackup.foreignImport.ForeignImportSource
import org.de_studio.diary.business.usecase.SettingsUseCase
import org.de_studio.diary.utils.extensionFunction.getAppContext
import org.de_studio.diary.utils.extensionFunction.notification

/**
 * Created by HaiNguyen on 10/31/17.
 */
sealed class BackgroundTask(val tag: String, open val isCompleted: Boolean){
    abstract val onProgressNoti: Notification
    abstract val completeNotification: Notification?

    class UploadPhotos(override val onProgressNoti: Notification, isCompleted: Boolean) : BackgroundTask("uploadPhotos", isCompleted) {
        override val completeNotification: Notification? get() = null
    }

    class ImportFromJourney(override val onProgressNoti: Notification, isCompleted: Boolean): BackgroundTask("importFromJourney", isCompleted){
        override val completeNotification: Notification? by lazy {
            val context = getAppContext()
            notification(context){
                setContentTitle(context.getString(R.string.import_from_jouney_completed))
                setSmallIcon(R.drawable.ic_entry3)
            }
        }
    }

    class ImportFromForeignSource(val source: ForeignImportSource, override val onProgressNoti: Notification, isCompleted: Boolean) : BackgroundTask("importFrom ${source.name}", isCompleted) {
        override val completeNotification: Notification? by lazy {
            val context = getAppContext()
            notification(context){
                setContentTitle(context.getString(R.string.import_from_foreign_source_completed, source.name))
                setSmallIcon(R.drawable.ic_entry3)
            }
        }
    }

    class ExportToLocal(override val onProgressNoti: Notification, isCompleted: Boolean): BackgroundTask("exportToLocal", isCompleted){
        override val completeNotification: Notification? by lazy {
            notification(getAppContext()){
                setContentTitle(getAppContext().getString(R.string.export_to_local_storage_completed))
                setSmallIcon(R.drawable.ic_entry3)
            }
        }
    }

    class ImportFromNativeData(override val onProgressNoti: Notification, isCompleted: Boolean) : BackgroundTask("importFromNativeData", isCompleted) {
        override val completeNotification: Notification? by lazy {
            notification(getAppContext()){
                setContentTitle(getAppContext().getString(R.string.successfully_import_data))
                setSmallIcon(R.drawable.ic_entry3)
            }
        }
    }



    companion object {
        fun forUploadingPhoto(onProgress: Int, total: Int, isCompleted: Boolean): UploadPhotos {
            val context = getAppContext()
            return notification(context) {
                setContentTitle(context.getString(R.string.processing_photo))
                setContentText(context.getString(R.string.processing_photo_detail, onProgress, total))
                setSmallIcon(R.drawable.ic_multiple_photos)
            }.let { UploadPhotos(it,isCompleted) }
        }

        fun forImportFromJourney(onProgress: Int, isCompleted: Boolean): ImportFromJourney {
            val context = getAppContext()
            return notification(context){
                setContentTitle(context.getString(R.string.import_from_journey))
                setContentText(context.getString(R.string.import_from_foreign_source_count, onProgress))
                setSmallIcon(R.drawable.ic_entry3)

            }.let { ImportFromJourney(it, isCompleted) }
        }

        fun forImportFromForeignSource(source: ForeignImportSource, onProgress: Int, isCompleted: Boolean): ImportFromForeignSource {
            val context = getAppContext()
            return notification(context){
                setContentTitle(context.getString(R.string.import_from_foreign_source, source.name))
                setContentText(context.getString(R.string.import_from_foreign_source_count, onProgress))
                setSmallIcon(R.drawable.ic_entry3)
            }.let { ImportFromForeignSource(source, it, isCompleted) }
        }

        fun forExportToLocalStorage(status: Result, isCompleted: Boolean): ExportToLocal {
            return notification(getAppContext()){
                val context = getAppContext()
                setContentTitle(context.getString(R.string.export_to_local_storage))
                setContentText(
                        when (status) {
                            is SettingsUseCase.ExportToLocalStorageStarted -> context.getString(R.string.started)
                            is SettingsUseCase.ExportToLocalStoragePreparingPhotos -> context.getString(R.string.preparing_photos, status.photosCount)
                            is SettingsUseCase.ExportToLocalStorageExporting -> context.getString(R.string.exporting_file)
                            is SettingsUseCase.ExportToLocalStorageSuccess -> context.getString(R.string.export_to_local_storage_completed)
                            is SettingsUseCase.ExportToLocalStorageError -> context.getString(R.string.error_when_exporting_file)
                            else -> throw IllegalArgumentException("Making notification for exporting data, status = ${status.javaClass.simpleName}")
                        }
                )
                setSmallIcon(R.drawable.ic_entry3)
            }.let { ExportToLocal(it, isCompleted) }
        }

        fun forImportFromNativeData(status: Result, isCompleted: Boolean): ImportFromNativeData {
            val context = getAppContext()
            return notification(context){
                setContentTitle(context.getString(R.string.importing))
                setContentText(
                        when (status) {
                            is SettingsUseCase.ImportFromNativeData.Started -> context.getString(R.string.preparing_file)
                            is SettingsUseCase.ImportFromNativeData.Syncing -> context.getString(R.string.syncing)
                            is SettingsUseCase.ImportFromNativeData.Success -> context.getString(R.string.successfully_import_data)
                            else -> throw IllegalArgumentException("Making notification for import from native data, status = ${status.javaClass.simpleName}")
                        }
                )
            }.let { ImportFromNativeData(it, isCompleted) }
        }
    }

}

