package org.de_studio.diary.base.architecture

import io.reactivex.ObservableTransformer

/**
 * Created by HaiNguyen on 9/12/17.
 */
interface ResultComposer {
    fun getComposer(): ObservableTransformer<Result, out ViewState>
}

abstract class BaseResultComposer<S: ViewState>(protected val viewState: S) : ResultComposer {
    override fun getComposer(): ObservableTransformer<Result, out ViewState> {
        return ObservableTransformer <Result, S> { resultObservable ->
            resultObservable.scan(viewState ){ state, result ->
                state.reset()
                when (result) {
                    is ErrorResult -> state.handleError(result.error) as S
                    is ToRenderContent -> state.renderContent() as S
                    is ToFinishView -> state.finishView() as S
                    else -> updateState(result, state)
                }
            }
        }
    }

    abstract fun updateState(result: Result, state: S): S
}