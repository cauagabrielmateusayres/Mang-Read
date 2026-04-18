package com.mangareader.app.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Stores and loads application configuration from:
 *   ~/.manga-reader/config.properties
 *
 * Defaults to localhost/root/1324 for ease of first-run.
 */
public class AppConfig {

    private static final Path CONFIG_DIR =
            Paths.get(System.getProperty("user.home"), ".manga-reader");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.properties");

    private final Properties props = new Properties();

    public AppConfig() {
        loadDefaults();
        load();
    }

    private void loadDefaults() {
        props.setProperty("library.paths", "");
    }

    private void load() {
        if (Files.exists(CONFIG_FILE)) {
            try (InputStream is = Files.newInputStream(CONFIG_FILE)) {
                props.load(is);
            } catch (IOException ignored) {}
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (OutputStream os = Files.newOutputStream(CONFIG_FILE)) {
                props.store(os, "Manga Reader Configuration");
            }
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar configuração", e);
        }
    }

    public boolean isConfigured() {
        return !getLibraryPaths().isEmpty();
    }

    // ----- Getters / Setters -----

    public java.util.List<String> getLibraryPaths() {
        String paths = props.getProperty("library.paths", "");
        if (paths.isBlank()) return new java.util.ArrayList<>();
        return new java.util.ArrayList<>(java.util.Arrays.asList(paths.split(",")));
    }

    public void setLibraryPaths(java.util.List<String> paths) {
        props.setProperty("library.paths", String.join(",", paths));
    }

    /** Keep for backward compatibility/single-path usage if needed, but redirects to list */
    public String getLibraryPath() {
        java.util.List<String> paths = getLibraryPaths();
        return paths.isEmpty() ? "" : paths.get(0);
    }
}
