package org.de_studio.diary.base.architecture

import android.support.annotation.CallSuper
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.realm.Realm
import org.de_studio.diary.android.Schedulers
import org.de_studio.diary.android.crashReporter.CrashReporter
import org.de_studio.diary.business.JustResult
import org.de_studio.diary.utils.EmissionDeferer
import timber.log.Timber

/**
 * Created by HaiNguyen on 7/21/17.
 */
abstract class BaseCoordinator< M : ViewState, EV: Event, out T: ActionComposer, out E: EventComposer<EV>>(
        val viewState: M,
        val actionComposer: T,
        val eventComposer: E,
        val resultComposer: ResultComposer,
        val realm: Realm,
        val schedulers: Schedulers
) {
    private val onEvent = PublishRelay.create<EV>()
    private val onAction = PublishRelay.create<Action>()
    private val onResult = PublishRelay.create<Result>()
    private val onViewState = PublishRelay.create<M>()

    private val onDetachDisposable = CompositeDisposable()
    private val onDestroyDisposable = CompositeDisposable()
    private val resultDeferer: EmissionDeferer<Result> = EmissionDeferer()
    private val eventDeferer: EmissionDeferer<EV> = EmissionDeferer()

    @CallSuper open fun bindViewController(viewEventObservable: Observable<EV>, observeViewState: (Observable<M>) -> Unit) {
        viewEventObservable
                .subscribe { fireEvent(it) }
                .disposeOnDetach()
        observeViewState.invoke(onViewState)
        resultDeferer.startEmission()
        fireResult(ToRenderContent)
    }

    protected fun coordinateDataStreamAndStartEventEmission() {
        coordinateDataStreams()
        startEventEmission()
    }

    protected fun coordinateDataStreams() {
        onEvent
                .observeOn(schedulers.main)
                .deferUntilEventEmissionReady()
                .doOnNext { Timber.e("${this.javaClass.simpleName}: onEvent: ${it.javaClass.simpleName}") }
                .compose(eventComposer.getComposer())
                .subscribe { onAction.accept(it) }
                .disposeOnDestroy()

        onAction
                .observeOn(schedulers.ios)
                .doOnNext { Timber.e("${this.javaClass.simpleName}: onAction: ${it::class.java.simpleName} ${if (it is JustResult) it.result.javaClass.simpleName else ""}") }
                .doOnNext { CrashReporter.log("Perform action: \"${this.javaClass.simpleName}: onAction: ${it::class.java.simpleName} ${if (it is JustResult) it.result.javaClass.simpleName else ""}\"") }
                .compose(actionComposer.getComposer())
                .observeOn(schedulers.main)
                .subscribe { onResult.accept(it) }
                .disposeOnDestroy()

        onResult
                .deferUntilViewAttached()
                .doOnNext {
                    if (it is ErrorResult) {
                        Timber.e("${this.javaClass.simpleName}: get result error: ${it.javaClass.name} , error = ${it.error}")
                        CrashReporter.log("${this.javaClass.simpleName}: get result error: ${it.javaClass.name} , error = ${it.error}")
                    } else {
                        Timber.e("${this.javaClass.simpleName}: get result: ${it.javaClass.name} ")
                        CrashReporter.log("${this.javaClass.simpleName}: get result: ${it.javaClass.name} ")
                    }
                }
                .compose(resultComposer.getComposer())
                .subscribe { onViewState.accept(it as M) }
                .disposeOnDestroy()
    }

    protected fun fireEvent(event: EV) {
        onEvent.accept(event)
    }

    protected fun fireResult(result: Result) {
        onResult.accept(result)
    }

    protected fun getViewStateObservable(): Observable<M> =
            onViewState

    protected fun getResultObservable(): Observable<Result> =
            onResult

    protected fun getActionObservable(): Observable<Action> =
            onAction

    @CallSuper open fun unbindViewController() {
        Timber.e("${this.javaClass.simpleName}: DetachView")
        resultDeferer.stopEmission()
        onDetachDisposable.clear()
    }

    @CallSuper open fun destroy() {
        Timber.e("${this.javaClass.simpleName}: Destroy")
        onDetachDisposable.clear()
        onDestroyDisposable.clear()
    }

    protected fun startEventEmission() {
        eventDeferer.startEmission()
    }

    private fun Disposable.disposeOnDetach() {
        onDetachDisposable.add(this)
    }

    protected fun Disposable.disposeOnDestroy() {
        onDestroyDisposable.add(this)
    }

    private fun Observable<Result>.deferUntilViewAttached(): Observable<Result> =
            compose(resultDeferer.deferUntilStart())

    private fun Observable<EV>.deferUntilEventEmissionReady(): Observable<EV> =
            compose(eventDeferer.deferUntilStart())

    protected fun Observable<out Action>.acceptActionUntilDestroy() {
        subscribe { onAction.accept(it) }
                .disposeOnDestroy()
    }

    protected fun<T: Event> observeForEvent(clazz: Class<T>): Observable<T> = onEvent.ofType(clazz)
    protected fun<T: Result> observeForResult(clazz: Class<T>): Observable<T> = onResult.ofType(clazz)
}