package com.mangareader.core.service;

import com.mangareader.core.model.Chapter;
import com.mangareader.core.model.Manga;
import com.mangareader.core.repository.ChapterRepository;
import com.mangareader.core.repository.MangaRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Scans a library directory to discover mangas and chapters.
 * Directory layout expected:
 *   libraryRoot/
 *     MangaTitle/
 *       chapter01.pdf
 *       chapter02.pdf
 *       cover.jpg  (optional)
 */
public class LibraryService {

    private final MangaRepository mangaRepository;
    private final ChapterRepository chapterRepository;

    public LibraryService(MangaRepository mangaRepository, ChapterRepository chapterRepository) {
        this.mangaRepository = mangaRepository;
        this.chapterRepository = chapterRepository;
    }

    public List<Manga> getAllMangas() {
        return mangaRepository.findAll();
    }

    public List<Chapter> getChapters(int mangaId) {
        return chapterRepository.findByMangaId(mangaId);
    }

    /**
     * Scans multiple library paths and registers/updates mangas/chapters found.
     * Existing entries are updated (cover, title) instead of duplicated.
     */
    public ScanResult scanLibrary(List<String> libraryPaths) {
        ScanCounter counter = new ScanCounter();
        List<String> errors = new ArrayList<>();

        for (String path : libraryPaths) {
            try {
                File libraryDir = new File(path);
                if (!libraryDir.exists() || !libraryDir.isDirectory()) continue;

                // 1. Check if the directory itself is a manga (contains PDFs anywhere)
                List<File> allPdfs = new ArrayList<>();
                findPdfsRecursive(libraryDir, allPdfs);
                
                if (!allPdfs.isEmpty()) {
                    // Check if there are subdirectories that might be separate mangas
                    File[] subDirs = libraryDir.listFiles(File::isDirectory);
                    boolean hasMangaSubs = false;
                    if (subDirs != null) {
                        for (File sub : subDirs) {
                            if (containsPdf(sub)) {
                                hasMangaSubs = true;
                                break;
                            }
                        }
                    }

                    if (!hasMangaSubs) {
                        // This directory IS the manga
                        processMangaFolder(libraryDir, allPdfs.toArray(new File[0]), errors, counter);
                        continue;
                    }
                }

                // 2. Check subdirectories as potential separate mangas
                File[] mangaDirs = libraryDir.listFiles(File::isDirectory);
                if (mangaDirs != null) {
                    Arrays.sort(mangaDirs);
                    for (File mangaDir : mangaDirs) {
                        List<File> pdfs = new ArrayList<>();
                        findPdfsRecursive(mangaDir, pdfs);
                        if (!pdfs.isEmpty()) {
                            processMangaFolder(mangaDir, pdfs.toArray(new File[0]), errors, counter);
                        }
                    }
                }
            } catch (Exception e) {
                errors.add("Erro ao escanear " + path + ": " + e.getMessage());
            }
        }
        
        return new ScanResult(counter.mangasFound, counter.chaptersFound, errors);
    }

    private void findPdfsRecursive(File dir, List<File> result) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) findPdfsRecursive(f, result);
            else if (f.getName().toLowerCase().endsWith(".pdf")) result.add(f);
        }
    }

    private boolean containsPdf(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return false;
        for (File f : files) {
            if (f.isDirectory() && containsPdf(f)) return true;
            if (f.isFile() && f.getName().toLowerCase().endsWith(".pdf")) return true;
        }
        return false;
    }

    private void processMangaFolder(File mangaDir, File[] pdfs, List<String> errors, ScanCounter counter) {
        try {
            String absPath = mangaDir.getAbsolutePath();
            var existingManga = mangaRepository.findByFolderPath(absPath);
            
            Manga manga;
            if (existingManga.isPresent()) {
                manga = existingManga.get();
                // Update title and cover if needed
                manga.setTitle(mangaDir.getName());
                File cover = findCover(mangaDir);
                if (cover != null) manga.setCoverPath(cover.getAbsolutePath());
                mangaRepository.save(manga);
            } else {
                manga = new Manga(mangaDir.getName(), absPath);
                File cover = findCover(mangaDir);
                if (cover != null) manga.setCoverPath(cover.getAbsolutePath());
                manga = mangaRepository.save(manga);
            }
            counter.mangasFound++;

            Arrays.sort(pdfs);

            for (int i = 0; i < pdfs.length; i++) {
                File pdf = pdfs[i];
                var existingChapter = chapterRepository.findByFilePath(pdf.getAbsolutePath());
                
                if (existingChapter.isEmpty()) {
                    Chapter ch = new Chapter();
                    ch.setMangaId(manga.getId());
                    ch.setChapterNumber(i + 1);
                    ch.setTitle(stripExtension(pdf.getName()));
                    ch.setFilePath(pdf.getAbsolutePath());
                    ch.setPageCount(0);
                    chapterRepository.save(ch);
                    counter.chaptersFound++;
                } else {
                    // Just update metadata if needed
                    Chapter ch = existingChapter.get();
                    ch.setChapterNumber(i + 1);
                    ch.setTitle(stripExtension(pdf.getName()));
                    chapterRepository.save(ch);
                }
            }
        } catch (Exception e) {
            errors.add(mangaDir.getName() + ": " + e.getMessage());
        }
    }

    private static class ScanCounter {
        int mangasFound = 0;
        int chaptersFound = 0;
    }

    private File findCover(File dir) {
        String[] exts = {".jpg", ".jpeg", ".png", ".webp"};
        File[] imgs = dir.listFiles(f -> {
            String n = f.getName().toLowerCase();
            for (String e : exts) if (n.endsWith(e)) return true;
            return false;
        });
        
        if (imgs == null || imgs.length == 0) return null;
        
        // Prioritize specific filenames
        List<String> priorityNames = Arrays.asList("cover", "folder", "poster", "thumb");
        for (String priority : priorityNames) {
            for (File img : imgs) {
                String name = stripExtension(img.getName().toLowerCase());
                if (name.equals(priority)) return img;
            }
        }
        
        return imgs[0]; // Fallback to first image found
    }

    private String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(0, dot) : name;
    }

    public void deleteManga(Manga manga) {
        // Delete from DB (cascade deletes chapters and progress)
        mangaRepository.delete(manga.getId());
        
        // Delete from disk
        File folder = new File(manga.getFolderPath());
        if (folder.exists() && folder.isDirectory()) {
            deleteDirectory(folder);
        }
    }

    public void removeMangaFromDb(Manga manga) {
        // Delete only from DB
        mangaRepository.delete(manga.getId());
    }

    private void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    public void setFavorite(int mangaId, boolean isFavorite) {
        mangaRepository.setFavorite(mangaId, isFavorite);
    }

    public void updateCover(int mangaId, String coverPath) {
        mangaRepository.findById(mangaId).ifPresent(m -> {
            m.setCoverPath(coverPath);
            mangaRepository.save(m);
        });
    }

    public record ScanResult(int mangasFound, int chaptersFound, List<String> errors) {}
}
