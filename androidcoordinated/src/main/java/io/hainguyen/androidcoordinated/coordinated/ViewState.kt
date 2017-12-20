package org.de_studio.diary.base.architecture

import android.support.annotation.CallSuper
import org.de_studio.diary.screen.action.ViewsInstantState
import timber.log.Timber

/**
 * Created by HaiNguyen on 8/24/17.
 */
open class ViewState(
        var handleError: Boolean = false,
        var error: Throwable? = null,
        var renderContent: Boolean = false,
        var finished: Boolean = false,
        var viewsState: ViewsInstantState? = null
) {

    @CallSuper
    open fun reset() {
        handleError = false
        error = null
        renderContent = false
        finished = false
    }

    fun handleError(error: Throwable): ViewState {
        Timber.e("handleError: $error")
        this.error = error
        handleError = true
        return this
    }

    fun renderContent(): ViewState {
        renderContent = true
        return this
    }

    open fun finishView(): ViewState {
        Timber.e("finishView finished")
        finished = true
        return this
    }

    fun storeViewsInstantState(state: ViewsInstantState): ViewState {
        Timber.e("storeViewsInstantState $state")
        viewsState = state
        return this
    }
}