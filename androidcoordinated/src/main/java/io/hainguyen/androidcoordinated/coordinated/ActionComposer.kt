package org.de_studio.diary.base.architecture

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.de_studio.diary.utils.extensionFunction.publishMerge

/**
 * Created by HaiNguyen on 8/5/17.
 */
interface ActionComposer {
    fun getComposer(): ObservableTransformer<Action, Result>
    //Remember to: this.ofType(JustResult::class.java).compose(justResultTrans),
}

abstract class BaseActionComposer : ActionComposer {
    override fun getComposer(): ObservableTransformer<Action, Result> {
        return ObservableTransformer { obs ->
            obs
                    .publishMerge {
                val list = toListOfResultObservable(this)
                list.add(this.ofType(UseCase::class.java).flatMap { it.execute() })
                list
            }
        }
    }

    abstract fun toListOfResultObservable(actions: Observable<Action>): ArrayList<Observable<Result>>
}

class UseCaseActionComposer : ActionComposer {
    override fun getComposer(): ObservableTransformer<Action, Result> {
        return ObservableTransformer { obs ->
            obs.flatMap { (it as UseCase).execute() }
        }
    }
}


