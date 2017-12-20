package org.de_studio.diary.base.architecture

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.de_studio.diary.utils.extensionFunction.publishMerge

/**
 * Created by HaiNguyen on 8/27/17.
 */
interface EventComposer<E: Event> {
    fun getComposer(): ObservableTransformer<E, Action>
}

abstract class BaseEventComposer<E: Event> : EventComposer<E> {
    override fun getComposer(): ObservableTransformer<E, Action> {
        return ObservableTransformer { obs: Observable<E> ->
            obs.publishMerge {
                toActionObservable(this)
            }
        }
    }

    abstract fun toActionObservable(events: Observable<E>): ArrayList<Observable<out Action>>
}
