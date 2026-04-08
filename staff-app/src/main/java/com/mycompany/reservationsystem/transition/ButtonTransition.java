package com.mycompany.reservationsystem.transition;

import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class ButtonTransition {
    private static final Duration BTN_ANIM_DURATION = Duration.millis(120);
    private static final double HOVER_TRANSLATE_X = 6;
    private static final double HOVER_SCALE = 1.03;
    private static final double PRESS_SCALE = 0.96;

    private static void animateHoverIn(Button btn) {
        TranslateTransition slide = new TranslateTransition(BTN_ANIM_DURATION, btn);
        slide.setToX(HOVER_TRANSLATE_X);

        ScaleTransition scale = new ScaleTransition(BTN_ANIM_DURATION, btn);
        scale.setToX(HOVER_SCALE);
        scale.setToY(HOVER_SCALE);

        new ParallelTransition(slide, scale).play();
    }

    private static void animateHoverOut(Button btn) {
        TranslateTransition slide = new TranslateTransition(BTN_ANIM_DURATION, btn);
        slide.setToX(0);

        ScaleTransition scale = new ScaleTransition(BTN_ANIM_DURATION, btn);
        scale.setToX(1);
        scale.setToY(1);

        new ParallelTransition(slide, scale).play();
    }

    private static void animatePress(Button btn) {
        ScaleTransition press = new ScaleTransition(Duration.millis(80), btn);
        press.setToX(PRESS_SCALE);
        press.setToY(PRESS_SCALE);
        press.play();
    }

    private static void animateRelease(Button btn) {
        ScaleTransition release = new ScaleTransition(Duration.millis(80), btn);
        release.setToX(1);
        release.setToY(1);
        release.play();
    }

    /// ///////////////////MAIN//////////////////////////////////////////////////////////
    public static void setupButtonAnimation(Button btn) {
        btn.setOnMouseEntered(e -> animateHoverIn(btn));
        btn.setOnMouseExited(e -> animateHoverOut(btn));
        btn.setOnMousePressed(e -> animatePress(btn));
        btn.setOnMouseReleased(e -> animateRelease(btn));
    }


}
