package com.mangareader.core.repository;

import com.mangareader.core.model.Manga;
import java.util.List;
import java.util.Optional;

public interface MangaRepository {
    Manga save(Manga manga);
    Optional<Manga> findById(int id);
    Optional<Manga> findByFolderPath(String folderPath);
    List<Manga> findAll();
    void delete(int id);
    void setFavorite(int id, boolean favorite);
}
