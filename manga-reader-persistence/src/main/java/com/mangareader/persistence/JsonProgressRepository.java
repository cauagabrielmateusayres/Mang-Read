package com.mangareader.persistence;

import com.mangareader.core.model.ReadingProgress;
import com.mangareader.core.repository.ProgressRepository;

import java.util.Optional;

public class JsonProgressRepository implements ProgressRepository {
    private final JsonDatabase db;

    public JsonProgressRepository(JsonDatabase db) {
        this.db = db;
    }

    @Override
    public ReadingProgress upsert(ReadingProgress progress) {
        Optional<ReadingProgress> existing = findByUserAndManga(progress.getUserId(), progress.getMangaId());
        if (existing.isPresent()) {
            ReadingProgress e = existing.get();
            e.setChapterId(progress.getChapterId());
            e.setPage(progress.getPage());
        } else {
            progress.setId(db.getData().nextProgressId++);
            db.getData().progresses.add(progress);
        }
        db.save();
        return progress;
    }

    @Override
    public Optional<ReadingProgress> findByUserAndManga(int userId, int mangaId) {
        return db.getData().progresses.stream()
                .filter(p -> p.getUserId() == userId && p.getMangaId() == mangaId)
                .findFirst();
    }
}
