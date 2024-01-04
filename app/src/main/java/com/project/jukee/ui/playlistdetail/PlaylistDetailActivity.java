package com.project.jukee.ui.playlistdetail;

import static com.project.jukee.utils.Constants.COL_ARTIST;
import static com.project.jukee.utils.Constants.COL_IMAGE;
import static com.project.jukee.utils.Constants.COL_SONG_LINK;
import static com.project.jukee.utils.Constants.COL_TITLE;
import static com.project.jukee.utils.Constants.EXTRA_PLAYLIST_KEY;
import static com.project.jukee.utils.Constants.EXTRA_SONG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.project.jukee.data.models.Playlist;
import com.project.jukee.data.models.Songs;
import com.project.jukee.data.remote.FirebaseConfig;
import com.project.jukee.databinding.ActivityPlaylistDetailBinding;
import com.project.jukee.ui.adapters.SongsAdapter;
import com.project.jukee.ui.callbacks.OnItemClickedCallback;
import com.project.jukee.ui.songdetail.SongDetailActivity;
import com.project.jukee.utils.PreferenceManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class PlaylistDetailActivity extends AppCompatActivity {
    private ActivityPlaylistDetailBinding binding;

    private String userKey;
    private String playlistName;
    private DatabaseReference firebaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaylistDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userKey = new PreferenceManager(this).getUserKey();
        playlistName = getIntent().getStringExtra(EXTRA_PLAYLIST_KEY);
        firebaseReference = new FirebaseConfig().getPlaylistItemReference(userKey, playlistName);

        initializeData();
        setListeners();
    }

    private void initializeData() {
        showLoading(true);

        firebaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    binding.tvTitle.setText(playlistName);
                    Boolean flag = false;
                    ArrayList<Integer> songIndexList = new ArrayList<>();

                    for (DataSnapshot snap : snapshot.getChildren()) {
                        songIndexList.add(Integer.parseInt(Objects.requireNonNull(snap.getValue()).toString()));
                    }

                    DatabaseReference songReference = new FirebaseConfig().getSongListDatabaseReference();

                    songReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ArrayList<Songs> songList = new ArrayList<>();

                            for (Integer savedPlaylistIndex: songIndexList) {
                                for (DataSnapshot songSnapshots : snapshot.getChildren()) {
                                    if (Integer.parseInt(Objects.requireNonNull(songSnapshots.getKey())) == savedPlaylistIndex) {
                                        Songs song = new Songs();

                                        song.setTitle(Objects.requireNonNull(Objects.requireNonNull(songSnapshots.child(COL_TITLE).getValue()).toString()));
                                        song.setArtist(Objects.requireNonNull(Objects.requireNonNull(songSnapshots.child(COL_ARTIST).getValue()).toString()));
                                        song.setImage(Objects.requireNonNull(Objects.requireNonNull(songSnapshots.child(COL_IMAGE).getValue()).toString()));
                                        song.setSongLink(Objects.requireNonNull(Objects.requireNonNull(songSnapshots.child(COL_SONG_LINK).getValue()).toString()));
                                        song.setSongId(Integer.parseInt(Objects.requireNonNull(songSnapshots.getKey())));

                                        songList.add(song);
                                    }
                                }
                            }

                            SongsAdapter songsAdapter = new SongsAdapter();
                            songsAdapter.setSongList(songList);

                            songsAdapter.setOnItemClickedCallback(new OnItemClickedCallback() {
                                @Override
                                public void onSongClicked(Songs songs) {
                                    Intent iSongDetail = new Intent(PlaylistDetailActivity.this, SongDetailActivity.class);
                                    iSongDetail.putExtra(EXTRA_SONG, (Serializable) songs);
                                    startActivity(iSongDetail);
                                }

                                @Override
                                public void onPlaylistClicked(Playlist playlist) {
                                }
                            });

                            binding.rvSongs.setAdapter(songsAdapter);
                            binding.rvSongs.setLayoutManager(new LinearLayoutManager(PlaylistDetailActivity.this));

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

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnDeletePlaylist.setOnClickListener(v -> {
            firebaseReference.removeValue().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    Toast.makeText(PlaylistDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    finish();
                    Toast.makeText(PlaylistDetailActivity.this, "Successfully delete Playlist!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showLoading(Boolean isLoading) {
        if (isLoading) {
            binding.progressbar.setVisibility(View.VISIBLE);
            binding.rvSongs.setVisibility(View.GONE);
            binding.tvError.setVisibility(View.GONE);
        } else {
            binding.progressbar.setVisibility(View.GONE);
            binding.rvSongs.setVisibility(View.VISIBLE);
            binding.tvError.setVisibility(View.GONE);
        }
    }

    private void showError(Boolean isError) {
        if (isError) {
            binding.tvError.setVisibility(View.VISIBLE);
        } else {
            binding.tvError.setVisibility(View.GONE);
        }
    }
}