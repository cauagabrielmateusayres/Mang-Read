package com.mangareader.persistence;

import com.mangareader.core.model.Chapter;
import com.mangareader.core.repository.ChapterRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonChapterRepository implements ChapterRepository {
    private final JsonDatabase db;

    public JsonChapterRepository(JsonDatabase db) {
        this.db = db;
    }

    @Override
    public Chapter save(Chapter chapter) {
        if (chapter.getId() <= 0) {
            chapter.setId(db.getData().nextChapterId++);
            db.getData().chapters.add(chapter);
        } else {
            for (int i = 0; i < db.getData().chapters.size(); i++) {
                if (db.getData().chapters.get(i).getId() == chapter.getId()) {
                    db.getData().chapters.set(i, chapter);
                    break;
                }
            }
        }
        db.save();
        return chapter;
    }

    @Override
    public Optional<Chapter> findById(int id) {
        return db.getData().chapters.stream()
                .filter(c -> c.getId() == id)
                .findFirst();
    }

    @Override
    public Optional<Chapter> findByFilePath(String filePath) {
        return db.getData().chapters.stream()
                .filter(c -> c.getFilePath().equals(filePath))
                .findFirst();
    }

    @Override
    public List<Chapter> findByMangaId(int mangaId) {
        return db.getData().chapters.stream()
                .filter(c -> c.getMangaId() == mangaId)
                .collect(Collectors.toList());
    }

    @Override
    public void updatePageCount(int chapterId, int pageCount) {
        findById(chapterId).ifPresent(c -> {
            c.setPageCount(pageCount);
            db.save();
        });
    }

    @Override
    public void deleteByMangaId(int mangaId) {
        db.getData().chapters.removeIf(c -> c.getMangaId() == mangaId);
        db.save();
    }
}
