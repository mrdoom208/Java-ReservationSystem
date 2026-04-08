package com.mycompany.reservationsystem.util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class NotificationManager {

    private static VBox container;

    // Set the VBox container from your controller
    public static void setContainer(VBox notificationArea) {
        container = notificationArea;
    }
    public enum NotificationType {
        SUCCESS,
        ERROR,
        CHANGE,
        INFO,
        WARNING,
        CONFIRM,
        NO_SHOW

    }

    // Show notification
    public static void show(String title, String message, NotificationType type) {
        if(container == null) return;
        
        try {
            javafx.application.Platform.runLater(() -> {
                doShow(title, message, type);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void doShow(String title, String message, NotificationType type) {
        if(container == null) return;
        
        try {
            HBox root = new HBox(15);
            root.getStyleClass().add("notification-root");

            StackPane iconCircle = new StackPane();
            iconCircle.getStyleClass().add("icon-circle");

            Label icon = new Label();
            switch (type) {
                case SUCCESS -> {
                    root.getStyleClass().add("success-border");
                    iconCircle.getStyleClass().add("success-icon");
                    icon.setText("✓");
                }
                case ERROR -> {
                    root.getStyleClass().add("error-border");
                    iconCircle.getStyleClass().add("error-icon");
                    icon.setText("✕");
                }
                case CHANGE -> {
                    root.getStyleClass().add("change-border");
                    iconCircle.getStyleClass().add("change-icon");
                    icon.setText("↻");
                }
                case INFO -> {
                    root.getStyleClass().add("info-border");
                    iconCircle.getStyleClass().add("info-icon");
                    icon.setText("ℹ");
                }
                case WARNING -> {
                    root.getStyleClass().add("warning-border");
                    iconCircle.getStyleClass().add("warning-icon");
                    icon.setText("⚠");
                }
                case CONFIRM -> {
                    root.getStyleClass().add("confirm-border");
                    iconCircle.getStyleClass().add("confirm-icon");
                    icon.setText("✔");
                }
                case NO_SHOW -> {
                    root.getStyleClass().add("noshow-border");
                    iconCircle.getStyleClass().add("noshow-icon");
                    icon.setText("⛔");
                }
            }
            icon.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
            iconCircle.getChildren().add(icon);

            VBox texts = new VBox(5);
            Label t = new Label(title);
            t.getStyleClass().add("notification-title");
            Label m = new Label(message);
            m.getStyleClass().add("notification-message");
            texts.getChildren().addAll(t, m);

            root.getChildren().addAll(iconCircle, texts);

            if(container.getChildren().size() >= 5) {
                container.getChildren().remove(container.getChildren().size() - 1);
            }

            container.getChildren().add(0, root);
            root.setMaxWidth(Double.MAX_VALUE);
            root.prefWidthProperty().bind(container.widthProperty());

            root.setOpacity(0);
            root.setTranslateY(-20);

            Timeline showAnim = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(root.opacityProperty(), 0),
                            new KeyValue(root.translateYProperty(), -20)
                    ),
                    new KeyFrame(Duration.millis(250),
                            new KeyValue(root.opacityProperty(), 1),
                            new KeyValue(root.translateYProperty(), 0)
                    )
            );

            Timeline wait = new Timeline(new KeyFrame(Duration.seconds(5)));

            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.ZERO),
                    new KeyFrame(Duration.millis(250),
                            new KeyValue(root.opacityProperty(), 0),
                            new KeyValue(root.translateYProperty(), -20)
                    )
            );
            wait.setOnFinished(e -> fadeOut.play());
            fadeOut.setOnFinished(e -> {
                try {
                    container.getChildren().remove(root);
                } catch (Exception ex) {
                    // Ignore if already removed
                }
            });

            showAnim.play();
            wait.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
