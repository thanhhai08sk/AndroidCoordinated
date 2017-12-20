package org.de_studio.diary.android.adapter

/**
 * Created by HaiNguyen on 10/1/17.
 */
sealed class ItemAction<R: Any>(val itemIndex: Int, var item: R? = null) {
    class Click<R: Any>(itemIndex: Int): ItemAction<R>(itemIndex)
    class LongClick<R: Any>(itemIndex: Int): ItemAction<R>(itemIndex)
    class Delete<R: Any>(itemIndex: Int): ItemAction<R>(itemIndex)
    class Upload<R: Any>(itemIndex: Int): ItemAction<R>(itemIndex)
    class RequestPhotoToDisplay<R: Any>(itemIndex: Int): ItemAction<R>(itemIndex)
    class NewEntry<R: Any>(itemIndex: Int): ItemAction<R>(itemIndex)
    class NewEntryTakePhoto<R: Any>(itemIndex: Int): ItemAction<R>(itemIndex)
    class ToggleFavorite<R: Any>(itemIndex: Int): ItemAction<R>(itemIndex)
    class ToggleFinished<R: Any>(itemIndex: Int): ItemAction<R>(itemIndex)
}