package com.mangareader.app.ui.controller;

import com.mangareader.app.AppContext;
import com.mangareader.app.Main;
import com.mangareader.app.ui.component.MangaCard;
import com.mangareader.core.model.Chapter;
import com.mangareader.core.model.Manga;
import com.mangareader.core.model.ReadingProgress;
import com.mangareader.core.service.LibraryService;
import com.mangareader.core.service.ProgressService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class LibraryController {

    @FXML private FlowPane mangaGrid;
    @FXML private Label statusLabel;

    private LibraryService libraryService;
    private ProgressService progressService;

    @FXML
    public void initialize() {
        libraryService  = AppContext.get().getLibraryService();
        progressService = AppContext.get().getProgressService();
        loadLibrary();
    }

    private void loadLibrary() {
        mangaGrid.getChildren().clear();
        List<Manga> mangas = libraryService.getAllMangas();
        
        if (mangas.isEmpty()) {
            statusLabel.setText("Nenhum mangá na biblioteca. Clique em 'Escanear'.");
            return;
        }

        statusLabel.setText(mangas.size() + " mangá(s) carregado(s)");

        for (Manga manga : mangas) {
            List<Chapter> chapters = libraryService.getChapters(manga.getId());
            Optional<ReadingProgress> progress = progressService.getProgress(
                    AppContext.get().currentUserId(), manga.getId());

            MangaCard card = new MangaCard(manga, chapters, progress);
            card.setOnMouseClicked(e -> {
                if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) openReader(manga);
            });
            
            card.setOnContextMenuRequested(e -> createContextMenu(manga, card).show(card, e.getScreenX(), e.getScreenY()));
            mangaGrid.getChildren().add(card);
        }
    }

    private javafx.scene.control.ContextMenu createContextMenu(Manga manga, MangaCard card) {
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();

        javafx.scene.control.MenuItem favItem = new javafx.scene.control.MenuItem(manga.isFavorite() ? "Desfavoritar" : "Favoritar");
        favItem.setOnAction(e -> {
            libraryService.setFavorite(manga.getId(), !manga.isFavorite());
            loadLibrary();
        });

        javafx.scene.control.MenuItem coverItem = new javafx.scene.control.MenuItem("Alterar Capa...");
        coverItem.setOnAction(e -> {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Selecionar Capa");
            chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.png", "*.webp"));
            java.io.File file = chooser.showOpenDialog(mangaGrid.getScene().getWindow());
            if (file != null) {
                libraryService.updateCover(manga.getId(), file.getAbsolutePath());
                loadLibrary();
            }
        });

        javafx.scene.control.SeparatorMenuItem separator = new javafx.scene.control.SeparatorMenuItem();

        javafx.scene.control.MenuItem delDbItem = new javafx.scene.control.MenuItem("Remover da Biblioteca");
        delDbItem.setOnAction(e -> confirmAndRun("Remover da Biblioteca", "Deseja remover apenas o registro de '" + manga.getTitle() + "'?", () -> {
            libraryService.removeMangaFromDb(manga);
            loadLibrary();
        }));

        javafx.scene.control.MenuItem delItem = new javafx.scene.control.MenuItem("Excluir Permanentemente");
        delItem.setStyle("-fx-text-fill: #ff4444;");
        delItem.setOnAction(e -> confirmAndRun("Excluir Definitivamente", "ATENÇÃO: Isso apagará os arquivos de '" + manga.getTitle() + "' do disco!", () -> {
            libraryService.deleteManga(manga);
            loadLibrary();
        }));

        contextMenu.getItems().addAll(favItem, coverItem, separator, delDbItem, delItem);
        return contextMenu;
    }

    private void confirmAndRun(String title, String content, Runnable action) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait().ifPresent(res -> {
            if (res == javafx.scene.control.ButtonType.OK) action.run();
        });
    }

    @FXML
    private void toggleTheme() {
        if (mangaGrid.getScene() != null) {
            javafx.collections.ObservableList<String> classes = mangaGrid.getScene().getRoot().getStyleClass();
            if (classes.contains("light-theme")) {
                classes.remove("light-theme");
            } else {
                classes.add("light-theme");
            }
        }
    }

    @FXML
    private void scanLibrary() {
        java.util.List<String> paths = AppContext.get().getConfig().getLibraryPaths();
        if (paths.isEmpty()) {
            showAlert("Configure ao menos uma pasta de mangás nas Configurações.");
            return;
        }
        new Thread(() -> {
            try {
                LibraryService.ScanResult result = libraryService.scanLibrary(paths);
                Platform.runLater(() -> {
                    statusLabel.setText("Scan completo: " + result.mangasFound()
                            + " mangás processados, " + result.chaptersFound() + " novos capítulos.");
                    loadLibrary();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Erro no scan: " + e.getMessage()));
            }
        }, "library-scan").start();
    }

    @FXML
    private void openSettings() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/settings.fxml"));
        javafx.scene.Parent root = loader.load();
        Stage stage = (Stage) mangaGrid.getScene().getWindow();
        stage.setTitle("Configurações — Manga Reader");
        stage.getScene().setRoot(root);
    }

    private void openReader(Manga manga) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/reader.fxml"));
            javafx.scene.Parent root = loader.load();
            ReaderController ctrl = loader.getController();
            ctrl.openManga(manga);
            Stage stage = (Stage) mangaGrid.getScene().getWindow();
            stage.setTitle(manga.getTitle() + " — Manga Reader");
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Erro ao abrir manga: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
