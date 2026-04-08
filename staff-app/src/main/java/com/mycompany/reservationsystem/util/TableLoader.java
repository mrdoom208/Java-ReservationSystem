package com.mycompany.reservationsystem.util;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TableLoader {

    /**
     * Load data into an ObservableList with dynamic ProgressIndicator.
     *
     * @param list        The ObservableList to populate
     * @param dataSupplier Supplier to fetch data
     * @param container   StackPane containing the TableView or UI
     * @param onLoaded    Callback after data is loaded
     * @param <T>         Type of list items
     */
    public static <T> void loadListWithProgress(
            ObservableList<T> list,
            Supplier<List<T>> dataSupplier,
            StackPane container,
            Consumer<List<T>> onLoaded
    ) {
        // Create ProgressIndicator dynamically
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(80, 80);
        progressIndicator.setStyle("-fx-progress-color: #00aaff;");
        container.getChildren().add(progressIndicator);

        // Background task
        Task<List<T>> task = new Task<>() {
            @Override
            protected List<T> call() {
                return dataSupplier.get();
            }
        };

        // On success
        task.setOnSucceeded(e -> {
            List<T> result = task.getValue();
            list.setAll(result); // populate the ObservableList

            container.getChildren().remove(progressIndicator);

            if (onLoaded != null) {
                onLoaded.accept(result);
            }
        });

        // On failure
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            container.getChildren().remove(progressIndicator);
        });

        new Thread(task).start();
    }
}