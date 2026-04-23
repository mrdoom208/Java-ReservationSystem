package com.mycompany.reservationsystem.transition;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class LoginTransition {

    public static final Duration ANIMATION_DURATION = Duration.millis(400);

    public static void animateLogin(Node root) {
        root.setOpacity(0);
        root.setTranslateX(50);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), root);
        slideIn.setFromX(50);
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition transition = new ParallelTransition(fadeIn, slideIn);
        transition.play();
    }

    public static void animateElements(Node[] elements) {
        int delay = 100;
        for (Node node : elements) {
            animateElement(node, delay);
            delay += 80;
        }
    }

    public static void animateElement(Node node, int delayMs) {
        node.setOpacity(0);
        node.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(400), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delayMs));

        TranslateTransition slide = new TranslateTransition(Duration.millis(400), node);
        slide.setFromY(30);
        slide.setToY(0);
        slide.setDelay(Duration.millis(delayMs));
        slide.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition animate = new ParallelTransition(fade, slide);
        animate.play();
    }

    public static void animateTitle(Node title, int delayMs) {
        title.setOpacity(0);
        title.setTranslateX(-50);

        FadeTransition fade = new FadeTransition(Duration.millis(600), title);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delayMs));

        TranslateTransition slide = new TranslateTransition(Duration.millis(600), title);
        slide.setFromX(-50);
        slide.setToX(0);
        slide.setDelay(Duration.millis(delayMs));
        slide.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition animate = new ParallelTransition(fade, slide);
        animate.play();
    }

    public static void animateButton(Button button, int delayMs) {
        button.setOpacity(0);
        button.setScaleX(0.8);
        button.setScaleY(0.8);

        FadeTransition fade = new FadeTransition(Duration.millis(400), button);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delayMs));

        ScaleTransition scale = new ScaleTransition(Duration.millis(400), button);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);
        scale.setDelay(Duration.millis(delayMs));
        scale.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition animate = new ParallelTransition(fade, scale);
        animate.play();
    }

    public static void pulseEffect(Node node) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(200), node);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.setInterpolator(Interpolator.EASE_IN);
        pulse.play();
    }

    public static class LoginButtonAnimator {
        private final Button button;
        private String originalText;
        private Region originalGraphic;
        private HBox loadingContainer;
        private ProgressIndicator spinner;
        private boolean isAnimating = false;

        public LoginButtonAnimator(Button button) {
            this.button = button;
        }

        public void startLoading() {
            if (isAnimating) return;
            isAnimating = true;

            originalText = button.getText();
            originalGraphic = (Region) button.getGraphic();
            button.setDisable(true);

            createLoadingAnimation();
            button.setText("");
            button.setGraphic(loadingContainer);
        }

        public void updateLoadingText(String newText) {
            if (loadingContainer != null && loadingContainer.getChildren().get(0) instanceof Text) {
                ((Text) loadingContainer.getChildren().get(0)).setText(newText);
            }
        }

        private void createLoadingAnimation() {
            spinner = new ProgressIndicator();
            spinner.setMaxSize(16, 16);
            spinner.setStyle("-fx-progress-color: white;");
            
            loadingContainer = new HBox();
            loadingContainer.setAlignment(Pos.CENTER);
            loadingContainer.setSpacing(10);

            Text loadingText = new Text("Signing in");
            loadingText.setFill(Color.WHITE);
            loadingText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            loadingContainer.getChildren().addAll(loadingText, spinner);
        }

        public void startSuccessAnimation(Runnable onComplete) {
            if (!isAnimating) {
                if (onComplete != null) onComplete.run();
                return;
            }

            Text checkmark = new Text("\u2713");
            checkmark.setFill(Color.WHITE);
            checkmark.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            button.setText("");
            button.setGraphic(checkmark);
            button.setStyle("-fx-background-color: #4CAF50;");

            ScaleTransition pop = new ScaleTransition(Duration.millis(200), button);
            pop.setFromX(0.9);
            pop.setToX(1);
            pop.setFromY(0.9);
            pop.setToY(1);
            pop.setInterpolator(Interpolator.EASE_OUT);

            pop.setOnFinished(e -> {
                button.setDisable(false);
                if (onComplete != null) onComplete.run();
            });
            pop.play();
        }

        public void startErrorAnimation(String errorText, Runnable onComplete) {
            if (!isAnimating) {
                if (onComplete != null) onComplete.run();
                return;
            }
            isAnimating = false;

            TranslateTransition shake = new TranslateTransition(Duration.millis(60), button);
            shake.setFromX(0);
            shake.setToX(-8);
            shake.setAutoReverse(true);
            shake.setCycleCount(3);
            shake.setInterpolator(Interpolator.EASE_IN);
            shake.setOnFinished(e -> {
                button.setDisable(false);
                button.setText(originalText);
                button.setGraphic(originalGraphic);
                button.setStyle("");
                if (onComplete != null) onComplete.run();
            });
            shake.play();
        }

        public void reset() {
            isAnimating = false;
            button.setText(originalText != null ? originalText : "Login");
            button.setGraphic(originalGraphic);
            button.setStyle("");
        }

        public boolean isAnimating() {
            return isAnimating;
        }
    }
}
