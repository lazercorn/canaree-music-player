package dev.olog.presentation.widget_home_screen

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection
import dev.olog.shared_android.WidgetConstants

abstract class AbsWidgetApp : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        super.onReceive(context, intent)
        when (intent.action){
            WidgetConstants.METADATA_CHANGED -> {

                val appWidgetIds = intent.extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()){

                    val id = intent.getLongExtra(WidgetConstants.ARGUMENT_SONG_ID, 0)
                    val title = intent.getStringExtra(WidgetConstants.ARGUMENT_TITLE)
                    val subtitle = intent.getStringExtra(WidgetConstants.ARGUMENT_SUBTITLE)
                    val image = intent.getStringExtra(WidgetConstants.ARGUMENT_IMAGE)
                    val duration = intent.getLongExtra(WidgetConstants.ARGUMENT_DURATION, 0)
                    val metadata = WidgetMetadata(id, title, subtitle, image, duration)
                    onMetadataChanged(context, metadata, appWidgetIds)
                }
            }
            WidgetConstants.STATE_CHANGED -> {
                val appWidgetIds = intent.extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()){
                    val isPlaying = intent.getBooleanExtra(WidgetConstants.ARGUMENT_IS_PLAYING, false)
                    val bookmark = intent.getLongExtra(WidgetConstants.ARGUMENT_BOOKMARK, 0)
                    val state = WidgetState(isPlaying, bookmark)
                    onPlaybackStateChanged(context, state, appWidgetIds)
                }
            }
            WidgetConstants.ACTION_CHANGED -> {
                val appWidgetIds = intent.extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()){
                    val showPrevious = intent.getBooleanExtra(WidgetConstants.ARGUMENT_SHOW_PREVIOUS, true)
                    val showNext = intent.getBooleanExtra(WidgetConstants.ARGUMENT_SHOW_NEXT, true)
                    onActionVisibilityChanged(context, showPrevious, showNext, appWidgetIds)
                }
            }
        }
    }

    protected abstract fun onActionVisibilityChanged(context: Context, showPrevious: Boolean,
                                          showNext: Boolean, appWidgetIds: IntArray)

    protected abstract fun onMetadataChanged(context: Context, metadata: WidgetMetadata, appWidgetIds: IntArray)

    protected abstract fun onPlaybackStateChanged(context: Context, state: WidgetState, appWidgetIds: IntArray)


}
