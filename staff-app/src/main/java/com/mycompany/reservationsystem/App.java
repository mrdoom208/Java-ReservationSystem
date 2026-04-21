package com.mycompany.reservationsystem;

import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.websocket.ReservationWebSocketClient;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.TimeZone;

public class App extends Application {
    public static Stage primaryStage;
    private static Scene scene;
    public String ApplicationTitle;
    public static ReservationWebSocketClient wsClient;

    public static void connectWebSocket() {
        ReservationWebSocketClient.connectToServer();
    }

    private static final Duration FADE_DURATION = Duration.millis(400);
    private static final String CONFIG_FILE = "config.properties";
    private static final String LOG_FILE = "app.log";

    private static void log(String message) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.println(message);
        } catch (IOException e) {
            System.err.println("[LOG] " + message);
        }
    }

    @Override
    public void init() throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            log("=== UNCAUGHT EXCEPTION in " + t.getName() + " ===");
            e.printStackTrace();
        });
        
        log("=== App Starting ===");
        log("Working directory: " + System.getProperty("user.dir"));
        
        loadConfigFromFile();
    }

    private void loadConfigFromFile() {
        Path configPath = Paths.get(CONFIG_FILE);
        
        if (!Files.exists(configPath)) {
            configPath = Paths.get(System.getProperty("user.home"), "ReservationSystem", CONFIG_FILE);
        }
        
        if (Files.exists(configPath)) {
            try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
                Properties props = new Properties();
                props.load(fis);
                
                setPropertyIfPresent(props, "DATABASE_URL");
                setPropertyIfPresent(props, "DB_USER");
                setPropertyIfPresent(props, "DB_PASSWORD");
                setPropertyIfPresent(props, "PHILSMS_TOKEN");
                setPropertyIfPresent(props, "PHILSMS_SENDER_ID");
                setPropertyIfPresent(props, "WEBSITE_URL");
                setPropertyIfPresent(props, "SERVER_URL");
                log("Loaded config from: " + configPath.toAbsolutePath());
            } catch (IOException e) {
                log("Error loading config: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            log("Config file not found, checking if it's in settings...");
        }
    }
    
    private void setPropertyIfPresent(Properties props, String key) {
        String value = props.getProperty(key);
        if (value != null && !value.isEmpty()) {
            System.setProperty(key, value);
        }
    }

    @Override
    public void start(Stage stage) {
        try {
            App.primaryStage = stage;
            this.ApplicationTitle = AppSettings.loadApplicationTitle();

            showSplashAndLogin(stage);

        } catch (Exception e) {
            System.err.println("=== FATAL ERROR in start() ===");
            e.printStackTrace();
        }
    }

    private void showSplashAndLogin(Stage stage) {
        log("showSplashAndLogin() called");
        try {
            FXMLLoader splashLoader = new FXMLLoader(App.class.getResource("/fxml/Splash.fxml"));
            Parent splashRoot = splashLoader.load();
            log("Splash FXML loaded successfully");

            scene = new Scene(splashRoot);
            scene.setFill(Color.TRANSPARENT);

            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(scene);
            stage.setTitle(ApplicationTitle);
            stage.centerOnScreen();
            stage.show();
            log("Splash screen displayed");

            Task<Void> switchTask = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };

            switchTask.setOnSucceeded(e -> {
                Platform.runLater(() -> {
                    try {
                        String serverUrl = AppSettings.loadServerUrl();
                        String websocketUrl = AppSettings.loadWebsocketUrl();
                        log("Server URL: " + serverUrl + ", Websocket URL: " + websocketUrl);
                        if (serverUrl == null || websocketUrl == null) {
                            log("Going to show ServerConfig");
                            showServerConfig(stage);
                        } else {
                            log("Going to show Login");
                            switchToLogin(stage);
                        }
                    } catch (Exception ex) {
                        log("=== ERROR during splash transition ===: " + ex.getMessage());
                        ex.printStackTrace();
                        showServerConfig(stage);
                    }
                });
            });

            new Thread(switchTask, "splash-delay").start();

        } catch (Exception e) {
            log("=== ERROR showing splash ===: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showServerConfig(Stage stage) {
        log("showServerConfig() called");
        try {
            FXMLLoader configLoader = new FXMLLoader(App.class.getResource("/fxml/ServerConfig.fxml"));
            Parent configRoot = configLoader.load();
            log("ServerConfig FXML loaded successfully");

            io.github.palexdev.materialfx.theming.UserAgentBuilder.builder()
                    .themes(io.github.palexdev.materialfx.theming.JavaFXThemes.MODENA)
                    .themes(io.github.palexdev.materialfx.theming.MaterialFXStylesheets.forAssemble(true))
                    .setDeploy(true)
                    .setResolveAssets(true)
                    .build()
                    .setGlobal();

            Scene configScene = new Scene(configRoot);
            configScene.setFill(Color.TRANSPARENT);
            stage.setScene(configScene);
            stage.centerOnScreen();
            log("ServerConfig scene displayed");

        } catch (Throwable e) {
            log("=== ERROR showing server config ===: " + e.getMessage());
            e.printStackTrace();
            try {
                log("Falling back to Login screen");
                switchToLogin(stage);
            } catch (Throwable fallbackError) {
                log("=== CRITICAL: Cannot show any screen ===: " + fallbackError.getMessage());
            }
        }
    }

    private void switchToLogin(Stage stage) {
        log("switchToLogin() called");
        try {
            FXMLLoader loginLoader = new FXMLLoader(App.class.getResource("/fxml/Login.fxml"));
            Parent loginRoot = loginLoader.load();
            log("Login FXML loaded successfully");

            try {
                Font.loadFont(getClass().getResourceAsStream("/fonts/Lora-Regular.ttf"), 14);
                Font.loadFont(getClass().getResourceAsStream("/fonts/Lora-Bold.ttf"), 14);
            } catch (Exception ignored) {}

            loginRoot.setOpacity(0);
            loginRoot.setTranslateX(50);

            UserAgentBuilder.builder()
                    .themes(JavaFXThemes.MODENA)
                    .themes(MaterialFXStylesheets.forAssemble(true))
                    .setDeploy(true)
                    .setResolveAssets(true)
                    .build()
                    .setGlobal();

            Scene loginScene = new Scene(loginRoot);
            loginScene.setFill(Color.TRANSPARENT);
            stage.setScene(loginScene);

            stage.centerOnScreen();
            log("Login screen displayed");

            loginRoot.styleProperty().bind(
                    Bindings.createStringBinding(() -> {
                        double referenceWidth = 1600;
                        double referenceHeight = 900;
                        double scale = Math.min(loginScene.getWidth() / referenceWidth, loginScene.getHeight() / referenceHeight);
                        double fontSize = Math.min(32, Math.max(14, 16 * scale));
                        return "-fx-font-size: " + fontSize + "px;";
                    }, loginScene.widthProperty(), loginScene.heightProperty())
            );

            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), loginRoot);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), loginRoot);
            slideIn.setFromX(50);
            slideIn.setToX(0);

            ParallelTransition parallelTransition = new ParallelTransition(fadeIn, slideIn);
            parallelTransition.play();

        } catch (Throwable e) {
            log("=== ERROR in switchToLogin() ===: " + e.getMessage());
            e.printStackTrace();
            try {
                log("Falling back to Login screen (emergency method)");
                FXMLLoader loginLoader = new FXMLLoader(App.class.getResource("/fxml/Login.fxml"));
                Parent loginRoot = loginLoader.load();
                Scene loginScene = new Scene(loginRoot);
                stage.setScene(loginScene);
                stage.centerOnScreen();
                log("Emergency login screen shown");
            } catch (Exception emergency) {
                log("=== CRITICAL: Cannot show any screen ===: " + emergency.getMessage());
            }
        }
    }

    public static void fadeOutAndSwitch(Parent currentRoot, Parent newRoot, Stage stage, Runnable onComplete) {
        FadeTransition fadeOut = new FadeTransition(FADE_DURATION, currentRoot);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            Scene newScene = new Scene(newRoot);
            newScene.setFill(Color.TRANSPARENT);
            newRoot.setOpacity(0);

            newRoot.styleProperty().bind(
                    Bindings.createStringBinding(() -> {
                        double referenceWidth = 1600;
                        double referenceHeight = 900;
                        double scale = Math.min(newScene.getWidth() / referenceWidth, newScene.getHeight() / referenceHeight);
                        double fontSize = Math.min(32, Math.max(14, 16 * scale));
                        return "-fx-font-size: " + fontSize + "px;";
                    }, newScene.widthProperty(), newScene.heightProperty())
            );

            stage.setScene(newScene);

            FadeTransition fadeIn = new FadeTransition(FADE_DURATION, newRoot);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setOnFinished(event -> {
                if (onComplete != null) onComplete.run();
            });
            fadeIn.play();
        });
        fadeOut.play();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (wsClient != null) {
            ReservationWebSocketClient.disconnect();
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+08:00"));
        System.setProperty("prism.lcdtext", "true");
        System.setProperty("prism.text", "t2k");
        launch(args);
    }
}