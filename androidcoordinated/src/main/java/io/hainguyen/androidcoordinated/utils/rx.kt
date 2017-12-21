package io.hainguyen.androidcoordinated.utils

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.internal.operators.completable.CompletableDefer
import org.de_studio.diary.base.architecture.ErrorResult
import org.de_studio.diary.base.architecture.Result
import org.de_studio.diary.base.architecture.SuccessResult

/**
 * Created by HaiNguyen on 9/7/17.
 */
inline fun <R: Any, T : Any> Observable<R>.publishMerge(crossinline func: Observable<R>.() -> Iterable<Observable<out T>>): Observable< T> {
    return publish { sharedObs ->
        Observable.merge(func.invoke(sharedObs))
    }
}

inline fun <R : Any, T : Any> Single<R>.publishMerge(crossinline func: Observable<R>.() -> Iterable<Observable<out T>>): Single<T> {
    return toObservable()
            .publishMerge (func)
            .firstOrError()
}

fun Completable.toSuccessOrError(result: Result, toError: Throwable.() -> ErrorResult): Observable<Result> {
    return toSingleDefault(result)
            .onErrorReturn { toError.invoke(it) }
            .toObservable()
}

fun Observable<Result>.useCaseCompleteOrError(): Completable =
        map { if (it is ErrorResult) throw it.error else it }
                .ignoreElements()

fun Observable<Result>.useCaseSingleResultOrError(): Single<SuccessResult> =
        map { if (it is ErrorResult) throw it.error else it }
                .filter { it is SuccessResult }
                .map { it as SuccessResult }
                .firstOrError()


fun Completable.andThenDefer(toCompletable: () -> Completable): Completable =
    andThen(
            CompletableDefer{ toCompletable.invoke()}
    )





