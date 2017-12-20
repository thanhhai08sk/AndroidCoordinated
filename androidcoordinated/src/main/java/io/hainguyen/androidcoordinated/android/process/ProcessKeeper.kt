package org.de_studio.diary.android.process

import android.content.Context
import android.content.Intent
import org.de_studio.diary.android.crashReporter.CrashReporter
import org.de_studio.diary.utils.extensionFunction.getAppContext
import org.de_studio.diary.utils.extensionFunction.notify
import org.de_studio.diary.utils.extensionFunction.stopService
import timber.log.Timber

/**
 * Created by HaiNguyen on 8/30/17.
 */
class ProcessKeeper(private val appContext: Context) {
    private val tasks = hashMapOf<String, BackgroundTask>()
    fun register(task: BackgroundTask) {
        Timber.e("Register task: $task")
        CrashReporter.log("Register task: $task with tag: ${task.tag}")
        tasks.put(task.tag, task)
        sendInfoToService(task)
    }

    private fun sendInfoToService(task: BackgroundTask) {
        Intent(appContext, ProcessKeeperService::class.java)
                .apply {
                    putExtra(ProcessKeeperService.NOTIFICATION, task.onProgressNoti)
                    putExtra(ProcessKeeperService.NOTIFICATION_ID, task.tag.hashCode())
                }.run { appContext.startService(this) }
    }

    fun update(task: BackgroundTask) {
        tasks.remove(task.tag)
        tasks.put(task.tag, task)
        sendInfoToService(task)
    }

    fun completed(task: BackgroundTask) {
        Timber.e("task completed ${task.javaClass.simpleName}")
        if (tasks.keys.contains(task.tag)) tasks.remove(task.tag) else CrashReporter.logException(IllegalArgumentException("Task hasn't registered yet: $task with tag ${task.tag}, uid = ${getAppContext().userComponent.uid()}"))
        if (tasks.isEmpty()) {
            Timber.e("No more task, stop service")
            appContext.stopService(ProcessKeeperService::class.java)
        }
        task.completeNotification?.apply { this.notify(task.tag.hashCode() + 1) }
    }
}