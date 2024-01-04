package com.project.jukee.utils;

import static android.content.Context.MODE_PRIVATE;

import static com.project.jukee.utils.Constants.COL_USERNAME;
import static com.project.jukee.utils.Constants.COL_USER_KEY;
import static com.project.jukee.utils.Constants.KEY_LOGGED_HOST;
import static com.project.jukee.utils.Constants.KEY_LOGGED_NORMAL_USER;
import static com.project.jukee.utils.Constants.KEY_LOGGED_VISITOR;
import static com.project.jukee.utils.Constants.SHARED_PREFS;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PreferenceManager {
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setLoggedIn(String actor, Boolean isLoggedIn) {
        switch (actor) {
            case KEY_LOGGED_NORMAL_USER -> {
                editor.putBoolean(KEY_LOGGED_NORMAL_USER, isLoggedIn);
                editor.commit();
            }
            case KEY_LOGGED_HOST -> {
                editor.putBoolean(KEY_LOGGED_HOST, isLoggedIn);
                editor.commit();
            }
            case KEY_LOGGED_VISITOR -> {
                editor.putBoolean(KEY_LOGGED_VISITOR, isLoggedIn);
                editor.commit();
            }
        }
    }

    public Boolean isLoggedAsNormalUser() {
        return sharedPreferences.getBoolean(KEY_LOGGED_NORMAL_USER, false);
    }

    public Boolean isLoggedAsHost() {
        return sharedPreferences.getBoolean(KEY_LOGGED_HOST, false);
    }

    public Boolean isLoggedAsVisitor() {
        return sharedPreferences.getBoolean(KEY_LOGGED_VISITOR, false);
    }

    public void setUsername(String username) {
        editor.putString(COL_USERNAME, username);
        editor.commit();
    }

    public String getUsername() {
        return sharedPreferences.getString(COL_USERNAME, "");
    }

    public void setUserKey(String userKey) {
        editor.putString(COL_USER_KEY, userKey);
        editor.commit();
    }

    public String getUserKey() {
        return sharedPreferences.getString(COL_USER_KEY, "");
    }

    public void clearPreferences() {
        editor.clear();
        editor.commit();
    }
}
