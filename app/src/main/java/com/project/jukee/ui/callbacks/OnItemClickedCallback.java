package com.project.jukee.ui.callbacks;


import com.project.jukee.data.models.Playlist;
import com.project.jukee.data.models.Songs;

public interface OnItemClickedCallback {
    public void onSongClicked(Songs songs);
    public void onPlaylistClicked(Playlist playlist);
}
