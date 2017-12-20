package org.de_studio.diary.android.viewController

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.Unbinder
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.de_studio.diary.R
import org.de_studio.diary.android.ActivityResult
import org.de_studio.diary.android.InjectionPoint
import org.de_studio.diary.base.architecture.*
import org.de_studio.diary.screen.base.BaseViewsProvider
import org.de_studio.diary.screen.base.ViewsProvider
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

/**
 * Created by HaiNguyen on 10/11/17.
 */
abstract class BaseDialogFragment<P : BaseCoordinator<S,E, *, *>, S : ViewState, E: Event, C : Component<*, P>, CH : ComponentHolder<P, C>>
    : DialogFragment(), ViewController<S, E>, InjectionPoint<P, C, CH> {
    internal var unbinder: Unbinder? = null
    @Inject
    protected lateinit var presenter: P
    @Inject
    protected lateinit var viewState: S
    private var viewControllerHelper: ViewControllerHelper<S, E>? = null
    override var componentHolder: CH? = null
    protected abstract val layoutRes: Int
    protected open val dialogTheme: Int = R.style.ProgressContent
    protected abstract val fullScreen: Boolean
    protected open val heightRatio = 0.90f
    private val dismissRL = PublishRelay.create<Any>()


    fun onDismiss(): Observable<Any> = dismissRL

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectToFragment(
                this,
                (arguments ?: Bundle(1))
                        .apply { if (savedInstanceState != null) this.putAll(savedInstanceState) }
        )
        viewControllerHelper = ViewControllerHelper(WeakReference<ViewController<S, E>>(this), WeakReference<BaseCoordinator<S, E, *, *>>(presenter))
        if (fullScreen) setStyle(DialogFragment.STYLE_NORMAL, dialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = super.onCreateDialog(savedInstanceState)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return object : Dialog(activity, theme) {
            override fun onBackPressed() {
                handleBackPress()
            }
        }
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

    override fun onResume() {
        super.onResume()
        if (dialog != null && !fullScreen) {
            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels
            val scale = resources.displayMetrics.density
            var dialogWidth = 0
            var dialogHeight = 0
            if (width < height) {
                dialogWidth = (width * 0.95f).toInt()
                dialogHeight = (dialogWidth * heightRatio).toInt()
            } else {
                dialogWidth = (350 * scale).toInt()
                dialogHeight = (dialogWidth * heightRatio).toInt()
            }
            dialog.window!!.setLayout(dialogWidth, dialogHeight)

//            dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewControllerHelper = null
    }

    override fun handleBackPress(): Boolean {
        dismiss()
        return true
    }

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

    open protected fun handleActivityResult(result: ActivityResult) {}

    override fun onDestroyView() {
        unbindView()
        super.onDestroyView()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        onDialogDismissed()
        super.onDismiss(dialog)
    }

    protected fun fireEvent(event: E) {
        viewControllerHelper!!.fireEvent(event)
    }

    protected fun addToAutoDispose(vararg disposable: Disposable) {
        viewControllerHelper?.addToAutoDispose(*disposable)
    }

    protected fun finishView() {
        dismiss()
    }

    open fun onDialogDismissed() {
        dismissRL.accept(Any())
    }
    @CallSuper
    open fun clear() {
    }
}