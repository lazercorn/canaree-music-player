package dev.olog.msc.floating.window.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import dev.olog.msc.floating.window.service.api.HoverMenu
import dev.olog.msc.floating.window.service.api.HoverView
import dev.olog.msc.floating.window.service.notification.FloatingWindowNotification
import javax.inject.Inject

class FloatingWindowService : BaseFloatingService() {

    @Inject lateinit var hoverMenu: CustomHoverMenu
    @Inject lateinit var notification : FloatingWindowNotification

    companion object {
        const val TAG = "FloatingWindowService"
        const val ACTION_STOP = "$TAG.ACTION_STOP"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent != null && intent.action != null){
            when (intent.action){
                ACTION_STOP -> {
                    stopSelf()
                    return Service.START_NOT_STICKY
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHoverMenuLaunched(intent: Intent, hoverView: HoverView) {
        hoverView.setMenu(createHoverMenu())
        hoverView.collapse()

        hoverView.addOnExpandAndCollapseListener(onExpansionListener)
        notification.startObserving()
        hoverMenu.startObserving()
    }

    override fun onHoverMenuExitingByUserRequest() {
        super.onHoverMenuExitingByUserRequest()
        hoverView.removeOnExpandAndCollapseListener(onExpansionListener)
    }



    private val onExpansionListener = object : HoverView.Listener {
        override fun onCollapsing() {
            hoverMenu.sections.forEach { it.tabView.setHidden(true) }
        }

        override fun onExpanding() {
            hoverMenu.sections.forEach { it.tabView.setExpanded() }
        }

        override fun onExpanded() {
        }

        override fun onClosing() {
        }

        override fun onClosed() {
        }

        override fun onCollapsed() {
        }
    }

    override fun getForegroundNotificationId(): Int = FloatingWindowNotification.NOTIFICATION_ID

    override fun getForegroundNotification(): Notification {
        return notification.buildNotification()
    }

    private fun createHoverMenu() : HoverMenu = hoverMenu

}