package com.project.jukee.data.models;

import java.io.Serializable;

public class Playlist implements Serializable {
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
