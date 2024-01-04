package com.project.jukee.ui.songdetail;

import static com.project.jukee.utils.Constants.COL_IS_CONNECTED_TO;
import static com.project.jukee.utils.Constants.EXTRA_SONG;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.project.jukee.R;
import com.project.jukee.data.models.Songs;
import com.project.jukee.data.remote.FirebaseConfig;
import com.project.jukee.databinding.ActivitySongDetailBinding;
import com.project.jukee.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SongDetailActivity extends AppCompatActivity {
    private ActivitySongDetailBinding binding;

    private PreferenceManager preferenceManager;
    private DatabaseReference firebaseReference;

    private MediaPlayer mediaPlayer = null;
    boolean wasPlaying = false;

    private Handler handler = new Handler();

    private Songs songs = new Songs();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySongDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);
        firebaseReference = new FirebaseConfig().getPlaylistReference(preferenceManager.getUserKey());

//        if (preferenceManager.isLoggedAsVisitor()) {
//            binding.btnAddPlaylist.setVisibility(View.GONE);
//        }

        songs = (Songs) getIntent().getSerializableExtra(EXTRA_SONG);

        setData();
        setListeners();
        setupSeekbar();
    }

    private void setData() {
        Glide.with(binding.getRoot())
                .asBitmap()
                .placeholder(R.drawable.jukee_logo)
                .load(songs.getImage())
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(binding.ivSongs);

        binding.tvSongTitle.setText(songs.getTitle());
        binding.tvSongArtist.setText(songs.getArtist());

    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                clearMediaPlayer();
            }
            finish();
        });

        binding.btnPlayStop.setOnClickListener(v -> playSong());
        binding.btnAddPlaylist.setOnClickListener(v -> addPlaylistHandler());
    }

    private void playSong() {
        if (!wasPlaying) {
            try {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(songs.getSongLink());
                    mediaPlayer.prepare();

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            binding.btnPlayStop.setImageDrawable(ContextCompat.getDrawable(SongDetailActivity.this, android.R.drawable.ic_media_play));
                            binding.tvDynamicDuration.setText("00:00");
                            handler.removeCallbacks(null);
                            wasPlaying = false;
                            if (mediaPlayer != null) {
                                clearMediaPlayer();
                            }
                        }
                    });
                }

                mediaPlayer.start();
                wasPlaying = true;
                binding.btnPlayStop.setImageDrawable(ContextCompat.getDrawable(SongDetailActivity.this, android.R.drawable.ic_media_pause));

                initSeekbar();

            } catch (Exception e) {
                e.printStackTrace();
                showToast(e.getMessage());
            }
        } else {
            mediaPlayer.pause();
            wasPlaying = false;
            binding.btnPlayStop.setImageDrawable(ContextCompat.getDrawable(SongDetailActivity.this, android.R.drawable.ic_media_play));
        }
    }

    private void initSeekbar() {
        binding.seekbar.setMax(mediaPlayer.getDuration());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    binding.seekbar.setProgress(mediaPlayer.getCurrentPosition());
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    updateDuration(currentPosition);

                    handler.postDelayed(this::run, 100);
                } catch (Exception e) {
                    binding.seekbar.setProgress(0);
                }
            }
        }, 0);
    }

    private void addPlaylistHandler() {
        if (preferenceManager.isLoggedAsVisitor()) {
            DatabaseReference hostReference = new FirebaseConfig().getAccountReference(preferenceManager.getUserKey());
            hostReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(COL_IS_CONNECTED_TO).getValue() != null) {
                        if (Objects.requireNonNull(snapshot.child(COL_IS_CONNECTED_TO).getValue()).toString().isEmpty()) {
                            showToast("Please Connect to a Host first!");
                        } else {
                            DatabaseReference hostPlaylistReference =  new FirebaseConfig().getPlaylistReference(Objects.requireNonNull(snapshot.child(COL_IS_CONNECTED_TO).getValue()).toString());
                            hostPlaylistReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        Boolean flag = false;
                                        ArrayList<Integer> songList = new ArrayList<>();

                                        for (DataSnapshot snap : snapshot.getChildren()) {
                                            if (Integer.parseInt(Objects.requireNonNull(snap.getValue()).toString()) == songs.getSongId()) {
                                                flag = true;
                                                break;
                                            }
                                            songList.add(Integer.parseInt(Objects.requireNonNull(snap.getValue()).toString()));
                                        }

                                        if (flag) {
                                            showToast("Song was added in this Host's playlist before!");
                                            songList.clear();
                                        } else {
                                            Map<String, Integer> songUpdates = new HashMap<>();
                                            Integer idx = 0;

                                            for (Integer song : songList) {
                                                songUpdates.put(idx.toString(), song);
                                                idx++;
                                            }

                                            songUpdates.put(idx.toString(), songs.getSongId());

                                            firebaseReference.setValue(songUpdates).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    showToast(e.getMessage());
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    showToast("Successfully added to Host's playlist!");
                                                }
                                            });
                                        }
                                    } else {
                                        addEmptyPlaylist();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    showToast(error.getMessage());
                                }
                            });
                        }
                    } else {
                        addEmptyHostConnector();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast(error.getMessage());
                }
            });

        } else {
            firebaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Boolean flag = false;
                        ArrayList<Integer> songList = new ArrayList<>();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            if (Integer.parseInt(Objects.requireNonNull(snap.getValue()).toString()) == songs.getSongId()) {
                                flag = true;
                                break;
                            }
                            songList.add(Integer.parseInt(Objects.requireNonNull(snap.getValue()).toString()));
                        }

                        if (flag) {
                            showToast("Song was added in this playlist before!");
                            songList.clear();
                        } else {
                            Map<String, Integer> songUpdates = new HashMap<>();
                            Integer idx = 0;

                            for (Integer song : songList) {
                                songUpdates.put(idx.toString(), song);
                                idx++;
                            }

                            songUpdates.put(idx.toString(), songs.getSongId());

                            firebaseReference.setValue(songUpdates).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showToast(e.getMessage());
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    showToast("Successfully added to playlist!");
                                }
                            });
                        }
                    } else {
                        addEmptyPlaylist();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast(error.getMessage());
                }
            });
        }
    }

    private void addEmptyPlaylist() {
        firebaseReference.setValue("");
        addPlaylistHandler();
    }

    private void addEmptyHostConnector() {
        DatabaseReference firebaseReference = new FirebaseConfig().getIsConnectedTo(preferenceManager.getUserKey());
        firebaseReference.setValue("");
        addPlaylistHandler();
    }

    private void setupSeekbar() {
        binding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    mediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void clearMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void updateDuration(int currentPosition) {
        int minutes = (currentPosition / 1000) / 60;
        int seconds = (currentPosition / 1000) % 60;

        String durationText = String.format("%02d:%02d", minutes, seconds);

        binding.tvDynamicDuration.setText(durationText);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            clearMediaPlayer();
        }
        super.onDestroy();
    }
}