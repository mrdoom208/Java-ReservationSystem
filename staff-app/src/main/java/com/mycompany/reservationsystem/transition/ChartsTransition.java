package com.mycompany.reservationsystem.transition;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ChartsTransition {
    /// /////////////////////////////////////////////////////////
    private void expand(BarChart<?, ?> target, VBox rootVBox) {
        // Heights and animation duration defined inside the method
        double normalHeight = 200;
        double hoverHeight = 400;
        double shrinkHeight = 120;
        Duration animationDuration = Duration.millis(300);

        for (Node node : rootVBox.getChildren()) {
            if (node instanceof BarChart) {
                BarChart<?, ?> chart = (BarChart<?, ?>) node;
                double targetHeight = (chart == target) ? hoverHeight : shrinkHeight;

                Timeline timeline = new Timeline(
                        new KeyFrame(animationDuration,
                                new KeyValue(chart.prefHeightProperty(), targetHeight))
                );
                timeline.play();

                VBox.setVgrow(chart, (chart == target) ? Priority.ALWAYS : Priority.NEVER);
            }
        }
    }

    private void resetSizes(VBox rootVBox) {
        // Heights and animation duration defined inside the method
        double normalHeight = 200;
        Duration animationDuration = Duration.millis(300);

        for (Node node : rootVBox.getChildren()) {
            if (node instanceof BarChart) {
                BarChart<?, ?> chart = (BarChart<?, ?>) node;

                Timeline timeline = new Timeline(
                        new KeyFrame(animationDuration,
                                new KeyValue(chart.prefHeightProperty(), normalHeight))
                );
                timeline.play();

                VBox.setVgrow(chart, Priority.ALWAYS);
            }
        }
    }

    public void setupHoverExpand(BarChart<?, ?> chart, VBox root) {
        chart.setOnMouseEntered(e -> expand(chart,root));
        chart.setOnMouseExited(e -> resetSizes(root));

    }
    /// ///////////////////////////////////////////////////////////////////////////////
}
