package com.mangareader.core.repository;

import com.mangareader.core.model.ReadingProgress;
import java.util.Optional;

public interface ProgressRepository {
    /** Insert or update progress for a user+manga combination. */
    ReadingProgress upsert(ReadingProgress progress);
    Optional<ReadingProgress> findByUserAndManga(int userId, int mangaId);
}
