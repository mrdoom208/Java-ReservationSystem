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
import java.io.InputStream;
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
    @FXML
    private io.github.palexdev.materialfx.controls.MFXButton closebtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            String savedServerUrl = AppSettings.loadServerUrl();
            String savedWebsocketUrl = AppSettings.loadWebsocketUrl();

            if (savedServerUrl != null && !savedServerUrl.isEmpty() && serverUrlField != null) {
                serverUrlField.setText(savedServerUrl);
            }
            if (savedWebsocketUrl != null && !savedWebsocketUrl.isEmpty() && websocketUrlField != null) {
                websocketUrlField.setText(savedWebsocketUrl);
            }
        } catch (Exception e) {
            System.err.println("Error initializing ServerConfigController: " + e.getMessage());
        }
    }

    @FXML
    private void saveConfig() {
        try {
            if (serverUrlField == null || websocketUrlField == null) {
                showError("UI not loaded properly");
                return;
            }
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

            saveBtn.setDisable(true);
            saveBtn.setText("Saving...");

            AppSettings.saveServerUrl(serverUrl);
            AppSettings.saveWebsocketUrl(websocketUrl);

            System.setProperty("SERVER_URL", serverUrl);
            System.setProperty("WEBSOCKET_URL", websocketUrl);

            fadeOutAndProceed();
        } catch (Exception e) {
            showError("Error saving config: " + e.getMessage());
            saveBtn.setDisable(false);
            saveBtn.setText("Save");
        }
    }

    private void fadeOutAndProceed() {
        try {
            javafx.scene.Parent root = serverUrlField.getScene().getRoot();
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(200), root);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> proceedToLogin());
            fadeOut.play();
        } catch (Exception e) {
            proceedToLogin();
        }
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
        try {
            if (serverUrlField == null || testConnectionBtn == null || messageLabel == null) {
                return;
            }
            String serverUrl = serverUrlField.getText().trim();

            if (serverUrl.isEmpty()) {
                showError("Enter server URL first");
                return;
            }

            testConnectionBtn.setDisable(true);
            messageLabel.setText("Testing connection...");
            messageLabel.setStyle("-fx-text-fill: #3498DB;");

            String wsUrl = websocketUrlField != null ? websocketUrlField.getText().trim() : "";
            String userDir = System.getProperty("user.home");
            java.io.File debugFile = new java.io.File(userDir, "server_connection_debug.txt");
            try {
                java.io.FileWriter fw = new java.io.FileWriter(debugFile, false);
                java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
                bw.write("=== SERVER CONNECTION TEST ===");
                bw.newLine();
                bw.write("Time: " + java.time.LocalDateTime.now());
                bw.newLine();
                bw.write("Server URL: " + serverUrl);
                bw.newLine();
                bw.write("WebSocket URL: " + wsUrl);
                bw.newLine();
                bw.write("===========================");
                bw.newLine();
                bw.close();
            } catch (Exception ex) {
                System.err.println("Debug init error: " + ex.getMessage());
            }

        new Thread(() -> {
            String result = "error";
            int statusCode = -1;

            try {
                String testUrl = serverUrl.trim();
                if (testUrl.endsWith("/api")) {
                    testUrl = testUrl + "/health";
                } else if (!testUrl.endsWith("/api/health") && !testUrl.endsWith("/health")) {
                    if (testUrl.endsWith("/")) {
                        testUrl = testUrl + "api/health";
                    } else {
                        testUrl = testUrl + "/api/health";
                    }
                }
                
                writeDebugInfo("Testing connection to: " + testUrl);
                
                // Force TLS 1.2
                System.setProperty("https.protocols", "TLSv1.2");
                System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
                System.setProperty("crypto.policy", "unlimited");
                writeDebugInfo("TLS properties set");
                
                java.net.URL url = new java.net.URI(testUrl).toURL();
                java.net.URLConnection urlConn = url.openConnection();
                
                if (urlConn instanceof javax.net.ssl.HttpsURLConnection) {
                    javax.net.ssl.HttpsURLConnection httpsConn = (javax.net.ssl.HttpsURLConnection) urlConn;
                    httpsConn.setConnectTimeout(15000);
                    httpsConn.setReadTimeout(15000);
                    httpsConn.setRequestProperty("Connection", "close");
                    httpsConn.setRequestProperty("User-Agent", "ReservationSystem/1.0");
                    
                    // Set SNI hostname
                    httpsConn.setHostnameVerifier((hostname, session) -> {
                        writeDebugInfo("SNI: " + hostname);
                        return true;
                    });
                    
                    // Force TLS 1.2
                    System.setProperty("https.protocols", "TLSv1.2");
                    System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
                    
                    javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{
                        new javax.net.ssl.X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                        }
                    };
                    
                    javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLSv1.2");
                    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                    httpsConn.setSSLSocketFactory(sslContext.getSocketFactory());
                    
writeDebugInfo("Using TLSv1.2 with trust all");
                    
                    int code = httpsConn.getResponseCode();
                    statusCode = code;
                    writeDebugInfo("Using TLSv1.2 with trust all");
                    
                    try {
                        int responseCode = httpsConn.getResponseCode();
                        statusCode = responseCode;
                        writeDebugInfo("HTTP Response: " + responseCode);
                        
                        if (statusCode >= 200 && statusCode < 300) {
                            result = "success";
                        } else if (statusCode == 401 || statusCode == 403) {
                            result = "unauthorized";
                        } else {
                            result = "failed:" + statusCode;
                        }
                    } catch (Exception e) {
                        writeDebugInfo("Exception: " + e.getClass().getName() + " - " + e.getMessage());
                        throw e;
                    }
                }
            } catch (java.net.SocketTimeoutException e) {
                result = "timeout";
                writeDebugInfo("HTTP Error: Connection timed out", "Exception: " + e.getClass().getName(), "Message: " + e.getMessage());
            } catch (java.net.ConnectException e) {
                result = "timeout";
                writeDebugInfo("HTTP Error: Connection failed", "Exception: " + e.getClass().getName(), "Message: " + e.getMessage());
            } catch (Exception e) {
                result = "error:" + e.getMessage();
                StringBuilder fullError = new StringBuilder();
                fullError.append("HTTP Error: ").append(e.getMessage()).append("\n");
                java.io.StringWriter sw = new java.io.StringWriter();
                java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                e.printStackTrace(pw);
                fullError.append("Stack Trace:\n").append(sw.toString());
                writeDebugInfo(fullError.toString());
            }

            final String finalResult = result;
            final int finalStatusCode = statusCode;

            javafx.application.Platform.runLater(() -> {
                testConnectionBtn.setDisable(false);

                if (finalResult.equals("success")) {
                    messageLabel.setText("Connection successful!");
                    messageLabel.setStyle("-fx-text-fill: #2ECC71;");
                } else if (finalResult.equals("unauthorized")) {
                    messageLabel.setText("Server reachable (auth required)");
                    messageLabel.setStyle("-fx-text-fill: #F39C12;");
                } else if (finalResult.equals("timeout")) {
                    showError("Connection timed out");
                } else if (finalResult.startsWith("failed:")) {
                    showError("Connection failed: HTTP " + finalStatusCode);
                } else if (finalResult.startsWith("error:")) {
                    showError("Connection failed: " + finalResult.substring(6));
                    messageLabel.setText("Error: See " + System.getProperty("user.home") + "/server_connection_debug.txt");
                }
            });
        }).start();
        } catch (Exception e) {
            showError("Connection test error: " + e.getMessage());
        }
    }
    
    private void writeDebugInfo(String... lines) {
        try {
            String userDir = System.getProperty("user.home");
            java.io.File debugFile = new java.io.File(userDir, "server_connection_debug.txt");
            java.io.FileWriter fw = new java.io.FileWriter(debugFile, true);
            java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            for (String line : lines) {
                bw.write("[" + now + "] " + line);
                bw.newLine();
            }
            bw.close();
        } catch (Exception e) {
            System.err.println("Debug write error: " + e.getMessage());
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

            try { javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fonts/Lora-Regular.ttf"), 14); } catch (Exception e) {}
            try { javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fonts/Lora-Bold.ttf"), 14); } catch (Exception e) {}

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

try { javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fonts/Lora-Regular.ttf"), 14); } catch (Exception e) {}
            try { javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fonts/Lora-Bold.ttf"), 14); } catch (Exception e) {}

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