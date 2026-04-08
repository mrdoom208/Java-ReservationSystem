package com.mycompany.reservationsystem.transition;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class NodeTransition {
    public static void showSmooth(Node node) {
        node.setVisible(true);
        node.setOpacity(0);
        node.setTranslateY(15);

        FadeTransition fade = new FadeTransition(Duration.millis(100), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(150), node);
        slide.setFromY(15);
        slide.setToY(0);

        new ParallelTransition(fade, slide).play();
    }

}
