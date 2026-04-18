package com.mangareader.persistence;

import com.mangareader.core.model.Manga;
import com.mangareader.core.repository.MangaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonMangaRepository implements MangaRepository {
    private final JsonDatabase db;

    public JsonMangaRepository(JsonDatabase db) {
        this.db = db;
    }

    @Override
    public Manga save(Manga manga) {
        if (manga.getId() <= 0) {
            manga.setId(db.getData().nextMangaId++);
            db.getData().mangas.add(manga);
        } else {
            for (int i = 0; i < db.getData().mangas.size(); i++) {
                if (db.getData().mangas.get(i).getId() == manga.getId()) {
                    db.getData().mangas.set(i, manga);
                    break;
                }
            }
        }
        db.save();
        return manga;
    }

    @Override
    public Optional<Manga> findById(int id) {
        return db.getData().mangas.stream()
                .filter(m -> m.getId() == id)
                .findFirst();
    }

    @Override
    public Optional<Manga> findByFolderPath(String folderPath) {
        return db.getData().mangas.stream()
                .filter(m -> m.getFolderPath().equals(folderPath))
                .findFirst();
    }

    @Override
    public List<Manga> findAll() {
        return new ArrayList<>(db.getData().mangas);
    }

    @Override
    public void delete(int id) {
        db.getData().mangas.removeIf(m -> m.getId() == id);
        db.save();
    }

    @Override
    public void setFavorite(int id, boolean favorite) {
        findById(id).ifPresent(m -> {
            m.setFavorite(favorite);
            db.save();
        });
    }
}
