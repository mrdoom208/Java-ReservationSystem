package com.mycompany.reservationsystem.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class PaneAnimationUtil {

    public static final Duration FAST = Duration.millis(150);
    public static final Duration NORMAL = Duration.millis(250);
    public static final Duration SLOW = Duration.millis(400);

    /**
     * Animate pane IN (show/appear)
     */
    public static void animateIn(Node pane, Runnable onFinished) {
        animateIn(pane, NORMAL, onFinished);
    }

    public static void animateIn(Node pane, Duration duration, Runnable onFinished) {
        pane.setOpacity(0);
        pane.setScaleX(0.9);
        pane.setScaleY(0.9);

        ParallelTransition parallel = new ParallelTransition(
                createFadeTransition(pane, 0, 1, duration),
                createScaleTransition(pane, 0.9, 1.0, duration)
        );

        parallel.setOnFinished(e -> {
            pane.setOpacity(1);
            pane.setScaleX(1.0);
            pane.setScaleY(1.0);
            if (onFinished != null) onFinished.run();
        });

        parallel.play();
    }

    /**
     * Animate pane OUT (hide/disappear)
     */
    public static void animateOut(Node pane, Runnable onFinished) {
        animateOut(pane, NORMAL, onFinished);
    }

    public static void animateOut(Node pane, Duration duration, Runnable onFinished) {
        ParallelTransition parallel = new ParallelTransition(
                createFadeTransition(pane, 1, 0, duration),
                createScaleTransition(pane, 1.0, 0.9, duration)
        );

        parallel.setOnFinished(e -> {
            pane.setOpacity(0);
            pane.setScaleX(0.9);
            pane.setScaleY(0.9);
            if (onFinished != null) onFinished.run();
        });

        parallel.play();
    }

    /**
     * Slide in from RIGHT (perfect for navigation)
     */
    public static void slideInFromRight(Node pane, Runnable onFinished) {
        slideInFromRight(pane, NORMAL, onFinished);
    }

    public static void slideInFromRight(Node pane, Duration duration, Runnable onFinished) {
        pane.setTranslateX(pane.getLayoutBounds().getWidth());
        pane.setOpacity(0);

        ParallelTransition parallel = new ParallelTransition(
                createTranslateTransition(pane, pane.getLayoutBounds().getWidth(), 0, duration),
                createFadeTransition(pane, 0, 1, duration)
        );

        parallel.setOnFinished(e -> {
            pane.setTranslateX(0);
            pane.setOpacity(1);
            if (onFinished != null) onFinished.run();
        });

        parallel.play();
    }

    /**
     * Slide out to LEFT (perfect for navigation back)
     */
    public static void slideOutToLeft(Node pane, Runnable onFinished) {
        slideOutToLeft(pane, NORMAL, onFinished);
    }

    public static void slideOutToLeft(Node pane, Duration duration, Runnable onFinished) {
        ParallelTransition parallel = new ParallelTransition(
                createTranslateTransition(pane, 0, -pane.getLayoutBounds().getWidth(), duration),
                createFadeTransition(pane, 1, 0, duration)
        );

        parallel.setOnFinished(e -> {
            pane.setTranslateX(0);
            pane.setOpacity(0);
            if (onFinished != null) onFinished.run();
        });

        parallel.play();
    }

    /**
     * Sequential: OUT then IN (perfect for pane switching)
     */
    public static void switchPanes(Node outPane, Node inPane, Runnable onComplete) {
        switchPanes(outPane, inPane, NORMAL, onComplete);
    }

    public static void switchPanes(Node outPane, Node inPane, Duration duration, Runnable onComplete) {
        animateOut(outPane, duration, () -> animateIn(inPane, duration, onComplete));
    }

    /**
     * Dialog open/close animations
     */
    public static void animateDialogOpen(Node dialogRoot, Runnable onFinished) {
        animateIn(dialogRoot, FAST, onFinished);
    }

    public static void animateDialogClose(Node dialogRoot, Runnable onFinished) {
        animateOut(dialogRoot, FAST, onFinished);
    }

    // === HELPER METHODS ===
    private static FadeTransition createFadeTransition(Node node, double from, double to, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(from);
        fade.setToValue(to);
        fade.setInterpolator(Interpolator.EASE_BOTH);
        return fade;
    }

    private static ScaleTransition createScaleTransition(Node node, double fromScale, double toScale, Duration duration) {
        ScaleTransition scale = new ScaleTransition(duration, node);
        scale.setFromX(fromScale);
        scale.setFromY(fromScale);
        scale.setToX(toScale);
        scale.setToY(toScale);
        scale.setInterpolator(Interpolator.EASE_BOTH);
        return scale;
    }

    private static TranslateTransition createTranslateTransition(Node node, double from, double to, Duration duration) {
        TranslateTransition translate = new TranslateTransition(duration, node);
        translate.setFromX(from);
        translate.setToX(to);
        translate.setInterpolator(Interpolator.EASE_BOTH);
        return translate;
    }
}
