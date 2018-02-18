package dev.olog.msc.presentation.library.tab.span.size.lookup

import dev.olog.msc.R
import dev.olog.msc.presentation.base.adapter.BaseListAdapter
import dev.olog.msc.presentation.model.DisplayableItem

class ArtistSpanSizeLookup(
        private val isPortrait: Boolean,
        private val adapter: BaseListAdapter<DisplayableItem>

) : AbsSpanSizeLookup() {

    override fun getSpanSize(position: Int): Int {
        val itemType = adapter.getItemAt(position).type
        if ((itemType == R.layout.item_tab_header ||
                        itemType == R.layout.item_tab_last_played_artist_horizontal_list ||
                        itemType == R.layout.item_tab_last_played_album_horizontal_list)){
            return spanCount
        }

        if (isPortrait){
            return spanCount / 3
        }

        return spanCount / 4
    }
}