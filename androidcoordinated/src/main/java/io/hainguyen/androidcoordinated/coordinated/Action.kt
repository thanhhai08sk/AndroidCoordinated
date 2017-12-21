package org.de_studio.diary.base.architecture

import io.reactivex.Observable

/**
 * Created by HaiNguyen on 8/5/17.aa
 */
open class Action

abstract class UseCase : Action() {
    abstract fun execute(): Observable<Result>
}

object DoNothingUseCase : UseCase() {
    override fun execute(): Observable<Result> {
        return Observable.empty()
    }
}
class JustResult(val result: Result) : UseCase() {
    override fun execute(): Observable<Result> = Observable.just(result)
}