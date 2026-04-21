/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.controller;
import com.mycompany.reservationsystem.App;
import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.controller.main.AdministratorUIController;
import com.mycompany.reservationsystem.model.ActivityLog;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.service.ActivityLogService;
import com.mycompany.reservationsystem.transition.LoginTransition;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Map;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import com.mycompany.reservationsystem.controller.popup.ConnectionFailedController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import javafx.animation.PauseTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 *
 * @author formentera
 */

public class LoginController {
    
    @FXML
    private Button Staff,Admin,Submit,activeButton,closebtn;
    @FXML
    private Hyperlink serverConfigBtn;
    @FXML
    private MFXTextField usernamefield;
    @FXML
    private MFXPasswordField passwordfield;
    @FXML
    private Label messageLabel;
    @FXML
    private StackPane dragArea;
    @FXML
    private ImageView loginLogo;
    @FXML
    private Text helloText;
    @FXML
    private Text welcomeText;
    @FXML
    private VBox welcomeTextContainer;
    @FXML
    private VBox titleContainer;
    @FXML
    private VBox inputContainer;
    @FXML
    private VBox loginFormContainer;
    @FXML
    private Label systemLabel;
    @FXML
    private Label loginLabel;

    private boolean isLoggingIn = false;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    private LoginTransition.LoginButtonAnimator buttonAnimator;
    private double xOffset = 0;
    private double yOffset = 0;

    public void Colorchange(ActionEvent event){
        Button clicked = (Button) event.getSource();
        Button buttons[] = {Admin, Staff}; 
        
        for (Button btn : buttons) {
            if (btn == null) continue;
            btn.getStyleClass().remove("login-button-active");
            if (!btn.getStyleClass().contains("login-button")) {
                btn.getStyleClass().add("login-button");
            }   
        }

        clicked.getStyleClass().remove("login-button");
        if (!clicked.getStyleClass().contains("login-button-active")) {
            clicked.getStyleClass().add("login-button-active");
        }
    }

    @FXML
    private void initialize() {
        try {
            animateLoginElements();
            if (Submit != null) {
                Submit.setDefaultButton(true);
                buttonAnimator = new LoginTransition.LoginButtonAnimator(Submit);
            }
            if (dragArea != null) {
                dragArea.setOnMousePressed(event -> {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                });

                dragArea.setOnMouseDragged(event -> {
                    if (App.primaryStage != null) {
                        App.primaryStage.setX(event.getScreenX() - xOffset);
                        App.primaryStage.setY(event.getScreenY() - yOffset);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error in LoginController.initialize(): " + e.getMessage());
        }
    }

    private void animateLoginElements() {
        try {
            if (loginLogo != null) {
                LoginTransition.animateElement(loginLogo, 100);
            }
            if (helloText != null) {
                LoginTransition.animateElement(helloText, 200);
            }
            if (welcomeText != null) {
                LoginTransition.animateElement(welcomeText, 300);
            }
            if (titleContainer != null) {
                LoginTransition.animateTitle(titleContainer, 400);
            }
            if (inputContainer != null) {
                LoginTransition.animateElement(inputContainer, 600);
            }
            if (Submit != null) {
                LoginTransition.animateButton(Submit, 800);
            }
        } catch (Exception e) {
            System.err.println("Animation error: " + e.getMessage());
        }
    }
    public void closeApp(ActionEvent event) {
        Platform.exit();     // cleanly shuts down JavaFX
        System.exit(0);      // ensures JVM exits
    }

    @FXML
    private void openServerConfig() {
        try {
            FXMLLoader configLoader = new FXMLLoader(App.class.getResource("/fxml/ServerConfig.fxml"));
            Parent configRoot = configLoader.load();

            io.github.palexdev.materialfx.theming.UserAgentBuilder.builder()
                    .themes(io.github.palexdev.materialfx.theming.JavaFXThemes.MODENA)
                    .themes(io.github.palexdev.materialfx.theming.MaterialFXStylesheets.forAssemble(true))
                    .setDeploy(true)
                    .setResolveAssets(true)
                    .build()
                    .setGlobal();

            Scene configScene = new Scene(configRoot);
            configScene.setFill(Color.TRANSPARENT);
            
            if (App.primaryStage != null) {
                App.primaryStage.setScene(configScene);
                App.primaryStage.centerOnScreen();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
        try {
            java.net.URL url = new java.net.URL("https://www.google.com");
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return (200 <= responseCode && responseCode < 400);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isServerReachable() {
        try {
            String serverUrl = AppSettings.loadServerUrl();
            if (serverUrl == null || serverUrl.isEmpty()) {
                serverUrl = System.getenv("SERVER_URL");
            }
            if (serverUrl == null || serverUrl.isEmpty()) {
                serverUrl = "localhost:13472";
            }
            
            if (!serverUrl.startsWith("http")) {
                serverUrl = "http://" + serverUrl;
            }
            
            java.net.URL url = new java.net.URL(serverUrl + "/api/health");
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return (200 <= responseCode && responseCode < 400);
        } catch (Exception e) {
            return false;
        }
    }

    private String getConnectionErrorMessage() {
        if (!isNetworkAvailable()) {
            return "Login failed. Please check your Internet connection";

        } else if (!isServerReachable()) {
            return "Login failed. Server is down";
        }
        return null;
    }

    @FXML
    public void SubmitButton(ActionEvent event) {
        if (isLoggingIn) return;
        
        String userf = usernamefield.getText();
        String passf = passwordfield.getText();

        if (userf.isEmpty() || passf.isEmpty()) {
            showError("Please enter username and password");
            return;
        }

        isLoggingIn = true;
        retryCount = 0;
        
        if (buttonAnimator != null) {
            buttonAnimator.startLoading();
        }
        performLogin(event, userf, passf);
    }

    private void performLogin(ActionEvent event, String userf, String passf) {
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                String connectionError = getConnectionErrorMessage();
                if (connectionError != null) {
                    throw new IOException(connectionError);
                }
                
                Map<String, Object> response = ApiClient.login(userf, passf);
                
                if (response != null && response.containsKey("error")) {
                    String error = (String) response.get("error");
                    if (error != null && (error.toLowerCase().contains("connection") || 
                        error.toLowerCase().contains("timeout") ||
                        error.toLowerCase().contains("unreachable"))) {
                        throw new IOException("Connection error: " + error);
                    }
                }
                
                if (response != null && response.containsKey("success") && Boolean.TRUE.equals(response.get("success"))) {
                    User user = new User();
                    user.setId(response.get("userId") != null ? ((Number) response.get("userId")).longValue() : null);
                    user.setUsername((String) response.get("username"));
                    user.setFirstname((String) response.get("firstname"));
                    user.setLastname((String) response.get("lastname"));
                    user.setPosition(User.Position.valueOf((String) response.get("position")));
                    user.setStatus("ACTIVE");
                    return user;
                } else if (response != null && response.containsKey("message")) {
                    return null;
                }
                return null;
            }
        };

        loginTask.setOnSucceeded(e -> {
            User found = loginTask.getValue();
            if (found == null) {
                buttonAnimator.startErrorAnimation("Wrong Username or Password", () -> {
                    showError("Wrong Username or Password");
                    isLoggingIn = false;
                });
                return;
            }

            showSuccess("Login Successfully, Welcome Back!");
            ActivityLogService.logAction(found.getUsername(), found.getPosition().name(), "Authentication", "Login", "User logged in");
            App.connectWebSocket();

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/main/AdministratorUI.fxml")
                );
                Parent root = loader.load();

                AdministratorUIController adminController = loader.getController();
                adminController.setUser(found);

                Stage oldStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene oldScene = oldStage.getScene();
                
                root.setOpacity(0);
                root.setScaleX(0.95);
                root.setScaleY(0.95);

                Stage newStage = new Stage();
                App.primaryStage = newStage;
                newStage.setTitle(AppSettings.loadApplicationTitle());
                Scene newScene = new Scene(root);
                newScene.setFill(Color.TRANSPARENT);
                root.setStyle("-fx-background-color: #1e1e1e;");
                root.styleProperty().bind(
                        Bindings.createStringBinding(() -> {
                            double referenceWidth = 1600;
                            double referenceHeight = 900;
                            double scale = Math.min(newScene.getWidth() / referenceWidth, newScene.getHeight() / referenceHeight);
                            double fontSize = Math.min(32, Math.max(14, 16 * scale));
                            return "-fx-font-size: " + fontSize + "px;";
                        }, newScene.widthProperty(), newScene.heightProperty())
                );
                newStage.setScene(newScene);
                newStage.initStyle(StageStyle.TRANSPARENT);
                newStage.setMaximized(true);
                newStage.show();

                FadeTransition fadeOutLogin = new FadeTransition(Duration.millis(100), oldScene.getRoot());
                fadeOutLogin.setFromValue(1.0);
                fadeOutLogin.setToValue(0.0);

                FadeTransition fadeInAdmin = new FadeTransition(Duration.millis(150), root);
                fadeInAdmin.setFromValue(0.0);
                fadeInAdmin.setToValue(1.0);

                ScaleTransition scaleInAdmin = new ScaleTransition(Duration.millis(150), root);
                scaleInAdmin.setFromX(0.95);
                scaleInAdmin.setFromY(0.95);
                scaleInAdmin.setToX(1.0);
                scaleInAdmin.setToY(1.0);

                SequentialTransition transition = new SequentialTransition(fadeOutLogin);
                transition.setOnFinished(evt -> {
                    oldStage.close();
                    fadeInAdmin.play();
                    scaleInAdmin.play();
                });
                transition.play();

            } catch (IOException ex) {
                ex.printStackTrace();
                showError("Failed to load dashboard");
            }
        });

        loginTask.setOnFailed(e -> {
            buttonAnimator.startErrorAnimation("Login failed", () -> {
                handleLoginFailure(event, userf, passf);
            });
        });

        new Thread(loginTask, "login-task").start();
    }

    private void handleLoginFailure(ActionEvent event, String userf, String passf) {
        String errorMsg = "Login failed";
        
        if (isNetworkConnectionError(errorMsg)) {
            retryCount++;
            if (retryCount < MAX_RETRIES) {
                showError("Connection lost. Retrying (" + retryCount + "/" + MAX_RETRIES + ")...");
                PauseTransition delay = new PauseTransition(javafx.util.Duration.seconds(2));
                delay.setOnFinished(evt -> performLogin(event, userf, passf));
                delay.play();
            } else {
                showConnectionFailedPopup(event, userf, passf);
            }
        } else {

            showError("Login failed. Please check your Internet connection");
            isLoggingIn = false;
        }
    }

    private void showConnectionFailedPopup(ActionEvent event, String userf, String passf) {
        isLoggingIn = false;
        retryCount = 0;
        
        ConnectionFailedController.show(
            () -> {
                retryCount = 0;
                isLoggingIn = true;
                performLogin(event, userf, passf);
            },
            () -> {
                Platform.exit();
                System.exit(0);
            }
        );
    }

    private boolean isNetworkConnectionError(String message) {
        if (message == null) return false;
        String lower = message.toLowerCase();
        return lower.contains("connection")
            || lower.contains("timeout")
            || lower.contains("network")
            || lower.contains("unreachable")
            || lower.contains("io error")
            || lower.contains("socket");
    }

    private void showRetryOption() {
        passwordfield.setText("");
        usernamefield.requestFocus();
    }

    private void showError(String message) {
        messageLabel.getStyleClass().removeAll("login-success", "login-message-hidden");
        messageLabel.getStyleClass().add("login-error");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.getStyleClass().removeAll("login-error", "login-message-hidden");
        messageLabel.getStyleClass().add("login-success");
        messageLabel.setText(message);
    }
    
    private void showConnectionError(String message) {
        messageLabel.getStyleClass().removeAll("login-success", "login-message-hidden");
        messageLabel.getStyleClass().add("login-error");
        messageLabel.setText(message);
    }
}
