package dev.olog.floating_info

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import dev.olog.domain.interactor.floating_info.GetFloatingInfoRequestUseCase
import dev.olog.floating_info.di.ServiceLifecycle
import dev.olog.shared.unsubscribe
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class InfoNotification @Inject constructor(
        private val service: Service,
        @ServiceLifecycle lifecycle: Lifecycle,
        private val activityClass : ActivityClass,
        private val notificationManager: NotificationManager,
        getFloatingInfoRequestUseCase: GetFloatingInfoRequestUseCase

) : DefaultLifecycleObserver {

    companion object {
        const val NOTIFICATION_ID = 0xABC
    }

    private val builder = NotificationCompat.Builder(service, "helper")
    private var disposable : Disposable? = null

    private var notificationTitle = ""

    init {
        lifecycle.addObserver(this)
        disposable = getFloatingInfoRequestUseCase.execute()
                .subscribe({
                    notificationTitle = it
                    val notification = builder.setContentTitle(notificationTitle).build()
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }, Throwable::printStackTrace)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        disposable.unsubscribe()
    }

    fun buildNotification(): Notification {
        return builder
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.vd_hot)
                .setContentTitle(notificationTitle)
                .setColor(Color.BLACK)
                .setContentText("")
                .setContentIntent(createContentIntent())
                .addAction(0, "Stop", createStopPendingIntent())
                .build()
    }

    private fun createContentIntent() : PendingIntent {
        return PendingIntent.getActivity(service, 0,
                Intent(service, activityClass.get()),
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createStopPendingIntent() : PendingIntent{
        val intent = Intent(service, FloatingInfoService::class.java)
        intent.action = FloatingInfoService.ACTION_STOP

        return PendingIntent.getService(service, 0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

}