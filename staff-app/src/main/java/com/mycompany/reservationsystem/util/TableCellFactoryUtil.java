package com.mycompany.reservationsystem.util;

import com.mycompany.reservationsystem.dto.CustomerReportDTO;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public final class TableCellFactoryUtil {

    private TableCellFactoryUtil() {
        // Prevent instantiation
    }

    public static <T> void addItemsToCombo(
            MFXComboBox<String> combo,
            FilteredList<T> filteredData,
            Function<T, String> propertyExtractor,
            String... statuses) {

        combo.getItems().addAll(statuses);

        combo.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            int i = newIndex.intValue();

            if (i >= 0 && i < statuses.length) {
                String selectedStatus = statuses[i];

                if (selectedStatus.equalsIgnoreCase("Show All")) {
                    filteredData.setPredicate(item -> {
                        String status = propertyExtractor.apply(item);
                        if (status == null) return false;
                        for (String s : statuses) {
                            if (!s.equalsIgnoreCase("Show All") && status.equalsIgnoreCase(s)) {
                                return true;
                            }
                        }
                        return false;
                    }); // no filter
                } else {
                    filteredData.setPredicate(item ->
                            propertyExtractor.apply(item) != null &&
                                    propertyExtractor.apply(item).equalsIgnoreCase(selectedStatus)
                    );
                }

                combo.setText(selectedStatus);
            }
        });
    }

    public static <T> void applyStatusStyle(TableColumn<T, String> column) {
        column.setCellFactory(tv -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }

                setText(item);

                String bgColor = switch (item) {
                    case "Available" -> "#2a4d2a";
                    case "Occupied" -> "#5a1e1e";
                    case "Reserved" -> "#7A5A00";
                    case "Confirm" -> "#2196F3";
                    case "Pending" -> "#455A64";
                    case "Seated" -> "#2E7D32";
                    case "Cancelled" -> "#D32F2F";
                    case "No Show" -> "#9C27B0";
                    case "Complete" -> "#4CAF50";
                    default -> "white";
                };

                setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 5;");
            }
        });
    }

    public static <T> void applyTimeFormat(TableColumn<T, LocalTime> column) {
        DateTimeFormatter TIME_FORMATTER =
                DateTimeFormatter.ofPattern("hh:mm a");
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(TIME_FORMATTER));
                }
            }
        });
    }

    public static <T, N extends Number> void applyDecimalFormat(
            TableColumn<T, N> column,
            int decimalPlaces
    ) {
        String format = "%." + decimalPlaces + "f";

        column.setCellFactory(col -> new TableCell<T, N>() {
            @Override
            protected void updateItem(N item, boolean empty) {
                super.updateItem(item, empty);

                setText(empty || item == null
                        ? null
                        : String.format(format, item.doubleValue()));
            }
        });

    }
}