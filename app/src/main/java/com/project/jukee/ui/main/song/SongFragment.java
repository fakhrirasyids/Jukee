package com.project.jukee.ui.main.song;

import static com.project.jukee.utils.Constants.COL_ARTIST;
import static com.project.jukee.utils.Constants.COL_IMAGE;
import static com.project.jukee.utils.Constants.COL_SONG_LINK;
import static com.project.jukee.utils.Constants.COL_TITLE;
import static com.project.jukee.utils.Constants.EXTRA_INDEX_SONG;
import static com.project.jukee.utils.Constants.EXTRA_SONG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.project.jukee.data.models.Playlist;
import com.project.jukee.data.models.Songs;
import com.project.jukee.data.remote.FirebaseConfig;
import com.project.jukee.databinding.FragmentSongBinding;
import com.project.jukee.ui.adapters.SongsAdapter;
import com.project.jukee.ui.callbacks.OnItemClickedCallback;
import com.project.jukee.ui.songdetail.SongDetailActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class SongFragment extends Fragment {

    private final DatabaseReference firebaseReference = new FirebaseConfig().getSongListDatabaseReference();
    private FragmentSongBinding binding;

    private final ArrayList<Songs> songList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSongBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initializeSongs();

        return root;
    }

    private void initializeSongs() {
        showLoading(true);
        firebaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    songList.clear();

                    for (DataSnapshot songSnapshots : snapshot.getChildren()) {
                        Songs song = new Songs();

                        song.setTitle(Objects.requireNonNull(Objects.requireNonNull(songSnapshots.child(COL_TITLE).getValue()).toString()));
                        song.setArtist(Objects.requireNonNull(Objects.requireNonNull(songSnapshots.child(COL_ARTIST).getValue()).toString()));
                        song.setImage(Objects.requireNonNull(Objects.requireNonNull(songSnapshots.child(COL_IMAGE).getValue()).toString()));
                        song.setSongLink(Objects.requireNonNull(Objects.requireNonNull(songSnapshots.child(COL_SONG_LINK).getValue()).toString()));
                        song.setSongId(Integer.parseInt(Objects.requireNonNull(songSnapshots.getKey())));

                        songList.add(song);
                    }

                    SongsAdapter songsAdapter = new SongsAdapter();
                    songsAdapter.setSongList(songList);

                    songsAdapter.setOnItemClickedCallback(new OnItemClickedCallback() {
                        @Override
                        public void onSongClicked(Songs songs) {
                            Intent iSongDetail = new Intent(requireContext(), SongDetailActivity.class);
                            iSongDetail.putExtra(EXTRA_SONG, (Serializable) songs);
                            requireActivity().startActivity(iSongDetail);
                        }

                        @Override
                        public void onPlaylistClicked(Playlist playlist) {}
                    });

                    binding.rvSongs.setAdapter(songsAdapter);
                    binding.rvSongs.setLayoutManager(new LinearLayoutManager(requireContext()));

                    setSearchView();

                    showLoading(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);

                Log.e("SongFragment", "onCancelled: " + error.getMessage());
                showToast(error.getMessage());
            }
        });
    }

    private void showLoading(Boolean isLoading) {
        if (isLoading) {
            binding.progressbar.setVisibility(View.VISIBLE);
            binding.layoutContent.setVisibility(View.GONE);
        } else {
            binding.progressbar.setVisibility(View.GONE);
            binding.layoutContent.setVisibility(View.VISIBLE);
        }
    }

    private void setSearchView() {
        binding.svSongs.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                filter(query);
                return false;
            }
        });
    }


    private void filter(String query) {
        ArrayList<Songs> filteredList = new ArrayList<>();

        for (Songs item : songList) {
            if (item.getTitle().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
                filteredList.add(item);
            }
        }

        if (!filteredList.isEmpty()) {
            SongsAdapter songsAdapter = new SongsAdapter();
            songsAdapter.setSongList(filteredList);

            songsAdapter.setOnItemClickedCallback(new OnItemClickedCallback() {
                @Override
                public void onSongClicked(Songs songs) {
                    Intent iSongDetail = new Intent(requireContext(), SongDetailActivity.class);
                    iSongDetail.putExtra(EXTRA_SONG, (Serializable) songs);
                    requireActivity().startActivity(iSongDetail);
                }

                @Override
                public void onPlaylistClicked(Playlist playlist) {}
            });

            binding.rvSongs.setAdapter(songsAdapter);
            binding.rvSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}