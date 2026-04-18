package com.mangareader.persistence;

import com.google.gson.*;
import com.mangareader.core.model.Chapter;
import com.mangareader.core.model.Manga;
import com.mangareader.core.model.ReadingProgress;
import com.mangareader.core.model.User;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JsonDatabase {
    private static final String FILE_NAME = "manga-reader-data.json";
    
    private final Path dbFile;
    private final Gson gson;
    private DatabaseRoot data;

    public JsonDatabase(String libraryPath) {
        if (libraryPath == null || libraryPath.isBlank()) {
            throw new IllegalArgumentException("Library path must be set before initializing database.");
        }
        this.dbFile = Paths.get(libraryPath, FILE_NAME);
        
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> 
                        LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .setPrettyPrinting()
                .create();
        
        load();
    }

    private void load() {
        if (Files.exists(dbFile)) {
            try (Reader reader = Files.newBufferedReader(dbFile, StandardCharsets.UTF_8)) {
                data = gson.fromJson(reader, DatabaseRoot.class);
            } catch (IOException e) {
                System.err.println("Failed to read database file: " + e.getMessage());
            }
        }
        if (data == null) {
            data = new DatabaseRoot();
            // Create default user if missing
            User defaultUser = new User();
            defaultUser.setId(1);
            defaultUser.setUsername("local_user");
            data.users.add(defaultUser);
            save();
        }
    }

    public synchronized void save() {
        try {
            Files.createDirectories(dbFile.getParent());
            try (Writer writer = Files.newBufferedWriter(dbFile, StandardCharsets.UTF_8)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save database file: " + e.getMessage());
        }
    }

    public DatabaseRoot getData() {
        return data;
    }

    // --- Inner Model ---
    public static class DatabaseRoot {
        public List<User> users = new ArrayList<>();
        public List<Manga> mangas = new ArrayList<>();
        public List<Chapter> chapters = new ArrayList<>();
        public List<ReadingProgress> progresses = new ArrayList<>();
        
        public int nextMangaId = 1;
        public int nextChapterId = 1;
        public int nextProgressId = 1;
        public int nextUserId = 2; // 1 is default
    }
}
