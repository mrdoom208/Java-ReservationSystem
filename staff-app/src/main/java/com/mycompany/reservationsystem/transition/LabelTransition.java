package com.mycompany.reservationsystem.transition;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class LabelTransition {
    private static SequentialTransition currentTransition;

    private LabelTransition() {}

    public static void play(Label label) {
        if (label == null) return;

        // Stop any ongoing animation
        if (currentTransition != null) {
            currentTransition.stop();
        }

        // Reset visibility and opacity
        label.setVisible(true);
        //label.setManaged(true);
        label.setOpacity(1);

        // Delay before fade out
        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        // Fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), label);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Sequential: pause → fade
        currentTransition = new SequentialTransition(pause, fadeOut);
        currentTransition.setOnFinished(e -> {
            label.setVisible(false);
            //label.setManaged(false);
            label.setOpacity(1); // reset for next message
        });

        currentTransition.play();
    }
}
