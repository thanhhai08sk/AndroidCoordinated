package org.de_studio.diary.android

import android.content.Context
import android.provider.MediaStore
import io.reactivex.Single
import org.de_studio.diary.MyApplication
import org.de_studio.diary.utils.extensionFunction.currentTime
import org.de_studio.diary.utils.extensionFunction.getInputStream
import timber.log.Timber
import java.io.File
import java.io.InputStream

/**
 * Created by HaiNguyen on 10/12/17.
 */
class RecentPhotosProvider(val context: Context) {
    fun getRecentPhotos(): Single<ArrayList<DevicePhoto>> {
        return Single.create { singleSubscriber ->
            val time = currentTime()
            val returnList = ArrayList<DevicePhoto>()
            val projection = arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images.ImageColumns.MIME_TYPE)
            val cursor = context.contentResolver
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC")

            var imagePath: String
            var imageFile: File
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    imagePath = cursor.getString(1)
                    imageFile = File(imagePath)
                    if (imageFile.canRead() && imageFile.exists()) {
                        returnList.add(DevicePhoto.File(imageFile))
                    }
                } while (cursor.moveToNext() && returnList.size < 50)
                cursor.close()
            }
            Timber.e("getRecentPhotos, time = ${currentTime() - time}")
            singleSubscriber.onSuccess(returnList)
        }
    }
}

sealed class DevicePhoto{
    abstract fun getInputStream(): InputStream
    abstract val uri: android.net.Uri


    class File(val file: java.io.File) : DevicePhoto() {
        override val uri: android.net.Uri
            get() = android.net.Uri.fromFile(file)

        override fun getInputStream(): InputStream = file.inputStream()
    }

    class Uri(override val uri: android.net.Uri) : DevicePhoto() {
        override fun getInputStream(): InputStream = uri.getInputStream(MyApplication.getContext())

    }
}
