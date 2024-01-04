package com.project.jukee.data.models;

import java.io.Serializable;

public class Songs implements Serializable {
    private String title;
    private String songLink;
    private String artist;
    private String image;
    private Integer songId;
    private Integer songPlaylistIndex;

    public Integer getSongPlaylistIndex() {
        return songPlaylistIndex;
    }

    public void setSongPlaylistIndex(Integer songPlaylistIndex) {
        this.songPlaylistIndex = songPlaylistIndex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSongLink() {
        return songLink;
    }

    public void setSongLink(String songLink) {
        this.songLink = songLink;
    }

    public String getArtist() {
        return artist;
    }

    public Integer getSongId() {
        return songId;
    }

    public void setSongId(Integer songId) {
        this.songId = songId;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
