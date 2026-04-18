package com.mangareader.app.ui.controller;

import com.mangareader.app.AppContext;
import com.mangareader.app.Main;
import com.mangareader.app.pdf.PdfRenderer;
import com.mangareader.core.model.Chapter;
import com.mangareader.core.model.Manga;
import com.mangareader.core.model.ReadingProgress;
import com.mangareader.core.service.ProgressService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ReaderController {

    @FXML private ScrollPane scrollPane;
    @FXML private StackPane imageContainer;
    @FXML private VBox verticalContainer;
    @FXML private ImageView pageView;
    @FXML private Label chapterLabel;
    @FXML private Label pageLabel;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Button prevChapterBtn;
    @FXML private Button nextChapterBtn;
    @FXML private StackPane loadingOverlay;
    @FXML private Button singleModeBtn;
    @FXML private Button verticalModeBtn;

    private Manga manga;
    private List<Chapter> chapters;
    private int chapterIndex;
    private int currentPage;
    private boolean verticalMode = false;

    private final PdfRenderer pdfRenderer = new PdfRenderer();
    private ProgressService progressService;

    @FXML
    public void initialize() {
        progressService = AppContext.get().getProgressService();
        updateModeButtons();
    }

    public void openManga(Manga manga) {
        this.manga = manga;
        try {
            chapters = progressService.getChapters(manga.getId());
            if (chapters.isEmpty()) {
                chapterLabel.setText("Nenhum capítulo encontrado");
                return;
            }

            Optional<ReadingProgress> saved = progressService.getProgress(
                    AppContext.get().currentUserId(), manga.getId());

            if (saved.isPresent()) {
                ReadingProgress rp = saved.get();
                currentPage  = rp.getPage();
                chapterIndex = indexOfChapter(rp.getChapterId());
            } else {
                chapterIndex = 0;
                currentPage  = 0;
            }

            loadChapter(chapterIndex, currentPage);
            setupKeyboard();
        } catch (Exception e) {
            chapterLabel.setText("Erro ao abrir: " + e.getMessage());
        }
    }

    private void loadChapter(int idx, int page) {
        if (idx < 0 || idx >= chapters.size()) return;
        
        Chapter chapter = chapters.get(idx);
        showLoading(true);

        new Thread(() -> {
            try {
                pdfRenderer.open(chapter.getFilePath());
                int total = pdfRenderer.getPageCount();
                int safePage = Math.max(0, Math.min(page, total - 1));

                if (chapter.getPageCount() == 0) {
                    progressService.updatePageCount(chapter.getId(), total);
                    chapter.setPageCount(total);
                }

                Platform.runLater(() -> {
                    chapterIndex = idx;
                    currentPage = safePage;
                    if (verticalMode) loadAllPagesVertical();
                    else loadSinglePage(safePage);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    chapterLabel.setText("Erro no PDF: " + e.getMessage());
                    showLoading(false);
                });
            }
        }, "pdf-loader").start();
    }

    private double lastZoomWidth = -1;

    private void loadSinglePage(int page) {
        showLoading(true);
        new Thread(() -> {
            try {
                var image = pdfRenderer.renderPage(page);
                Platform.runLater(() -> {
                    verticalContainer.getChildren().setAll(imageContainer);
                    pageView.setImage(image);
                    currentPage = page;
                    updateLabels(chapters.get(chapterIndex), pdfRenderer.getPageCount());
                    saveProgress();
                    
                    if (lastZoomWidth > 0) pageView.setFitWidth(lastZoomWidth);
                    else fitToWidth();
                    
                    showLoading(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showLoading(false));
            }
        }, "page-render").start();
    }

    private void loadAllPagesVertical() {
        showLoading(true);
        verticalContainer.getChildren().clear();
        int total = pdfRenderer.getPageCount();
        
        // 1. Calculate aspect ratio more robustly
        double aspectRatio = 1.41;
        try {
            var firstPage = pdfRenderer.renderPage(0);
            aspectRatio = firstPage.getHeight() / firstPage.getWidth();
        } catch (Exception ignored) {}

        double containerWidth = scrollPane.getViewportBounds().getWidth();
        if (containerWidth <= 0) containerWidth = 800;
        final double targetWidth = containerWidth * 0.50; // 50% for vertical mode
        final double targetHeight = targetWidth * aspectRatio;

        // 2. Create placeholders with fixed sizes to ensure ScrollPane knows the full height
        for (int i = 0; i < total; i++) {
            StackPane placeholder = new StackPane();
            placeholder.setPrefSize(targetWidth, targetHeight);
            placeholder.setMinSize(targetWidth, targetHeight);
            placeholder.setMaxSize(targetWidth, targetHeight);
            placeholder.setStyle("-fx-background-color: #1e1e2e; -fx-background-radius: 5;");
            
            Label pageNum = new Label("Carregando " + (i + 1));
            pageNum.setStyle("-fx-text-fill: #444;");
            placeholder.getChildren().add(pageNum);
            
            verticalContainer.getChildren().add(placeholder);
        }

        // 3. Setup Virtual Scrolling Listener
        scrollPane.vvalueProperty().addListener((obs, old, val) -> updateVisiblePages());
        scrollPane.viewportBoundsProperty().addListener((obs, old, val) -> updateVisiblePages());
        
        Platform.runLater(() -> {
            updateVisiblePages();
            showLoading(false);
            updateLabels(chapters.get(chapterIndex), total);
            scrollPane.setVvalue(0);
        });
    }

    private synchronized void updateVisiblePages() {
        if (!verticalMode || verticalContainer.getChildren().isEmpty()) return;

        double vValue = scrollPane.getVvalue();
        double contentHeight = verticalContainer.getBoundsInLocal().getHeight();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        
        if (contentHeight <= 0 || viewportHeight <= 0) return;

        double scrollPos = vValue * (contentHeight - viewportHeight);
        
        // Re-calculate individual page height from first child (they are all same)
        StackPane first = (StackPane) verticalContainer.getChildren().get(0);
        double pageH = first.getPrefHeight();
        double spacing = verticalContainer.getSpacing();

        int startIdx = (int) (scrollPos / (pageH + spacing)) - 1;
        int endIdx = (int) ((scrollPos + viewportHeight) / (pageH + spacing)) + 1;

        startIdx = Math.max(0, startIdx);
        endIdx = Math.min(verticalContainer.getChildren().size() - 1, endIdx);

        for (int i = 0; i < verticalContainer.getChildren().size(); i++) {
            StackPane pane = (StackPane) verticalContainer.getChildren().get(i);
            boolean isVisible = (i >= startIdx && i <= endIdx);
            
            boolean hasImage = pane.getChildren().stream().anyMatch(n -> n instanceof ImageView);
            boolean isLoading = pane.getProperties().containsKey("loading");
            
            if (isVisible && !hasImage && !isLoading) {
                pane.getProperties().put("loading", true);
                final int idx = i;
                new Thread(() -> {
                    try {
                        var img = pdfRenderer.renderPage(idx);
                        Platform.runLater(() -> {
                            ImageView iv = new ImageView(img);
                            iv.setPreserveRatio(true);
                            iv.setFitWidth(pane.getPrefWidth());
                            iv.setSmooth(true);
                            iv.setCache(true);
                            pane.getChildren().clear();
                            pane.getChildren().add(iv);
                            pane.getProperties().remove("loading");
                        });
                    } catch (Exception e) {
                        pane.getProperties().remove("loading");
                    }
                }).start();
            } else if (!isVisible && hasImage) {
                pane.getChildren().clear();
                Label pageNum = new Label("Página " + (i + 1));
                pageNum.setStyle("-fx-text-fill: #444;");
                pane.getChildren().add(pageNum);
            }
        }
    }

    @FXML private void setSingleMode() {
        verticalMode = false;
        updateModeButtons();
        loadChapter(chapterIndex, currentPage);
    }

    @FXML private void setVerticalMode() {
        verticalMode = true;
        updateModeButtons();
        loadChapter(chapterIndex, currentPage);
    }

    private void updateModeButtons() {
        if (singleModeBtn == null) return;
        singleModeBtn.getStyleClass().removeAll("btn-nav-primary");
        verticalModeBtn.getStyleClass().removeAll("btn-nav-primary");
        if (verticalMode) verticalModeBtn.getStyleClass().add("btn-nav-primary");
        else singleModeBtn.getStyleClass().add("btn-nav-primary");
    }

    @FXML private void fitToWidth() {
        if (verticalMode) return;
        double width = scrollPane.getViewportBounds().getWidth();
        if (width > 0) {
            pageView.setFitWidth(width * 0.40); // 40% default for pagination
            lastZoomWidth = pageView.getFitWidth();
            updateLabels(chapters.get(chapterIndex), pdfRenderer.getPageCount());
        }
    }

    private void updateLabels(Chapter chapter, int totalPages) {
        chapterLabel.setText("Cap. " + formatChapterNumber(chapter.getChapterNumber()) + " — " + chapter.getTitle());
        
        // Calculate zoom percentage
        double zoomPercent = 100;
        double viewWidth = scrollPane.getViewportBounds().getWidth();
        if (viewWidth > 0) {
            if (verticalMode && !verticalContainer.getChildren().isEmpty()) {
                StackPane first = (StackPane) verticalContainer.getChildren().get(0);
                double baseWidth = viewWidth * 0.50; // Base 50% for vertical
                zoomPercent = (first.getPrefWidth() / baseWidth) * 100;
            } else if (!verticalMode) {
                double baseWidth = viewWidth * 0.40; // Base 40% for pagination
                zoomPercent = (pageView.getFitWidth() / baseWidth) * 100;
            }
        }

        String zoomStr = String.format(" (%.0f%%)", zoomPercent);
        pageLabel.setText((verticalMode ? totalPages + " Páginas" : "Página " + (currentPage + 1) + " / " + totalPages) + zoomStr);
        
        prevPageBtn.setDisable(verticalMode || (currentPage <= 0 && chapterIndex <= 0));
        nextPageBtn.setDisable(verticalMode || (currentPage >= totalPages - 1 && chapterIndex >= chapters.size() - 1));
        
        prevChapterBtn.setDisable(chapterIndex <= 0);
        nextChapterBtn.setDisable(chapterIndex >= chapters.size() - 1);
    }

    @FXML private void nextPage() {
        if (verticalMode) return;
        int total = pdfRenderer.getPageCount();
        if (currentPage < total - 1) loadSinglePage(currentPage + 1);
        else if (chapterIndex < chapters.size() - 1) loadChapter(chapterIndex + 1, 0);
    }

    @FXML private void prevPage() {
        if (verticalMode) return;
        if (currentPage > 0) loadSinglePage(currentPage - 1);
        else if (chapterIndex > 0) loadChapter(chapterIndex - 1, Integer.MAX_VALUE);
    }

    @FXML private void nextChapter() {
        if (chapterIndex < chapters.size() - 1) loadChapter(chapterIndex + 1, 0);
    }

    @FXML private void prevChapter() {
        if (chapterIndex > 0) loadChapter(chapterIndex - 1, 0);
    }

    private void saveProgress() {
        if (chapters == null || chapters.isEmpty()) return;
        Chapter ch = chapters.get(chapterIndex);
        progressService.saveProgress(AppContext.get().currentUserId(), manga.getId(), ch.getId(), currentPage);
    }

    private void setupKeyboard() {
        if (scrollPane.getScene() == null) {
            scrollPane.sceneProperty().addListener((obs, old, newScene) -> {
                if (newScene != null) registerListeners(newScene);
            });
        } else registerListeners(scrollPane.getScene());
    }

    private void registerListeners(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.isControlDown()) {
                if (e.getCode() == KeyCode.EQUALS || e.getCode() == KeyCode.PLUS || e.getCode() == KeyCode.ADD) zoom(1.05);
                else if (e.getCode() == KeyCode.MINUS || e.getCode() == KeyCode.SUBTRACT) zoom(0.95);
            } else {
                if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.SPACE) nextPage();
                else if (e.getCode() == KeyCode.LEFT) prevPage();
            }
        });

        // Zoom only with Ctrl as requested
        scrollPane.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                double factor = (e.getDeltaY() > 0) ? 1.05 : 0.95;
                zoom(factor);
                updateLabels(chapters.get(chapterIndex), pdfRenderer.getPageCount());
                e.consume(); // Critical: consume to stop scrolling
            }
        });

        setupDragToScroll();
    }

    private void setupDragToScroll() {
        final double[] lastPos = new double[2];
        scrollPane.setOnMousePressed(e -> {
            lastPos[0] = e.getSceneX();
            lastPos[1] = e.getSceneY();
        });
        scrollPane.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - lastPos[0];
            double dy = e.getSceneY() - lastPos[1];
            scrollPane.setHvalue(scrollPane.getHvalue() - dx / verticalContainer.getBoundsInLocal().getWidth());
            scrollPane.setVvalue(scrollPane.getVvalue() - dy / verticalContainer.getBoundsInLocal().getHeight());
            lastPos[0] = e.getSceneX();
            lastPos[1] = e.getSceneY();
        });
    }

    private void zoom(double factor) {
        if (verticalMode) {
            for (javafx.scene.Node node : verticalContainer.getChildren()) {
                if (node instanceof StackPane pane) {
                    double nw = pane.getPrefWidth() * factor;
                    double nh = pane.getPrefHeight() * factor;
                    if (nw >= 200 && nw <= 5000) {
                        pane.setPrefSize(nw, nh);
                        pane.setMinSize(nw, nh);
                        pane.setMaxSize(nw, nh);
                        
                        // Resize image if present
                        pane.getChildren().stream()
                            .filter(n -> n instanceof ImageView)
                            .map(n -> (ImageView) n)
                            .forEach(iv -> iv.setFitWidth(nw));
                    }
                }
            }
            return;
        }
        
        double currentWidth = pageView.getFitWidth();
        if (currentWidth <= 0) currentWidth = pageView.getBoundsInLocal().getWidth();
        if (currentWidth <= 0) currentWidth = 600;

        double newWidth = currentWidth * factor;
        if (newWidth >= 200 && newWidth <= 5000) {
            pageView.setFitWidth(newWidth);
            lastZoomWidth = newWidth; // Store for pagination mode
        }
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisible(show);
    }

    private String formatChapterNumber(float n) {
        return n == (int) n ? String.valueOf((int) n) : String.valueOf(n);
    }

    private int indexOfChapter(int chapterId) {
        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).getId() == chapterId) return i;
        }
        return 0;
    }

    @FXML
    private void backToLibrary() throws IOException {
        pdfRenderer.close();
        Main.loadScene((Stage) scrollPane.getScene().getWindow(), "fxml/library.fxml", "Biblioteca — Manga Reader");
    }
}
