package com.gameboost;

import com.gameboost.util.AdminChecker;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Warn but don't block — UAC manifest handles the real elevation
        if (!AdminChecker.isAdmin()) {
            System.err.println("[GameBoost] WARNING: Not running as Administrator. Some features will fail.");
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gameboost/main.fxml"));
        Scene scene = new Scene(loader.load(), 1100, 720);
        scene.getStylesheets().add(getClass().getResource("/com/gameboost/css/dark-theme.css").toExternalForm());

        stage.setTitle("GameBoost");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
