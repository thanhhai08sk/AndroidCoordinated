package org.de_studio.diary.android.viewController

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.annotation.CallSuper
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.de_studio.diary.MyApplication
import org.de_studio.diary.android.*
import org.de_studio.diary.base.architecture.*
import org.de_studio.diary.feature.lock.LockHelper
import org.de_studio.diary.screen.base.BaseViewsProvider
import org.de_studio.diary.screen.base.ViewsProvider
import org.de_studio.diary.screen.intro.MyAppIntro
import org.de_studio.diary.screen.login.LoginViewController
import org.de_studio.diary.utils.Cons
import java.lang.ref.WeakReference
import javax.inject.Inject

/**
 * Created by HaiNguyen on 7/22/17.
 */
abstract class BaseActivity<S: ViewState, P : BaseCoordinator<S, E, *, *>, E: Event,  C : Component<*, P>, CH : ComponentHolder<P, C>> : AppCompatActivity(), FirebaseAuth.AuthStateListener
        , ViewController<S, E>
        , InjectionPoint<P,C,CH>{

    @Inject
    protected lateinit var presenter: P
    @Inject
    protected lateinit var viewState: S
    @Inject
    protected lateinit var appEvent: PublishRelay<AppEvent>
    @Inject
    lateinit var preference: Preference
    private var firebaseAuth: FirebaseAuth? = null
    private var lockHelper: LockHelper? = null

    private val permissionDisposable = CompositeDisposable()
    private var viewControllerHelper: ViewControllerHelper<S, E>? = null
    private var firstOpen: Boolean = false
    override var componentHolder: CH? = null
    protected abstract val layoutRes: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences(Cons.SHARED_PREFERENCE, 0)

        if (sharedPreferences.getString(Cons.KEY_CURRENT_USER_UID, null) == null && this !is LoginViewController) {
            if (sharedPreferences.getBoolean(Cons.FIRST_START_KEY, true)) {
                startActivity(Intent(this, MyAppIntro::class.java))
            } else {
                navigateToLoginView(true)
            }
            firstOpen = true
            finish()
        } else {
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseAuth!!.addAuthStateListener(this)
            injectToActivity(this, (intent.extras ?: Bundle(1)).apply { putAll(savedInstanceState ?: Bundle.EMPTY) })
            viewControllerHelper = ViewControllerHelper(WeakReference<ViewController<S, E>>(this), WeakReference<BaseCoordinator<S, E, *, *>>(presenter))
            setContentView(layoutRes)
            bindView(BaseViewsProvider(window.decorView, this))
            if (preference.islockEnabled()) {
                lockHelper = LockHelper(this, preference)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewVisible()
    }

    override fun onStop() {
        viewInvisible()
        super.onStop()
    }

    override fun viewVisible() {}
    override fun viewInvisible() {}
    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    protected fun isLocking() = lockHelper?.isLocking() ?: false

    override fun handleBackPress(): Boolean = false

    override fun onPause() {
        lockHelper?.onActivityStop()
        super.onPause()
        permissionDisposable.clear()
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        permissionDisposable.add(
                appEvent.subscribe { appEvent ->
                    when (appEvent) {
                        is RequestPermission -> ActivityCompat.requestPermissions(
                                this,
                                arrayOf(appEvent.permission.permissionString),
                                appEvent.permission.requestCode
                        )
                    }
                }
        )
        lockHelper?.onActivityStart()
    }

    override fun onBackPressed() {
        if (!handleBackPress()) {
            super.onBackPressed()
        }
    }

    override fun bindView(viewsProvider: ViewsProvider) {
        viewControllerHelper!!.bindView(viewsProvider)
    }

    override fun unbindView() {
        viewControllerHelper?.unbindView()
        if (firebaseAuth != null) {
            firebaseAuth!!.removeAuthStateListener(this)
        }
    }

    @CallSuper
    override fun onDestroy() {
        unbindView()
        viewControllerHelper = null
        super.onDestroy()
    }

    fun navigateToLoginView(firstLaunch: Boolean) {
        startActivity(LoginViewController.getIntent(this, if (firstLaunch) LoginViewController.MODE_SIGN_IN else LoginViewController.MODE_COMPLETELY_SIGN_OUT))
        if (firebaseAuth != null) {
            firebaseAuth!!.removeAuthStateListener(this)
        }
    }

    fun navigateToLoginAndLinkAnonymousAccount() {
        startActivity(LoginViewController.getIntent(this, LoginViewController.MODE_LINK_ANONYMOUS_WITH_GOOGLE_ACCOUNT))
        finish()
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            val sharedPreferences = getSharedPreferences(Cons.SHARED_PREFERENCE, 0)
            if (!sharedPreferences.getBoolean(Cons.FIRST_START_KEY, true)) {
                this.firebaseAuth!!.signOut()
                MyApplication.get(this).releaseUserComponent()
                navigateToLoginView(false)
            }
        }
    }

    protected fun fireEvent(event: E) {
        viewControllerHelper!!.fireEvent(event)
    }

    protected fun fireEvents(vararg events: E) {
        events.forEach { fireEvent(it) }
    }

    protected fun addToAutoDispose(vararg disposables: Disposable) {
        viewControllerHelper!!.addToAutoDispose(*disposables)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        handleActivityResult(ActivityResult.fromOnActivityResultReturn(this, requestCode, resultCode, data))
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> ActivityCompat.finishAfterTransition(this)
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissions.forEachIndexed { index, permString ->
            appEvent.accept(PermissionResult(Permission.fromString(permString), grantResults[index]))
        }
    }

    open protected fun handleActivityResult(result: ActivityResult){}
}