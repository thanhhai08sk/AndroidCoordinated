package org.de_studio.diary.base.architecture

import butterknife.ButterKnife
import butterknife.Unbinder
import com.jakewharton.rxrelay2.PublishRelay
import io.hainguyen.androidcoordinated.coordinated.EmissionDeferer
import io.hainguyen.androidcoordinated.coordinated.ViewsProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.lang.ref.WeakReference

/**
 * Created by HaiNguyen on 10/2/17.
 */
open class ViewControllerHelper<S: ViewState, E: Event>(
        private val viewControllerWeakRef: WeakReference<ViewController<S, E>>,
        private val coordinatorWeakRef: WeakReference<BaseCoordinator<S, E, *, *>>
) {
    private val eventRL = PublishRelay.create<E>()
    private val viewStateDisposable = CompositeDisposable()
    lateinit var unbinder: Unbinder
    private val eventDeferer = EmissionDeferer<E>()

    fun bindView(viewsProvider: ViewsProvider) {
        unbinder = ButterKnife.bind(viewControllerWeakRef.get()!!, viewsProvider.view)
        eventDeferer.startEmission()
        viewControllerWeakRef.get()!!.setupViews()
        coordinatorWeakRef
                .get()!!
                .bindViewController(
                        onViewEvent(),
                        { observeViewState(it) }
                )
        addToAutoDispose(
                Observable
                        .merge(viewControllerWeakRef.get()!!.mapViewEventsToObservables())
                        .subscribe { fireEvent(it) }
        )
    }

    private fun observeViewState(viewStateObservable: Observable<S>) {
        addToAutoDispose(
                viewStateObservable
                        .subscribe {
                            if (it.handleError) viewControllerWeakRef.get()!!.handleError(it.error!!)
                            viewControllerWeakRef.get()!!.render(it)
                        }
        )
    }

    private fun onViewEvent(): Observable<E> = eventRL.compose(eventDeferer.deferUntilStart())

    fun unbindView() {
        coordinatorWeakRef.get()!!.unbindViewController()
        unbinder.unbind()
        viewStateDisposable.clear()
        eventDeferer.stopEmission()
    }

    fun fireEvent(event: E) {
        eventRL.accept(event)
    }

    fun addToAutoDispose(vararg disposables: Disposable) {
        disposables.forEach { viewStateDisposable.add(it) }
    }
}