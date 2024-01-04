package com.project.jukee.ui.main.host.playlist;

import static com.project.jukee.utils.Constants.COL_ARTIST;
import static com.project.jukee.utils.Constants.COL_IMAGE;
import static com.project.jukee.utils.Constants.COL_SONG_LINK;
import static com.project.jukee.utils.Constants.COL_TITLE;
import static com.project.jukee.utils.Constants.EXTRA_SONG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.project.jukee.databinding.FragmentPlaylistBinding;
import com.project.jukee.ui.adapters.SongsPlaylistAdapter;
import com.project.jukee.ui.callbacks.OnDeleteClickedCallback;
import com.project.jukee.ui.callbacks.OnItemClickedCallback;
import com.project.jukee.ui.songdetail.SongDetailActivity;
import com.project.jukee.utils.PreferenceManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class PlaylistFragment extends Fragment {

    private DatabaseReference firebaseReference;
    private PreferenceManager preferenceManager;

    private Context context;
    private FragmentPlaylistBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        preferenceManager = new PreferenceManager(requireContext());
        firebaseReference = new FirebaseConfig().getPlaylistReference(preferenceManager.getUserKey());
        context = requireContext();
        initializeData();
        setListeners();

        return root;
    }

    private void initializeData() {
        showLoading(true);
        binding.tvHostId.setText("Host ID : " + preferenceManager.getUserKey());

        addPlaylistHandler();
    }

    private void addPlaylistHandler() {
        firebaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (!snapshot.getValue().toString().isEmpty()) {
                        ArrayList<Integer> songIndexList = new ArrayList<>();
                        ArrayList<Integer> songIndexPlaylistList = new ArrayList<>();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            songIndexList.add(Integer.parseInt(Objects.requireNonNull(snap.getValue()).toString()));
                            songIndexPlaylistList.add(Integer.parseInt(Objects.requireNonNull(snap.getKey())));
                        }

                        DatabaseReference songReference = new FirebaseConfig().getSongListDatabaseReference();

                        songReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ArrayList<Songs> songList = new ArrayList<>();

                                for (int i = 0; i < songIndexList.size(); i++) {
                                    for (DataSnapshot songSnapshots : snapshot.getChildren()) {
                                        if (Integer.parseInt(Objects.requireNonNull(songSnapshots.getKey())) == songIndexList.get(i)) {
                                            Songs song = new Songs();

                                            song.setTitle(Objects.requireNonNull(Objects.requireNonNull(songSnapshots.child(COL_TITLE).getValue()).toString()));
                                            song.setArtist(Objects.requireNonNull(Objects.requireNonNull(songSnapshots.child(COL_ARTIST).getValue()).toString()));
                                            song.setImage(Objects.requireNonNull(Objects.requireNonNull(songSnapshots.child(COL_IMAGE).getValue()).toString()));
                                            song.setSongLink(Objects.requireNonNull(Objects.requireNonNull(songSnapshots.child(COL_SONG_LINK).getValue()).toString()));
                                            song.setSongId(Integer.parseInt(Objects.requireNonNull(songSnapshots.getKey())));
                                            song.setSongPlaylistIndex(songIndexPlaylistList.get(i));

                                            songList.add(song);
                                        }
                                    }
                                }

                                SongsPlaylistAdapter songsPlaylistAdapter = new SongsPlaylistAdapter();
                                songsPlaylistAdapter.setSongList(songList);

                                songsPlaylistAdapter.setOnItemClickedCallback(new OnItemClickedCallback() {
                                    @Override
                                    public void onSongClicked(Songs songs) {
                                        Intent iSongDetail = new Intent(requireContext(), SongDetailActivity.class);
                                        iSongDetail.putExtra(EXTRA_SONG, (Serializable) songs);
                                        startActivity(iSongDetail);
                                    }

                                    @Override
                                    public void onPlaylistClicked(Playlist playlist) {
                                    }
                                });

                                songsPlaylistAdapter.setOnDeleteClickedCallback(new OnDeleteClickedCallback() {
                                    @Override
                                    public void onDeleteClicked(Integer songIndex) {
                                        firebaseReference.child(songIndex.toString()).removeValue();
                                    }
                                });

                                binding.rvSongs.setAdapter(songsPlaylistAdapter);
                                binding.rvSongs.setLayoutManager(new LinearLayoutManager(context));

                                showError(false);
                                showLoading(false);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                binding.tvError.setText(error.getMessage());
                                showError(true);
                                showLoading(false);
                            }
                        });
                    } else {
                        binding.rvSongs.setAdapter(null);
                        binding.rvSongs.setLayoutManager(null);

                        showError(true);
                        showLoading(false);
                    }
                } else {
                    addEmptyPlaylist();
                    showError(true);
                    showLoading(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.tvError.setText(error.getMessage());
                showError(true);
                showLoading(false);
            }
        });
    }

    private void addEmptyPlaylist() {
        DatabaseReference firebaseReference = new FirebaseConfig().getPlaylistReference(preferenceManager.getUserKey());
        firebaseReference.setValue("");
    }

    private void setListeners() {
        binding.btnCopyId.setOnClickListener(v -> copyHostId());
    }

    private void copyHostId() {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("HostID", preferenceManager.getUserKey());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(requireContext(), "Successfully copied ID to clipboard!", Toast.LENGTH_SHORT).show();
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

    private void showError(Boolean isError) {
        if (isError) {
            binding.tvError.setVisibility(View.VISIBLE);
        } else {
            binding.tvError.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        firebaseReference = null;
    }
}