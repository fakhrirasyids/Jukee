package com.project.jukee.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.project.jukee.R;
import com.project.jukee.data.models.Songs;
import com.project.jukee.databinding.ItemSongsPlaylistRowBinding;
import com.project.jukee.databinding.ItemSongsRowBinding;
import com.project.jukee.ui.callbacks.OnDeleteClickedCallback;
import com.project.jukee.ui.callbacks.OnItemClickedCallback;

import java.util.ArrayList;

public class SongsPlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ViewHolder viewHolder;
    private OnItemClickedCallback onItemClickedCallback;
    private OnDeleteClickedCallback onDeleteClickedCallback;

    public ArrayList<Songs> songList = new ArrayList<>();

    public SongsPlaylistAdapter() {
    }

    public void setOnItemClickedCallback(OnItemClickedCallback onItemClickedCallback) {
        this.onItemClickedCallback = onItemClickedCallback;
    }

    public void setOnDeleteClickedCallback(OnDeleteClickedCallback onDeleteClickedCallback) {
        this.onDeleteClickedCallback = onDeleteClickedCallback;
    }

    public void setSongList(ArrayList<Songs> listSongs) {
        this.songList.clear();
        this.songList.addAll(listSongs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        viewHolder = new ViewHolder(ItemSongsPlaylistRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        viewHolder.setItem(songList.get(position));
        viewHolder.itemView.setOnClickListener(view -> onItemClickedCallback.onSongClicked(songList.get(position)));
        viewHolder.binding.btnDeleteSong.setOnClickListener(view -> onDeleteClickedCallback.onDeleteClicked(songList.get(position).getSongPlaylistIndex()));
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
        ItemSongsPlaylistRowBinding binding;

        public ViewHolder(@NonNull ItemSongsPlaylistRowBinding b) {
            super(b.getRoot());
            binding = b;
        }

        public void setItem(Songs songs) {
            Glide.with(binding.getRoot())
                    .asBitmap()
                    .placeholder(R.drawable.jukee_logo)
                    .load(songs.getImage())
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .into(binding.ivSong);

            binding.tvSongTitle.setText(songs.getTitle());
            binding.tvSongArtist.setText(songs.getArtist());
        }
    }
}
