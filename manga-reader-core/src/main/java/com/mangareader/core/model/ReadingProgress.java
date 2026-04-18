package com.mangareader.core.model;

import java.time.LocalDateTime;

public class ReadingProgress {
    private int id;
    private int userId;
    private int mangaId;
    private int chapterId;
    private int page;
    private LocalDateTime updatedAt;

    public ReadingProgress() {}

    public ReadingProgress(int userId, int mangaId, int chapterId, int page) {
        this.userId = userId;
        this.mangaId = mangaId;
        this.chapterId = chapterId;
        this.page = page;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getMangaId() { return mangaId; }
    public void setMangaId(int mangaId) { this.mangaId = mangaId; }

    public int getChapterId() { return chapterId; }
    public void setChapterId(int chapterId) { this.chapterId = chapterId; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
