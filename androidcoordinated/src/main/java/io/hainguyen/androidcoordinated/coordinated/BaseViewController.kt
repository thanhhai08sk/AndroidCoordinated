package org.de_studio.diary.base.architecture

import io.reactivex.disposables.Disposable
import org.de_studio.diary.screen.base.ViewsProvider
import java.lang.ref.WeakReference

/**
 * Created by HaiNguyen on 10/2/17.
 */
abstract class BaseViewController<S: ViewState, out P: BaseCoordinator<S, E,*, *>, E: Event>(
        val presenter: P,
        val viewState: S
) : ViewController<S, E> {

    private val viewControllerHelper: ViewControllerHelper<S, E> by lazy(LazyThreadSafetyMode.NONE) { ViewControllerHelper(WeakReference<ViewController<S, E>>(this), WeakReference<BaseCoordinator<S, E,*, *>>(presenter)) }

    override fun bindView(viewsProvider: ViewsProvider) {
        viewControllerHelper.bindView(viewsProvider)
    }

    override fun unbindView() {
        viewControllerHelper.unbindView()
    }

    override fun viewVisible() {
    }

    override fun viewInvisible() {
    }

    override fun handleBackPress(): Boolean = false

    protected fun fireEvent(event: E) {
        viewControllerHelper.fireEvent(event)
    }

    protected fun addToAutoDispose(vararg disposables: Disposable) {
        viewControllerHelper.addToAutoDispose(*disposables)
    }
}

