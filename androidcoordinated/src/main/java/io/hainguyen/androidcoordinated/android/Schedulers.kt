package org.de_studio.diary.android

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by HaiNguyen on 10/28/17.
 */
interface Schedulers {
    val ios: Scheduler
    val main: Scheduler
    val sync: Scheduler
    val computation: Scheduler
}

class MySchedulers : Schedulers {
    override val ios: Scheduler = io.reactivex.schedulers.Schedulers.io()
    override val main: Scheduler = AndroidSchedulers.mainThread()
    override val sync: Scheduler = io.reactivex.schedulers.Schedulers.single()
    override val computation: Scheduler by lazy { io.reactivex.schedulers.Schedulers.computation() }
}