package com.mangareader.core.repository;

import com.mangareader.core.model.Chapter;
import java.util.List;
import java.util.Optional;

public interface ChapterRepository {
    Chapter save(Chapter chapter);
    Optional<Chapter> findById(int id);
    Optional<Chapter> findByFilePath(String filePath);
    List<Chapter> findByMangaId(int mangaId);
    void updatePageCount(int chapterId, int pageCount);
    void deleteByMangaId(int mangaId);
}
