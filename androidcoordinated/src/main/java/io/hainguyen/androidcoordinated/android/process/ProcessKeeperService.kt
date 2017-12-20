package org.de_studio.diary.android.process

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.annotation.Nullable
import timber.log.Timber
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 * Created by HaiNguyen on 8/30/17.
 */
class ProcessKeeperService : Service() {

    override fun onRebind(intent: Intent?) {
        Timber.e("onRebind")
        super.onRebind(intent)
    }

    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        Timber.e("dump")
        super.dump(fd, writer, args)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.e("onStartCommand")
        intent?.apply {
            val notification = intent.getParcelableExtra<Notification>(NOTIFICATION)
            Timber.e("onStartCommand show notification: id = ${intent.getIntExtra(NOTIFICATION_ID, 0)}")
            startForeground(intent.getIntExtra(NOTIFICATION_ID, 0),notification)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        Timber.e("onCreate")
        super.onCreate()
    }

    override fun onLowMemory() {
        Timber.e("onLowMemory")
        super.onLowMemory()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Timber.e("onTaskRemoved")
        super.onTaskRemoved(rootIntent)
    }

    override fun onTrimMemory(level: Int) {
        Timber.e("onTrimMemory")
        super.onTrimMemory(level)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.e("onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Timber.e("destroy")
        stopForeground(true)
        super.onDestroy()
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        Timber.e("onBind")
        return null
    }

    companion object {
        const val NOTIFICATION = "notification"
        const val NOTIFICATION_ID = "notificationId"
    }


}