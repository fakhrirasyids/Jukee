package com.project.jukee.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.project.jukee.R;
import com.project.jukee.data.models.Playlist;
import com.project.jukee.data.models.Songs;
import com.project.jukee.databinding.ItemPlaylistRowBinding;
import com.project.jukee.databinding.ItemSongsRowBinding;
import com.project.jukee.ui.callbacks.OnItemClickedCallback;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ViewHolder viewHolder;
    private OnItemClickedCallback onItemClickedCallback;

    public ArrayList<Playlist> songList = new ArrayList<>();

    public PlaylistAdapter() {
    }

    public void setOnItemClickedCallback(OnItemClickedCallback onItemClickedCallback) {
        this.onItemClickedCallback = onItemClickedCallback;
    }

    public void setPlaylistList(ArrayList<Playlist> listSongs) {
        this.songList.clear();
        this.songList.addAll(listSongs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        viewHolder = new ViewHolder(ItemPlaylistRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        viewHolder.setItem(songList.get(position));
        viewHolder.itemView.setOnClickListener(view -> onItemClickedCallback.onPlaylistClicked(songList.get(position)));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemPlaylistRowBinding binding;

        public ViewHolder(@NonNull ItemPlaylistRowBinding b) {
            super(b.getRoot());
            binding = b;
        }

        public void setItem(Playlist songs) {
            binding.tvPlaylistTitle.setText(songs.getTitle());
        }
    }
}
