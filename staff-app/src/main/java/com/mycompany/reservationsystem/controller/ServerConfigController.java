package com.mycompany.reservationsystem.controller;

import com.mycompany.reservationsystem.App;
import com.mycompany.reservationsystem.config.AppSettings;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerConfigController implements Initializable {

    @FXML
    private Label messageLabel;
    @FXML
    private MFXTextField serverUrlField;
    @FXML
    private MFXTextField websocketUrlField;
    @FXML
    private MFXButton saveBtn;
    @FXML
    private MFXButton testConnectionBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String savedServerUrl = AppSettings.loadServerUrl();
        String savedWebsocketUrl = AppSettings.loadWebsocketUrl();

        if (savedServerUrl != null && !savedServerUrl.isEmpty()) {
            serverUrlField.setText(savedServerUrl);
        }
        if (savedWebsocketUrl != null && !savedWebsocketUrl.isEmpty()) {
            websocketUrlField.setText(savedWebsocketUrl);
        }
    }

    @FXML
    private void saveConfig() {
        String serverUrl = serverUrlField.getText().trim();
        String websocketUrl = websocketUrlField.getText().trim();

        if (serverUrl.isEmpty()) {
            showError("Server URL is required");
            return;
        }

        if (websocketUrl.isEmpty()) {
            showError("WebSocket URL is required");
            return;
        }

        if (!isValidUrl(serverUrl)) {
            showError("Invalid Server URL format");
            return;
        }

        if (!isValidWebsocketUrl(websocketUrl)) {
            showError("Invalid WebSocket URL format");
            return;
        }

        AppSettings.saveServerUrl(serverUrl);
        AppSettings.saveWebsocketUrl(websocketUrl);

        System.setProperty("SERVER_URL", serverUrl);
        System.setProperty("WEBSOCKET_URL", websocketUrl);

        proceedToLogin();
    }

    private boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() != null && (uri.getScheme().equals("http") || uri.getScheme().equals("https"));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidWebsocketUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() != null && (uri.getScheme().equals("ws") || uri.getScheme().equals("wss"));
        } catch (Exception e) {
            return false;
        }
    }

    @FXML
    private void testConnection() {
        String serverUrl = serverUrlField.getText().trim();

        if (serverUrl.isEmpty()) {
            showError("Enter server URL first");
            return;
        }

        try {
            URI uri = new URI(serverUrl);
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                messageLabel.setText("Connection successful!");
                messageLabel.setStyle("-fx-text-fill: #2ECC71;");
            } else {
                showError("Connection failed: HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            showError("Connection failed: " + e.getMessage());
        }
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #E74C3C;");
    }

    @FXML
    private void closeApp() {
        try {
            Stage stage = App.primaryStage;
            FXMLLoader loginLoader = new FXMLLoader(App.class.getResource("/fxml/Login.fxml"));
            Parent loginRoot = loginLoader.load();

            javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fonts/Lora-Regular.ttf"), 14);
            javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fonts/Lora-Bold.ttf"), 14);

            loginRoot.setOpacity(0);
            loginRoot.setTranslateX(50);

            io.github.palexdev.materialfx.theming.UserAgentBuilder.builder()
                    .themes(io.github.palexdev.materialfx.theming.JavaFXThemes.MODENA)
                    .themes(io.github.palexdev.materialfx.theming.MaterialFXStylesheets.forAssemble(true))
                    .setDeploy(true)
                    .setResolveAssets(true)
                    .build()
                    .setGlobal();

            Scene loginScene = new Scene(loginRoot);
            loginScene.setFill(Color.TRANSPARENT);
            stage.setScene(loginScene);
            stage.centerOnScreen();

            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), loginRoot);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            javafx.animation.TranslateTransition slideIn = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(400), loginRoot);
            slideIn.setFromX(50);
            slideIn.setToX(0);

            javafx.animation.ParallelTransition parallelTransition = new javafx.animation.ParallelTransition(fadeIn, slideIn);
            parallelTransition.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void proceedToLogin() {
        try {
            Stage stage = App.primaryStage;

            FXMLLoader loginLoader = new FXMLLoader(App.class.getResource("/fxml/Login.fxml"));
            Parent loginRoot = loginLoader.load();

            javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fonts/Lora-Regular.ttf"), 14);
            javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fonts/Lora-Bold.ttf"), 14);

            loginRoot.setOpacity(0);
            loginRoot.setTranslateX(50);

            io.github.palexdev.materialfx.theming.UserAgentBuilder.builder()
                    .themes(io.github.palexdev.materialfx.theming.JavaFXThemes.MODENA)
                    .themes(io.github.palexdev.materialfx.theming.MaterialFXStylesheets.forAssemble(true))
                    .setDeploy(true)
                    .setResolveAssets(true)
                    .build()
                    .setGlobal();

            Scene loginScene = new Scene(loginRoot);
            loginScene.setFill(Color.TRANSPARENT);
            stage.setScene(loginScene);

            stage.centerOnScreen();

            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), loginRoot);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            javafx.animation.TranslateTransition slideIn = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(400), loginRoot);
            slideIn.setFromX(50);
            slideIn.setToX(0);

            javafx.animation.ParallelTransition parallelTransition = new javafx.animation.ParallelTransition(fadeIn, slideIn);
            parallelTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}