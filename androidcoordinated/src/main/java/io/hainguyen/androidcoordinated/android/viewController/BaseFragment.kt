package org.de_studio.diary.android.viewController

import android.content.Intent
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.Unbinder
import io.reactivex.disposables.Disposable
import org.de_studio.diary.android.ActivityResult
import org.de_studio.diary.android.InjectionPoint
import org.de_studio.diary.base.architecture.*
import org.de_studio.diary.screen.base.BaseViewsProvider
import org.de_studio.diary.screen.base.ViewsProvider
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

/**
 * Created by HaiNguyen on 8/12/17.
 */

abstract class BaseFragment<P : BaseCoordinator<S,E, *, *>, S : ViewState, E: Event, C : Component<*, P>, CH : ComponentHolder<P, C>>
    : Fragment(), ViewController<S, E>, InjectionPoint<P, C, CH> {
    internal var unbinder: Unbinder? = null
    @Inject
    protected lateinit var presenter: P
    @Inject
    protected lateinit var viewState: S
    private var viewControllerHelper: ViewControllerHelper<S, E>? = null
    override var componentHolder: CH? = null
    protected abstract val layoutRes: Int


    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectToFragment(
                this,
                (arguments ?: Bundle(1))
                        .apply { if (savedInstanceState != null) this.putAll(savedInstanceState) }
        )
        viewControllerHelper = ViewControllerHelper(WeakReference<ViewController<S, E>>(this), WeakReference<BaseCoordinator<S,E, *, *>>(presenter))
    }

    override fun onStart() {
        super.onStart()
        viewVisible()
    }

    override fun onStop() {
        Timber.e("viewInvisible")
        viewInvisible()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewControllerHelper = null
    }

    override fun handleBackPress(): Boolean = false

    override fun bindView(viewsProvider: ViewsProvider) {
        viewControllerHelper!!.bindView(viewsProvider)
    }

    @CallSuper
    override fun unbindView() {
        viewControllerHelper!!.unbindView()
    }

    override fun viewVisible() {}
    override fun viewInvisible() {}

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater!!.inflate(layoutRes, container, false)

    @CallSuper
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(BaseViewsProvider(view!!, activity))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        handleActivityResult(ActivityResult.fromOnActivityResultReturn(this.activity, requestCode, resultCode, data))
        super.onActivityResult(requestCode, resultCode, data)
    }

    open protected fun handleActivityResult(result: ActivityResult){}

    protected fun addToAutoDispose(vararg disposable: Disposable) {
        viewControllerHelper?.addToAutoDispose(*disposable)
    }

    override fun onDestroyView() {
        unbindView()
        super.onDestroyView()
    }

    protected fun fireEvent(event: E) {
        viewControllerHelper!!.fireEvent(event)
    }
    @CallSuper
    open fun clear() {
    }
}
