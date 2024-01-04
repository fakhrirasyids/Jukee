package com.project.jukee.ui.main.visitor.scan;

import static com.project.jukee.utils.Constants.COL_ARTIST;
import static com.project.jukee.utils.Constants.COL_IMAGE;
import static com.project.jukee.utils.Constants.COL_IS_CONNECTED_TO;
import static com.project.jukee.utils.Constants.COL_SONG_LINK;
import static com.project.jukee.utils.Constants.COL_TITLE;
import static com.project.jukee.utils.Constants.COL_USERNAME;
import static com.project.jukee.utils.Constants.EXTRA_SONG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.project.jukee.data.models.Playlist;
import com.project.jukee.data.models.Songs;
import com.project.jukee.data.remote.FirebaseConfig;
import com.project.jukee.databinding.FragmentScanBinding;
import com.project.jukee.ui.adapters.SongsAdapter;
import com.project.jukee.ui.adapters.SongsPlaylistAdapter;
import com.project.jukee.ui.callbacks.OnDeleteClickedCallback;
import com.project.jukee.ui.callbacks.OnItemClickedCallback;
import com.project.jukee.ui.songdetail.SongDetailActivity;
import com.project.jukee.utils.PreferenceManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;


public class ScanFragment extends Fragment {

    private FragmentScanBinding binding;
    private DatabaseReference firebaseReference;
    private PreferenceManager preferenceManager;

    private Context context;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScanBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        context = requireContext();

        preferenceManager = new PreferenceManager(context);
        firebaseReference = new FirebaseConfig().getAccountReference(preferenceManager.getUserKey());

        initializeHost();
        setListeners();

        return root;
    }

    private void initializeHost() {
        showLoading(true);
        hostIdHandler();
    }

    private void hostIdHandler() {
        firebaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(COL_IS_CONNECTED_TO).getValue() != null) {
                    if (Objects.requireNonNull(snapshot.child(COL_IS_CONNECTED_TO).getValue()).toString().isEmpty()) {
                        showLoading(false);
                        showConnectLayout(true);
                    } else {
                        DatabaseReference usernameReference = new FirebaseConfig().getUsernameReference(Objects.requireNonNull(snapshot.child(COL_IS_CONNECTED_TO).getValue()).toString());
                        usernameReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                binding.tvHostName.setText(Objects.requireNonNull(snapshot.getValue()).toString());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

                        DatabaseReference playlistReference = new FirebaseConfig().getPlaylistReference(Objects.requireNonNull(snapshot.child(COL_IS_CONNECTED_TO).getValue()).toString());
                        addPlaylistHandler(playlistReference);

                        showLoading(false);
                        showConnectLayout(false);
                    }
                } else {
                    addEmptyHostConnector();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addEmptyHostConnector() {
        DatabaseReference firebaseReference = new FirebaseConfig().getIsConnectedTo(preferenceManager.getUserKey());
        firebaseReference.setValue("");
        hostIdHandler();
    }

    private void addPlaylistHandler(DatabaseReference playlistReference) {
        playlistReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (!snapshot.getValue().toString().isEmpty()) {
                        ArrayList<Integer> songIndexList = new ArrayList<>();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            songIndexList.add(Integer.parseInt(Objects.requireNonNull(snap.getValue()).toString()));
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

                                            songList.add(song);
                                        }
                                    }
                                }

                                SongsAdapter songsAdapter = new SongsAdapter();
                                songsAdapter.setSongList(songList);

                                songsAdapter.setOnItemClickedCallback(new OnItemClickedCallback() {
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

                                binding.rvSongs.setAdapter(songsAdapter);
                                binding.rvSongs.setLayoutManager(new LinearLayoutManager(context));

                                showConnectLayout(false);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT);
                            }
                        });
                    } else {
                        binding.rvSongs.setAdapter(null);
                        binding.rvSongs.setLayoutManager(null);

                        showConnectLayout(false);
                        showError(true);
                        showLoading(false);
                    }
                } else {
                    addEmptyPlaylist(playlistReference);
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

    private void addEmptyPlaylist(DatabaseReference playlistReference) {
        playlistReference.setValue("");
        addPlaylistHandler(playlistReference);
    }

    private void setListeners() {
        binding.btnConnect.setOnClickListener(v -> {
            if (binding.edHostId.getText().toString().isEmpty()) {
                Toast.makeText(context, "Host ID can't be empty!", Toast.LENGTH_SHORT).show();
            } else {
                showLoading(true);
                firebaseReference.child(COL_IS_CONNECTED_TO).setValue(binding.edHostId.getText().toString()).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showLoading(false);
                        Toast.makeText(context, "Failed to connect Host!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Successfully connected to Host!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.btnDisconnect.setOnClickListener(v -> {
            firebaseReference.child(COL_IS_CONNECTED_TO).setValue("");
        });
    }

    private void showLoading(Boolean isLoading) {
        if (isLoading) {
            binding.progressbar.setVisibility(View.VISIBLE);
            binding.layoutContent.setVisibility(View.GONE);
        } else {
            binding.progressbar.setVisibility(View.GONE);
        }
    }

    private void showConnectLayout(Boolean isDisconnected) {
        if (isDisconnected) {
            binding.layoutHostConnect.setVisibility(View.VISIBLE);
            binding.layoutContent.setVisibility(View.GONE);
        } else {
            binding.layoutHostConnect.setVisibility(View.GONE);
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

}