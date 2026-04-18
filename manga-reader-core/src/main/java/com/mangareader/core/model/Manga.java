package com.mangareader.core.model;

import java.time.LocalDateTime;

public class Manga {
    private int id;
    private String title;
    private String folderPath;
    private String coverPath;
    private boolean favorite;
    private LocalDateTime addedAt;

    public Manga() {}

    public Manga(String title, String folderPath) {
        this.title = title;
        this.folderPath = folderPath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFolderPath() { return folderPath; }
    public void setFolderPath(String folderPath) { this.folderPath = folderPath; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    @Override
    public String toString() { return "Manga{id=" + id + ", title='" + title + "', favorite=" + favorite + "}"; }
}
