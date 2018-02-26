package dev.olog.msc.presentation.player

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.databinding.ViewDataBinding
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.MotionEvent
import android.view.View
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.jakewharton.rxbinding2.view.RxView
import dev.olog.msc.BR
import dev.olog.msc.R
import dev.olog.msc.app.GlideApp
import dev.olog.msc.constants.AppConstants.PROGRESS_BAR_INTERVAL
import dev.olog.msc.constants.MusicConstants
import dev.olog.msc.dagger.qualifier.FragmentLifecycle
import dev.olog.msc.floating.window.service.FloatingWindowHelper
import dev.olog.msc.presentation.SeekBarObservable
import dev.olog.msc.presentation.base.adapter.AbsAdapter
import dev.olog.msc.presentation.base.adapter.DataBoundViewHolder
import dev.olog.msc.presentation.base.music.service.MediaProvider
import dev.olog.msc.presentation.model.DisplayableItem
import dev.olog.msc.presentation.navigator.Navigator
import dev.olog.msc.presentation.widget.SwipeableView
import dev.olog.msc.utils.MediaId
import dev.olog.msc.utils.TextUtils
import dev.olog.msc.utils.img.CoverUtils
import dev.olog.msc.utils.k.extension.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.fragment_player_controls.view.*
import kotlinx.android.synthetic.main.fragment_player_toolbar.view.*
import kotlinx.android.synthetic.main.layout_swipeable_view.view.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PlayerFragmentAdapter @Inject constructor(
        private val activity: Activity,
        @FragmentLifecycle lifecycle: Lifecycle,
        private val mediaProvider: MediaProvider,
        private val navigator: Navigator,
        private val viewModel: PlayerFragmentViewModel

): AbsAdapter<DisplayableItem>(lifecycle) {

    private var seekBarDisposable : Disposable? = null

    override fun initViewHolderListeners(viewHolder: DataBoundViewHolder, viewType: Int) {
        when (viewType){
            R.layout.item_mini_queue -> {
                viewHolder.setOnClickListener(controller) { item, _, _ ->
                    mediaProvider.skipToQueueItem(item.mediaId.leaf!!)
                }
                viewHolder.setOnLongClickListener(controller) { item, _, _ ->
                    navigator.toDialog(item, viewHolder.itemView)
                }
                viewHolder.setOnClickListener(R.id.more, controller) { item, _, view ->
                    navigator.toDialog(item, view)
                }
                viewHolder.elevateSongOnTouch()

                viewHolder.itemView.findViewById<View>(R.id.dragHandle)?.setOnTouchListener { _, event ->
                    if(event.actionMasked == MotionEvent.ACTION_DOWN) {
                        touchHelper?.startDrag(viewHolder)
                        true
                    } else false
                }
            }
        }
    }

    override fun onViewAttachedToWindow(holder: DataBoundViewHolder) {
        val viewType = holder.itemViewType
        when (viewType){
            R.layout.fragment_player_controls -> {
                bindPlayerControls(holder.itemView)
            }
        }
    }

    private fun bindPlayerControls(view: View){
        mediaProvider.onMetadataChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .takeUntil(RxView.detaches(view))
                .subscribe({
                    updateMetadata(view, it)
                    updateImage(view, it)
                }, Throwable::printStackTrace)

        mediaProvider.onMetadataChanged()
                .map { it.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) }
                .map { MediaId.fromString(it).leaf!! }
                .distinctUntilChanged()
                .flatMapSingle { viewModel.isFavoriteSongUseCase.execute(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .takeUntil(RxView.detaches(view))
                .subscribe({ updateFavorite(view, it) }, Throwable::printStackTrace)

        mediaProvider.onStateChanged()
                .takeUntil(RxView.detaches(view))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onPlaybackStateChanged(view, it) }, Throwable::printStackTrace)

        if (view.repeat != null){
            mediaProvider.onRepeatModeChanged()
                    .takeUntil(RxView.detaches(view))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(view.repeat::cycle, Throwable::printStackTrace)

            RxView.clicks(view.repeat)
                    .takeUntil(RxView.detaches(view))
                    .subscribe({ mediaProvider.toggleRepeatMode() }, Throwable::printStackTrace)
        }
        if (view.shuffle != null){
            mediaProvider.onShuffleModeChanged()
                    .takeUntil(RxView.detaches(view))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(view.shuffle::cycle, Throwable::printStackTrace)


            RxView.clicks(view.shuffle)
                    .takeUntil(RxView.detaches(view))
                    .subscribe({ mediaProvider.toggleShuffleMode() }, Throwable::printStackTrace)
        }

        RxView.clicks(view.floatingWindow)
                .takeUntil(RxView.detaches(view))
                .subscribe({
                    FloatingWindowHelper.startServiceOrRequestOverlayPermission(activity)
                }, Throwable::printStackTrace)

        RxView.clicks(view.favorite)
                .takeUntil(RxView.detaches(view))
                .subscribe({ mediaProvider.togglePlayerFavorite() }, Throwable::printStackTrace)

        RxView.clicks(view.playingQueue)
                .takeUntil(RxView.detaches(view))
                .subscribe({ navigator.toPlayingQueueFragment(view.playingQueue) }, Throwable::printStackTrace)

        val seekBarObservable = SeekBarObservable(view.seekBar)
                .share()

        seekBarObservable.ofType<Int>()
                .takeUntil(RxView.detaches(view))
                .map { TextUtils.getReadableSongLength(it) }
                .subscribe(view.bookmark::setText, Throwable::printStackTrace)

        seekBarObservable.ofType<Pair<SeekBarObservable.Notification, Int>>()
                .takeUntil(RxView.detaches(view))
                .filter { (notification, _) -> notification == SeekBarObservable.Notification.STOP }
                .map { (_, progress) -> progress.toLong() }
                .subscribe(mediaProvider::seekTo, Throwable::printStackTrace)

        view.swipeableView?.setOnSwipeListener(object : SwipeableView.SwipeListener{
            override fun onSwipedLeft() {
                mediaProvider.skipToNext()
            }

            override fun onSwipedRight() {
                mediaProvider.skipToPrevious()
            }

            override fun onClick() {
                mediaProvider.playPause()
            }

            override fun onLeftEdgeClick() {
                mediaProvider.skipToPrevious()
            }

            override fun onRightEdgeClick() {
                mediaProvider.skipToNext()
            }
        })

        viewModel.onFavoriteAnimateRequestObservable
                .takeUntil(RxView.detaches(view))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view.favorite::animateFavorite, Throwable::printStackTrace)
    }

    private fun updateMetadata(view: View, metadata: MediaMetadataCompat){
        view.title.text = metadata.getText(MediaMetadataCompat.METADATA_KEY_TITLE)
        view.artist.text = metadata.getText(MediaMetadataCompat.METADATA_KEY_ARTIST)

        val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
        val durationAsString = TextUtils.MIDDLE_DOT_SPACED + TextUtils.getReadableSongLength(duration)
        view.duration.text = durationAsString
        view.seekBar.max = duration.toInt()
        view.remix.toggleVisibility(metadata.getLong(MusicConstants.IS_REMIX) == 1L)
        view.explicit.toggleVisibility(metadata.getLong(MusicConstants.IS_EXPLICIT) == 1L)
    }

    private fun updateFavorite(view: View, isFavorite: Boolean){
        view.favorite.toggleFavorite(isFavorite)
    }

    private fun updateImage(view: View, metadata: MediaMetadataCompat){
        view.cover ?: return

        val context = view.context
        val image = Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))
        val id = MediaId.fromString(
                metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
        ).leaf!!.toInt()
        val placeholder = CoverUtils.getGradient(context, id)
        GlideApp.with(context!!).clear(view.cover)

        GlideApp.with(context)
                .load(image)
                .centerCrop()
                .placeholder(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.IMMEDIATE)
                .override(800)
                .into(view.cover)
    }

    private fun onPlaybackStateChanged(view: View, playbackState: PlaybackStateCompat){
        val state = playbackState.state
        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED){
            val isPlaying = state == PlaybackStateCompat.STATE_PLAYING
            view.nowPlaying.isActivated = isPlaying
            view.cover?.isActivated = isPlaying
            handleSeekBarState(view, isPlaying)
        }

        view.seekBar.progress = playbackState.position.toInt()
    }

    private fun handleSeekBarState(view: View, isPlaying: Boolean){
        seekBarDisposable.unsubscribe()

        if (isPlaying){
            seekBarDisposable = Observable.interval(PROGRESS_BAR_INTERVAL.toLong(), TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ view.seekBar.incrementProgressBy(PROGRESS_BAR_INTERVAL) }, Throwable::printStackTrace)
        }
    }

    override fun bind(binding: ViewDataBinding, item: DisplayableItem, position: Int) {
        binding.setVariable(BR.item, item)
    }

    override fun onMoved(from: Int, to: Int) {
        super.onMoved(from, to)
        notifyItemRangeChanged(0, controller.getSize())
    }

    override fun onSwiped(position: Int) {
        super.onSwiped(position)
        notifyItemRangeChanged(0, controller.getSize())
    }

    override val onDragAction = { from: Int, to: Int -> mediaProvider.swapRelative(from, to) }

    override val onSwipeAction = { position: Int -> mediaProvider.removeRelative(position) }

    override fun canInteractWithViewHolder(viewType: Int): Boolean? {
        return viewType == R.layout.item_mini_queue
    }
}