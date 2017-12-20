package org.de_studio.diary.android

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import org.de_studio.diary.MyApplication
import org.de_studio.diary.base.architecture.BaseCoordinator
import org.de_studio.diary.base.architecture.Component
import org.de_studio.diary.base.architecture.ComponentHolder
import org.de_studio.diary.dagger2.user.UserComponent

/**
 * Created by HaiNguyen on 10/5/17.
 */
interface InjectionPoint<out P : BaseCoordinator<*,*, *, *>, C : Component<*, P>, CH : ComponentHolder<P, C>> {
    var componentHolder: CH?
    val componentHolderClass: Class<CH>
    val component: C get() = componentHolder!!.component!!
    fun getDaggerComponent(userComponent: UserComponent, bundle: Bundle): C
    fun inject(component: C)
    fun injectToFragment(fragment: Fragment, androidBundle: Bundle?){
        componentHolder = ViewModelProviders.of(fragment).get(componentHolderClass)
                .apply { inject(this, androidBundle ?: Bundle.EMPTY) }
    }

    fun injectToActivity(activity: FragmentActivity, androidBundle: Bundle?){
        componentHolder = ViewModelProviders.of(activity).get(componentHolderClass)
                .apply { inject(this, androidBundle ?: Bundle.EMPTY) }
    }

    private fun inject(componentHolder: CH, androidBundle: Bundle){
        if (componentHolder.component == null) {
            val userComponent = MyApplication.getContext().userComponent
            if (userComponent != null) {
                componentHolder.component = getDaggerComponent(userComponent, androidBundle)
            } else
                throw IllegalArgumentException("userComponent can not be null")
        }
        inject(componentHolder.component!!)
    }
}

