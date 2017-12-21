package io.hainguyen.androidcoordinated.coordinated

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction

/**
 * Created by HaiNguyen on 9/19/17.
 */
class EmissionDeferer<T: Any> {
    private val items = arrayListOf<T>()
    private val emissionToggleRL = BehaviorRelay.create<Boolean>()

    fun deferUntilStart(): ObservableTransformer<T, T> {
        return ObservableTransformer<T, T> { obs: Observable<T> ->
            Observable.merge(
                    obs.map { Emission(it) as EmissionEvent },
                    emissionToggleRL.map { if (it) Start else Stop }

            )
                    .map {
                        when (it) {
                            is Start -> items
                            is Stop -> arrayListOf()
                            is Emission -> {
                                items.add(it.item as T)
                                items
                            }
                        }
                    }
                    .withLatestFrom(emissionToggleRL,
                            BiFunction<ArrayList<T>, Boolean, Pair<ArrayList<T>, Boolean>> { items, togged ->
                                Pair(items, togged)
                            })
                    .filter { it.second }
                    .map { it.first }
                    .flatMap {
                        Observable.fromIterable(it)
                                .doOnComplete { items.clear() }
                    }
        }
    }

    fun startEmission() {
        emissionToggleRL.accept(true)
    }

    fun stopEmission() {
        emissionToggleRL.accept(false)
    }
}

sealed class EmissionEvent()
object Start: EmissionEvent()
object Stop: EmissionEvent()
class Emission(val item: Any): EmissionEvent()