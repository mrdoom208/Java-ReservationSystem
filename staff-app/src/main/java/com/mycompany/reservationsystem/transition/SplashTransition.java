package com.mycompany.reservationsystem.transition;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class SplashTransition {

    public static void animateSplash(Node root) {
        root.setOpacity(0);
        root.setScaleX(0.8);
        root.setScaleY(0.8);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(600), root);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition entrance = new ParallelTransition(fadeIn, scaleIn);
        entrance.play();
    }

    public static void animateLogo(ImageView logo) {
        logo.setOpacity(0);
        logo.setScaleX(0.5);
        logo.setScaleY(0.5);

        FadeTransition fade = new FadeTransition(Duration.millis(800), logo);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(800), logo);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pulse = new ParallelTransition(fade, scale);
        pulse.play();

        ScaleTransition breathe = new ScaleTransition(Duration.millis(1500), logo);
        breathe.setFromX(1);
        breathe.setFromY(1);
        breathe.setToX(1.05);
        breathe.setToY(1.05);
        breathe.setAutoReverse(true);
        breathe.setCycleCount(Animation.INDEFINITE);
        breathe.setInterpolator(Interpolator.EASE_IN);
        breathe.play();
    }

    public static void animateText(Text text, int delayMs) {
        text.setOpacity(0);
        text.setTranslateY(20);

        FadeTransition fade = new FadeTransition(Duration.millis(500), text);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delayMs));

        TranslateTransition slide = new TranslateTransition(Duration.millis(500), text);
        slide.setFromY(20);
        slide.setToY(0);
        slide.setDelay(Duration.millis(delayMs));

        ParallelTransition animate = new ParallelTransition(fade, slide);
        animate.play();
    }

    public static void animateElement(Node node, int delayMs) {
        node.setOpacity(0);
        node.setTranslateY(20);

        FadeTransition fade = new FadeTransition(Duration.millis(500), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delayMs));

        TranslateTransition slide = new TranslateTransition(Duration.millis(500), node);
        slide.setFromY(20);
        slide.setToY(0);
        slide.setDelay(Duration.millis(delayMs));

        ParallelTransition animate = new ParallelTransition(fade, slide);
        animate.play();
    }

    public static void fadeOut(Node node, Runnable onComplete) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), node);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            if (onComplete != null) onComplete.run();
        });
        fadeOut.play();
    }
}
