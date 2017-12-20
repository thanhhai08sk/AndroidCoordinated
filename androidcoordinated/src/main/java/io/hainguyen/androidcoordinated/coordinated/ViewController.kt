package org.de_studio.diary.base.architecture

import io.reactivex.Observable
import org.de_studio.diary.screen.base.ViewsProvider

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

