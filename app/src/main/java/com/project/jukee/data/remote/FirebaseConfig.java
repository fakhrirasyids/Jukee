package com.project.jukee.data.remote;

import static com.project.jukee.utils.Constants.CHILD_HOST;
import static com.project.jukee.utils.Constants.CHILD_SONGS_LIST;
import static com.project.jukee.utils.Constants.CHILD_USER_LIST;
import static com.project.jukee.utils.Constants.CHILD_VISITOR;
import static com.project.jukee.utils.Constants.COL_EMAIL;
import static com.project.jukee.utils.Constants.COL_IS_CONNECTED_TO;
import static com.project.jukee.utils.Constants.COL_PLAYLIST;
import static com.project.jukee.utils.Constants.COL_USERNAME;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseConfig {
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public DatabaseReference getRootDatabaseReference() {
        return firebaseDatabase.getReference();
    }

    public DatabaseReference getUserListDatabaseReference() {
        return getRootDatabaseReference().child(CHILD_USER_LIST);
    }

    public DatabaseReference getSongListDatabaseReference() {
        return  getRootDatabaseReference().child(CHILD_SONGS_LIST);
    }

    public DatabaseReference getAccountReference(String key) {
        return getUserListDatabaseReference().child(key);
    }

    public DatabaseReference getPlaylistReference(String key) {
        return getAccountReference(key).child(COL_PLAYLIST);
    }

    public DatabaseReference getPlaylistItemReference(String key, String playlistItemName) {
        return getAccountReference(key).child(COL_PLAYLIST).child(playlistItemName);
    }

    public DatabaseReference getIsConnectedTo(String key) {
        return getAccountReference(key).child(COL_IS_CONNECTED_TO);
    }

    public DatabaseReference getUsernameReference(String key) {
        return getAccountReference(key).child(COL_USERNAME);
    }

    public DatabaseReference getEmailReference(String key) {
        return getAccountReference(key).child(COL_EMAIL);
    }

    public DatabaseReference getHostReference(String key) {
        return getAccountReference(key).child(CHILD_HOST);
    }

    public DatabaseReference getVisitorReference(String key) {
        return getAccountReference(key).child(CHILD_VISITOR);
    }
}
