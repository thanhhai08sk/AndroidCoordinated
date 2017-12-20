package org.de_studio.diary.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import org.de_studio.diary.data.ItemId
import org.de_studio.diary.data.sync.*
import org.de_studio.diary.screen.entriesContainer.activity.ActivityViewController
import org.de_studio.diary.screen.entriesContainer.category.CategoryViewController
import org.de_studio.diary.screen.entriesContainer.person.PersonViewController
import org.de_studio.diary.screen.entriesContainer.progress.ProgressViewController
import org.de_studio.diary.screen.entriesContainer.tag.TagViewController
import org.de_studio.diary.utils.Cons
import org.de_studio.diary.utils.Utils
import org.de_studio.diary.utils.extensionFunction.getAppContext
import timber.log.Timber

/**
 * Created by HaiNguyen on 10/6/17.
 */
sealed class ActivityRequest(val requestCode: RequestCode) {
    abstract fun start(context: AbstractContext)

    class PickPlace : ActivityRequest(RequestCode.PICK_PLACE) {

        override fun start(context: AbstractContext) {
            val builder = PlacePicker.IntentBuilder()
            try {
                context.startActivityForResult(builder.build(context.activity), requestCode.ordinal)
            } catch (e: GooglePlayServicesRepairableException) {
                e.printStackTrace()
                Utils.toast(context.activity, "Google Play repair")
            } catch (e: GooglePlayServicesNotAvailableException) {
                e.printStackTrace()
                Utils.toast(context.activity, "Google Play not available")
            }
        }
    }

    class TakePhoto(val output: Uri) : ActivityRequest(RequestCode.TAKE_PHOTO) {

        override fun start(context: AbstractContext) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    .apply { putExtra(MediaStore.EXTRA_OUTPUT, output) }
                    .run { context.startActivityForResult(this, requestCode.ordinal) }
        }
    }

    class PickPhoto : ActivityRequest(RequestCode.PICK_PHOTO) {
        override fun start(context: AbstractContext) {
            Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    .apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    }.run { context.startActivityForResult(this, requestCode.ordinal) }
        }
    }

    class NewItem(val title: String, val itemModel: DataModel) : ActivityRequest(RequestCode.NEW_ITEM) {
        override fun start(context: AbstractContext) {
            val intent = when (itemModel) {
                ProgressDataModel -> ProgressViewController.getIntentToCreateNewProgress(context.activity, title, true, true)
                EntryDataModel -> TODO()
                ActivityDataModel -> ActivityViewController.getIntentToCreateNewActivity(context.activity, title, true, true)
                TagDataModel -> TagViewController.getIntentToCreateNewTag(context.activity, title, true, true)
                CategoryDataModel -> CategoryViewController.getIntentToCreateNewCategory(context.activity, title, true, true)
                PersonDataModel -> PersonViewController.getIntentForNewPeople(context.activity, title, true)
                PlaceDataModel -> TODO()
                PhotoDataModel -> TODO()
                TodoDataModel -> TODO()
                TodoSectionDataModel -> TODO()
            }
            context.startActivityForResult(intent, requestCode.ordinal)
        }
    }

    class PickBackupFile : ActivityRequest(RequestCode.PICK_BACKUP_FILE) {
        override fun start(context: AbstractContext) {
            pickFile(context, requestCode)
        }
    }

    class PickJourneyFile : ActivityRequest(RequestCode.PICK_JOURNEY_FILE) {
        override fun start(context: AbstractContext) {
            pickFile(context, requestCode)
        }
    }

    class PickDayOneFile : ActivityRequest(RequestCode.PICK_DAY_ONE_FILE) {
        override fun start(context: AbstractContext) {
            pickFile(context, requestCode)
        }
    }

    class PickDiaroFile : ActivityRequest(RequestCode.PICK_DIARO_FILE) {
        override fun start(context: AbstractContext) {
            pickFile(context, requestCode)
        }
    }

    companion object {
        fun pickFile(context: AbstractContext, requestCode: RequestCode) {
            getAppContext()
                    .permissionHelper
                    .requestStorage()
                    .filter { it.success }
                    .subscribe {
                        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                        }.run { context.startActivityForResult(this, requestCode.ordinal) }
                    }
        }
    }

}

sealed class ActivityResult(val isSuccess: Boolean){

    class None(): ActivityResult(false)

    class PickPlace(isSuccess: Boolean, val googlePlace: Place?) : ActivityResult(isSuccess)

    class TakePhoto(isSuccess: Boolean) : ActivityResult(isSuccess)

    class PickPhoto(isSuccess: Boolean, val uris: List<Uri>): ActivityResult(isSuccess)

    class NewItem(isSuccess: Boolean, val itemId: ItemId?): ActivityResult(isSuccess)

    class PickBackupFile(isSuccess: Boolean, val uri: Uri?): ActivityResult(isSuccess)

    class PickJourneyFile(isSuccess: Boolean, val uri: Uri?): ActivityResult(isSuccess)

    class PickDayOneFile(isSuccess: Boolean, val uri: Uri?): ActivityResult(isSuccess)

    class PickDiaroFile(isSuccess: Boolean, val uri: Uri?): ActivityResult(isSuccess)

    companion object {
        fun isSuccess(resultCode: Int) = resultCode == Activity.RESULT_OK
        fun fromOnActivityResultReturn(context: Context, requestCode: Int, resultCode: Int, data: Intent?): ActivityResult {
            val isSuccess = isSuccess(resultCode)
            return when (RequestCode.fromInt(requestCode)) {
                RequestCode.PICK_PLACE -> ActivityResult
                        .PickPlace(
                                isSuccess(resultCode),
                                if (isSuccess) PlacePicker.getPlace(context, data) else null
                        )

                RequestCode.TAKE_PHOTO -> ActivityResult.TakePhoto(isSuccess)

                RequestCode.PICK_PHOTO -> {
                    val uris = mutableListOf<Uri>()
                    if (data != null) {
                        val clipData = data.clipData
                        if (clipData != null) {
                            (0 until clipData.itemCount)
                                    .map { clipData.getItemAt(it) }
                                    .filter { it.uri != null }
                                    .forEach { uris.add(it.uri) }
                        } else {
                            if (data.data != null) uris.add(data.data)
                        }
                    }
                    ActivityResult.PickPhoto(isSuccess, uris)
                }

                RequestCode.NEW_ITEM -> ActivityResult.NewItem(isSuccess, data?.getStringExtra(Cons.KEY_ITEM_ID)?.let { ItemId(it) })
                RequestCode.PICK_BACKUP_FILE -> ActivityResult.PickBackupFile(isSuccess, data?.data)
                RequestCode.PICK_JOURNEY_FILE -> ActivityResult.PickJourneyFile(isSuccess, data?.data)
                RequestCode.PICK_DAY_ONE_FILE -> ActivityResult.PickDayOneFile(isSuccess, data?.data)
                RequestCode.PICK_DIARO_FILE -> ActivityResult.PickDiaroFile(isSuccess, data?.data)

                else -> {
                    Timber.e("ActivityResult none, when getting activityResult with code = $requestCode")
                    Utils.logIntentContent(data)
                    ActivityResult.None()
                }
            }
        }
    }
}

enum class RequestCode {
    PICK_PLACE, TAKE_PHOTO, PICK_PHOTO, NEW_ITEM, PICK_BACKUP_FILE, PICK_JOURNEY_FILE, PICK_DAY_ONE_FILE, PICK_DIARO_FILE, MAKE_PURCHASE;
    companion object {
        private val map = RequestCode.values().associateBy(RequestCode::ordinal);
        fun fromInt(ordinal: Int) = map[ordinal]
    }
}
