package org.de_studio.diary.base.architecture

import android.arch.lifecycle.ViewModel

/**
 * Created by HaiNguyen on 10/5/17.
 */
abstract class ComponentHolder<out P: BaseCoordinator<*, *, *, *>, C : Component<*, P>> : ViewModel() {
    var component: C? = null

    override fun onCleared() {
        component?.presenter?.destroy()
        component = null
        super.onCleared()
    }
}

interface Component<in V: Any, out P : BaseCoordinator<*,*, *, *>>{
    val presenter: P
    fun inject(view: V)
}