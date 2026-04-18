package com.mangareader.app;

import com.mangareader.app.config.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static AppContext appContext;

    @Override
    public void start(Stage primaryStage) throws IOException {
        AppConfig config = new AppConfig();
        primaryStage.setTitle("Manga Reader");
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(680);

        if (!config.isConfigured()) {
            loadScene(primaryStage, "fxml/settings.fxml", "Configurações — Manga Reader");
        } else {
            try {
                AppContext.init(config);
                loadScene(primaryStage, "fxml/library.fxml", "Biblioteca — Manga Reader");
            } catch (Exception e) {
                loadScene(primaryStage, "fxml/settings.fxml", "Configurações — Manga Reader");
                // Pass error to settings controller after scene is loaded
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/settings.fxml"));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(
                        Main.class.getResource("css/style.css").toExternalForm());
                com.mangareader.app.ui.controller.SettingsController ctrl = loader.getController();
                ctrl.showError("Falha ao conectar: " + e.getMessage());
                primaryStage.setScene(scene);
                primaryStage.setMaximized(true);
            }
        }

        primaryStage.setOnCloseRequest(e -> shutdown());
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void loadScene(Stage stage, String fxml, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
        javafx.scene.Parent root = loader.load();
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("css/style.css").toExternalForm());
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        stage.setTitle(title);
    }

    private void shutdown() {
        try {
            AppContext.get().shutdown();
        } catch (IllegalStateException ignored) {
            // AppContext not initialized (e.g., closed before connecting)
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
