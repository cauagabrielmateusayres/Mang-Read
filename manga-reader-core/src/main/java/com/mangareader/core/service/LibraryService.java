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
    private final String baseDir;

    public LibraryService(MangaRepository mangaRepository, ChapterRepository chapterRepository, String baseDir) {
        this.mangaRepository = mangaRepository;
        this.chapterRepository = chapterRepository;
        this.baseDir = baseDir;
    }

    public List<Manga> getAllMangas() {
        List<Manga> mangas = mangaRepository.findAll();
        mangas.forEach(this::resolveMangaPaths);
        return mangas;
    }

    public List<Chapter> getChapters(int mangaId) {
        List<Chapter> chapters = chapterRepository.findByMangaId(mangaId);
        chapters.forEach(this::resolveChapterPaths);
        return chapters;
    }

    private void resolveMangaPaths(Manga manga) {
        manga.setFolderPath(resolvePath(manga.getFolderPath()));
        if (manga.getCoverPath() != null) {
            manga.setCoverPath(resolvePath(manga.getCoverPath()));
        }
    }

    private void resolveChapterPaths(Chapter chapter) {
        chapter.setFilePath(resolvePath(chapter.getFilePath()));
    }

    private String resolvePath(String path) {
        if (path == null || path.isBlank()) return path;
        java.nio.file.Path p = java.nio.file.Paths.get(path);
        if (p.isAbsolute()) return path;
        return java.nio.file.Paths.get(baseDir).resolve(p).toAbsolutePath().toString();
    }

    private String relativizePath(String path) {
        if (path == null || path.isBlank() || baseDir == null || baseDir.isBlank()) return path;
        try {
            java.nio.file.Path base = java.nio.file.Paths.get(baseDir).toAbsolutePath();
            java.nio.file.Path target = java.nio.file.Paths.get(path).toAbsolutePath();
            if (target.startsWith(base)) {
                return base.relativize(target).toString();
            }
        } catch (Exception e) {
            // Fallback to absolute if relativization fails (e.g. different drives)
        }
        return path;
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
            String relPath = relativizePath(absPath);
            
            // Search using both to be safe, but prioritize relPath if it changed
            var existingManga = mangaRepository.findByFolderPath(relPath);
            if (existingManga.isEmpty() && !relPath.equals(absPath)) {
                existingManga = mangaRepository.findByFolderPath(absPath);
            }
            
            Manga manga;
            if (existingManga.isPresent()) {
                manga = existingManga.get();
                manga.setTitle(mangaDir.getName());
                manga.setFolderPath(relPath); // Update to relative
                File cover = findCover(mangaDir);
                if (cover != null) manga.setCoverPath(relativizePath(cover.getAbsolutePath()));
                mangaRepository.save(manga);
            } else {
                manga = new Manga(mangaDir.getName(), relPath);
                File cover = findCover(mangaDir);
                if (cover != null) manga.setCoverPath(relativizePath(cover.getAbsolutePath()));
                manga = mangaRepository.save(manga);
            }
            counter.mangasFound++;

            Arrays.sort(pdfs);

            for (int i = 0; i < pdfs.length; i++) {
                File pdf = pdfs[i];
                String absPdfPath = pdf.getAbsolutePath();
                String relPdfPath = relativizePath(absPdfPath);
                
                var existingChapter = chapterRepository.findByFilePath(relPdfPath);
                if (existingChapter.isEmpty() && !relPdfPath.equals(absPdfPath)) {
                    existingChapter = chapterRepository.findByFilePath(absPdfPath);
                }
                
                if (existingChapter.isEmpty()) {
                    Chapter ch = new Chapter();
                    ch.setMangaId(manga.getId());
                    ch.setChapterNumber(i + 1);
                    ch.setTitle(stripExtension(pdf.getName()));
                    ch.setFilePath(relPdfPath);
                    ch.setPageCount(0);
                    chapterRepository.save(ch);
                    counter.chaptersFound++;
                } else {
                    Chapter ch = existingChapter.get();
                    ch.setChapterNumber(i + 1);
                    ch.setTitle(stripExtension(pdf.getName()));
                    ch.setFilePath(relPdfPath); // Update to relative
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
        // Ensure we use the resolved path for deletion!
        File folder = new File(resolvePath(manga.getFolderPath()));
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
            m.setCoverPath(relativizePath(coverPath));
            mangaRepository.save(m);
        });
    }

    public record ScanResult(int mangasFound, int chaptersFound, List<String> errors) {}
}

