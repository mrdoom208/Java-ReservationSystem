package com.mycompany.reservationsystem.util;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;

public class LoadingUtil {

    private static final String LOADING_KEY = "loading-indicator";

    /**
     * Show loading spinner on a button and optionally hide a label
     */
    public static void show(Button button, Label targetLabel) {

        // Prevent double loading
        if (button.getProperties().containsKey(LOADING_KEY)) return;

        // Save original text
        button.getProperties().put("original-text", button.getText());
        button.setText("");
        button.setDisable(true);

        // Hide label if provided
        if (targetLabel != null) {
            targetLabel.setVisible(false);
        }

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(18, 18);
        spinner.setStyle("-fx-progress-color: white;");

        StackPane container = new StackPane(spinner);
        container.setMouseTransparent(true);

        button.setGraphic(container);
        button.getProperties().put(LOADING_KEY, spinner);
    }

    /**
     * Hide loading spinner and restore button state
     */
    public static void hide(Button button, Label targetLabel) {

        if (!button.getProperties().containsKey(LOADING_KEY)) return;

        button.setDisable(false);
        button.setGraphic(null);

        // Restore text
        String originalText = (String) button.getProperties().get("original-text");
        button.setText(originalText);

        // Restore label
        if (targetLabel != null) {
            targetLabel.setVisible(true);
        }

        button.getProperties().remove(LOADING_KEY);
        button.getProperties().remove("original-text");
    }
}
