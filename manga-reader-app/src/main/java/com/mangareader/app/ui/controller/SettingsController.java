package com.mangareader.app.ui.controller;

import com.mangareader.app.AppContext;
import com.mangareader.app.Main;
import com.mangareader.app.config.AppConfig;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class SettingsController {

    @FXML private ListView<String> pathsListView;
    @FXML private Label statusLabel;
    @FXML private Button saveButton;

    private final AppConfig config = new AppConfig();
    private final javafx.collections.ObservableList<String> paths = javafx.collections.FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        paths.addAll(config.getLibraryPaths());
        pathsListView.setItems(paths);
    }

    @FXML
    private void addPath() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Selecionar pasta da biblioteca");
        File dir = chooser.showDialog(saveButton.getScene().getWindow());
        if (dir != null) {
            String newPath = dir.getAbsolutePath();
            if (!paths.contains(newPath)) {
                paths.add(newPath);
            }
        }
    }

    @FXML
    private void removePath() {
        String selected = pathsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            paths.remove(selected);
        }
    }

    @FXML
    private void saveAndClose() {
        config.setLibraryPaths(new java.util.ArrayList<>(paths));
        config.save();

        try {
            AppContext.init(config);
            navigateTo("fxml/library.fxml", "Biblioteca — Manga Reader");
        } catch (Exception e) {
            showError("Falha ao salvar: " + e.getMessage());
        }
    }

    private void navigateTo(String fxml, String title) throws IOException {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        Main.loadScene(stage, fxml, title);
    }

    public void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
    }
}
