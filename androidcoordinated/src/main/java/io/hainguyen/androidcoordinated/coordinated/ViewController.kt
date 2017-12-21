package org.de_studio.diary.base.architecture

import io.hainguyen.androidcoordinated.coordinated.ViewsProvider
import io.reactivex.Observable

/**
 * Created by HaiNguyen on 11/30/16.
 */

interface ViewController<in S: ViewState, E: Event> {
    fun render(state: S)
    fun handleError(error: Throwable)
    fun bindView(viewsProvider: ViewsProvider)
    fun setupViews()
    fun mapViewEventsToObservables(): ArrayList<Observable<out E>>
    fun unbindView()
    fun viewVisible()
    fun viewInvisible()
    fun handleBackPress(): Boolean
}

