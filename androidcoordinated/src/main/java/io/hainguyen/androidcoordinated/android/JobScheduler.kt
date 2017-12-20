package org.de_studio.diary.android

import com.firebase.jobdispatcher.*
import org.de_studio.diary.MyApplication
import org.de_studio.diary.database.PhotoUploadJobService
import timber.log.Timber

/**
 * Created by HaiNguyen on 10/27/17.
 */
object JobScheduler {

    fun scheduleUploadPhoto() {
        Timber.e("scheduleUploadPhoto ")
        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(MyApplication.getContext()))
        val myJob = dispatcher.newJobBuilder()
                .setService(PhotoUploadJobService::class.java)
                .setTag("UploadPhotos")
                .setRecurring(false)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(0, 60))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setConstraints(
                        Constraint.ON_UNMETERED_NETWORK
                )
                .build()
        dispatcher.mustSchedule(myJob)
    }
}