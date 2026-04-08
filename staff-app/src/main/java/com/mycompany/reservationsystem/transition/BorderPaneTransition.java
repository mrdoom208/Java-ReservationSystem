package com.mycompany.reservationsystem.transition;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

public final class BorderPaneTransition {

    private static final Duration ANIM_DURATION = Duration.millis(220);

    private BorderPaneTransition() {
        // Utility class – prevent instantiation
    }

    /* ---------------- ANIMATE OUT ---------------- */

    public static void animateOut(BorderPane pane, Runnable onFinished) {

        if (pane == null) return;

        FadeTransition fadeOut = new FadeTransition(ANIM_DURATION, pane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition slideOut = new TranslateTransition(ANIM_DURATION, pane);
        slideOut.setFromX(0);
        slideOut.setToX(-20);

        ParallelTransition out = new ParallelTransition(fadeOut, slideOut);
        out.setOnFinished(e -> {
            pane.setVisible(false);
            pane.setManaged(false);
            pane.setTranslateX(0);

            if (onFinished != null) {
                onFinished.run();
            }
        });

        out.play();
    }

    /* ---------------- ANIMATE IN ---------------- */

    public static void animateIn(BorderPane pane) {

        if (pane == null) return;

        pane.setManaged(true);
        pane.setVisible(true);
        pane.setOpacity(0);
        pane.setTranslateX(30);

        FadeTransition fadeIn = new FadeTransition(ANIM_DURATION, pane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(ANIM_DURATION, pane);
        slideIn.setFromX(30);
        slideIn.setToX(0);

        new ParallelTransition(fadeIn, slideIn).play();
    }

}
