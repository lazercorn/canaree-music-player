package dev.olog.msc.presentation.edit.artist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.olog.msc.domain.entity.Song
import dev.olog.msc.utils.k.extension.unsubscribe
import io.reactivex.disposables.Disposable
import org.jaudiotagger.tag.TagOptionSingleton
import javax.inject.Inject

class EditArtistFragmentViewModel @Inject constructor(
        private val presenter: EditArtistFragmentPresenter

) : ViewModel(){

    private val songList = MutableLiveData<List<Song>>()

    private val displayedArtist = MutableLiveData<DisplayableArtist>()

    private var songListDisposable: Disposable? = null
    private var artistDisposable: Disposable? = null

    init {
        TagOptionSingleton.getInstance().isAndroid = true

        artistDisposable = presenter.observeArtist()
                .subscribe({
                    this.displayedArtist.postValue(it)
                }, Throwable::printStackTrace)

        songListDisposable = presenter.getSongList()
                .subscribe({
                    songList.postValue(it)
                }, Throwable::printStackTrace)
    }

    fun updateImage(image: String?){
        val oldValue = displayedArtist.value!!
        val newValue = oldValue.copy(image = image)
        displayedArtist.postValue(newValue)
    }

    fun getNewImage(): String? {
        return displayedArtist.value!!.image

    }

    fun getArtist(): DisplayableArtist = presenter.getArtist()

    override fun onCleared() {
        songListDisposable.unsubscribe()
    }

    fun observeData(): LiveData<DisplayableArtist> = displayedArtist
    fun observeSongList(): LiveData<List<Song>> = songList



}