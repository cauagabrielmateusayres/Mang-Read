package com.mangareader.core.service;

import com.mangareader.core.model.Chapter;
import com.mangareader.core.model.ReadingProgress;
import com.mangareader.core.repository.ChapterRepository;
import com.mangareader.core.repository.ProgressRepository;

import java.util.List;
import java.util.Optional;

public class ProgressService {

    private final ProgressRepository progressRepository;
    private final ChapterRepository chapterRepository;

    public ProgressService(ProgressRepository progressRepository,
                           ChapterRepository chapterRepository) {
        this.progressRepository = progressRepository;
        this.chapterRepository = chapterRepository;
    }

    /** Returns saved progress, or empty if the manga was never opened. */
    public Optional<ReadingProgress> getProgress(int userId, int mangaId) {
        return progressRepository.findByUserAndManga(userId, mangaId);
    }

    /** Upserts progress (chapter + page) for a user/manga pair. */
    public ReadingProgress saveProgress(int userId, int mangaId, int chapterId, int page) {
        return progressRepository.upsert(new ReadingProgress(userId, mangaId, chapterId, page));
    }

    public Optional<Chapter> getChapterById(int chapterId) {
        return chapterRepository.findById(chapterId);
    }

    public List<Chapter> getChapters(int mangaId) {
        return chapterRepository.findByMangaId(mangaId);
    }

    /** Returns the first chapter of a manga (chapter_number ascending). */
    public Optional<Chapter> getFirstChapter(int mangaId) {
        List<Chapter> chapters = chapterRepository.findByMangaId(mangaId);
        return chapters.isEmpty() ? Optional.empty() : Optional.of(chapters.get(0));
    }

    /** Updates the page count of a chapter after it is first opened and rendered. */
    public void updatePageCount(int chapterId, int pageCount) {
        chapterRepository.updatePageCount(chapterId, pageCount);
    }
}
