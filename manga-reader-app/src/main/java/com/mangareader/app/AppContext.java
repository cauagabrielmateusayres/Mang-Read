package com.mangareader.app;

import com.mangareader.app.config.AppConfig;
import com.mangareader.core.service.AuthService;
import com.mangareader.core.service.LibraryService;
import com.mangareader.core.service.ProgressService;
import com.mangareader.persistence.*;

/**
 * Application-wide singleton that wires all services and repositories.
 * Call AppContext.init(config) once at startup, then use AppContext.get() anywhere.
 */
public class AppContext {

    private static AppContext instance;

    private final AppConfig config;
    private final JsonDatabase jsonDb;
    private final LibraryService libraryService;
    private final ProgressService progressService;
    private final AuthService authService;

    private AppContext(AppConfig config) throws Exception {
        this.config = config;

        // 1. Initialize JSON database
        jsonDb = new JsonDatabase(config.getLibraryPath());

        // 2. Build repositories
        var userRepo     = new JsonUserRepository(jsonDb);
        var mangaRepo    = new JsonMangaRepository(jsonDb);
        var chapterRepo  = new JsonChapterRepository(jsonDb);
        var progressRepo = new JsonProgressRepository(jsonDb);

        // 3. Build services
        authService     = new AuthService(userRepo);
        libraryService  = new LibraryService(mangaRepo, chapterRepo, config.getLibraryPath());
        progressService = new ProgressService(progressRepo, chapterRepo);

        // 4. Auto-login as local user
        authService.loginAsLocalUser();
    }

    public static void init(AppConfig config) throws Exception {
        instance = new AppContext(config);
    }

    public static AppContext get() {
        if (instance == null) throw new IllegalStateException("AppContext not initialized");
        return instance;
    }

    public AppConfig getConfig()               { return config; }
    public LibraryService getLibraryService()  { return libraryService; }
    public ProgressService getProgressService(){ return progressService; }
    public AuthService getAuthService()        { return authService; }

    public int currentUserId() {
        return authService.getCurrentUser().getId();
    }

    public void shutdown() {
        if (jsonDb != null) jsonDb.save();
    }
}
