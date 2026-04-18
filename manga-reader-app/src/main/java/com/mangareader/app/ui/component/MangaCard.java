package com.mangareader.app.ui.component;

import com.mangareader.core.model.Chapter;
import com.mangareader.core.model.Manga;
import com.mangareader.core.model.ReadingProgress;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Visual card representing a manga in the library grid.
 */
public class MangaCard extends VBox {

    private static final double CARD_WIDTH   = 160;
    private static final double COVER_HEIGHT = 220;

    private final Manga manga;

    public MangaCard(Manga manga, List<Chapter> chapters,
                     Optional<ReadingProgress> progress) {
        this.manga = manga;
        getStyleClass().add("manga-card");
        setAlignment(Pos.TOP_CENTER);
        setSpacing(6);
        setPrefWidth(CARD_WIDTH);

        // Cover container (image or placeholder)
        StackPane coverContainer = buildCover(manga.getCoverPath());
        
        // Favorite Badge
        if (manga.isFavorite()) {
            Label favBadge = new Label("★");
            favBadge.getStyleClass().add("fav-badge");
            StackPane.setAlignment(favBadge, Pos.TOP_RIGHT);
            coverContainer.getChildren().add(favBadge);
        }

        // Chapter Count Badge (always show if chapters exist)
        if (!chapters.isEmpty()) {
            Label chBadge = new Label(chapters.size() + " Caps");
            chBadge.getStyleClass().add("chapter-badge");
            StackPane.setAlignment(chBadge, Pos.TOP_LEFT);
            coverContainer.getChildren().add(chBadge);
        }

        getChildren().add(coverContainer);

        // Title
        Label title = new Label(manga.getTitle());
        title.getStyleClass().add("manga-title");
        title.setWrapText(true);
        title.setMaxWidth(CARD_WIDTH - 10);
        getChildren().add(title);

        // Progress bar (only if manga has chapters and saved progress exists)
        if (!chapters.isEmpty() && progress.isPresent()) {
            ReadingProgress rp = progress.get();
            int currentChapterIdx = indexOfChapter(chapters, rp.getChapterId());
            Chapter currentChapter = chapters.get(currentChapterIdx);
            
            double chapterProgress = 0;
            if (currentChapter.getPageCount() > 0) {
                chapterProgress = (double) (rp.getPage() + 1) / currentChapter.getPageCount();
            }
            
            double totalProgress = (double) (currentChapterIdx + chapterProgress) / chapters.size();

            ProgressBar bar = new ProgressBar(Math.min(1.0, totalProgress));
            bar.getStyleClass().add("manga-progress");
            bar.setPrefWidth(CARD_WIDTH - 10);

            int percent = (int) (totalProgress * 100);
            Label progressLabel = new Label("Progresso: " + percent + "%");
            progressLabel.getStyleClass().add("manga-progress-label");

            getChildren().addAll(bar, progressLabel);
        }
    }

    private StackPane buildCover(String coverPath) {
        StackPane container = new StackPane();
        container.setPrefWidth(CARD_WIDTH);
        container.setPrefHeight(COVER_HEIGHT);
        container.getStyleClass().add("cover-container");
        container.setStyle("-fx-background-color: #1e1e38; -fx-background-radius: 10; -fx-overflow: hidden;");

        if (coverPath != null && !coverPath.isBlank()) {
            File file = new File(coverPath);
            if (file.exists()) {
                try {
                    // Use ImageIO + SwingFXUtils as a fallback for WebP support
                    java.awt.image.BufferedImage bimg = javax.imageio.ImageIO.read(file);
                    if (bimg != null) {
                        Image img = javafx.embed.swing.SwingFXUtils.toFXImage(bimg, null);
                        ImageView view = new ImageView(img);
                        view.setFitWidth(CARD_WIDTH);
                        view.setFitHeight(COVER_HEIGHT);
                        view.setPreserveRatio(false);
                        view.setSmooth(true);
                        
                        Rectangle clip = new Rectangle(CARD_WIDTH, COVER_HEIGHT);
                        clip.setArcWidth(20);
                        clip.setArcHeight(20);
                        view.setClip(clip);
                        
                        container.getChildren().add(view);
                        return container;
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao carregar capa via ImageIO: " + e.getMessage());
                }
            }
        }

        return buildPlaceholder(container);
    }

    private StackPane buildPlaceholder(StackPane container) {
        VBox placeholderBox = new VBox(10);
        placeholderBox.setAlignment(Pos.CENTER);
        Label icon = new Label("📖");
        icon.setStyle("-fx-font-size: 48px;");
        Label text = new Label(manga.getTitle());
        text.setStyle("-fx-text-fill: #6c63ff; -fx-font-size: 10px; -fx-font-weight: bold;");
        text.setWrapText(true);
        text.setAlignment(Pos.CENTER);
        text.setMaxWidth(CARD_WIDTH - 20);
        
        placeholderBox.getChildren().addAll(icon, text);
        container.getChildren().add(placeholderBox);
        return container;
    }

    private int indexOfChapter(List<Chapter> chapters, int chapterId) {
        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).getId() == chapterId) return i;
        }
        return 0;
    }

    public Manga getManga() { return manga; }
}
