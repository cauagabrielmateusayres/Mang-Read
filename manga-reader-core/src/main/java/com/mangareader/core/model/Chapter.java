package com.mangareader.core.model;

public class Chapter {
    private int id;
    private int mangaId;
    private float chapterNumber;
    private String title;
    private String filePath;
    private int pageCount;

    public Chapter() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMangaId() { return mangaId; }
    public void setMangaId(int mangaId) { this.mangaId = mangaId; }

    public float getChapterNumber() { return chapterNumber; }
    public void setChapterNumber(float chapterNumber) { this.chapterNumber = chapterNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }

    @Override
    public String toString() {
        return "Chapter{id=" + id + ", number=" + chapterNumber + ", title='" + title + "'}";
    }
}
