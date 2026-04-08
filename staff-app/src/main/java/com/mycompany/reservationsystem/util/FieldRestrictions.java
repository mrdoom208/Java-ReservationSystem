package com.mycompany.reservationsystem.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;

import java.util.function.UnaryOperator;


public class FieldRestrictions {

    // Email restriction
    public static void applyEmailRestriction(TextField textField) {

        // Block invalid characters while typing
        textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String c = event.getCharacter();

            // no spaces
            if (c.equals(" ")) {
                event.consume();
                return;
            }

            // valid email characters only
            if (!c.matches("[a-zA-Z0-9@._-]")) {
                event.consume();
            }
        });

        // Prevent pasted invalid text
        textField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z0-9@._-]*")) {
                textField.setText(oldValue);
            }
        });
    }

    public static void applyNumericDecimalNonZeroFilter(TextField field) {
        // 1️⃣ Prevent typing letters or multiple dots immediately
        field.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            String character = e.getCharacter();

            // Block letters
            if (!character.matches("[0-9\\.]")) {
                e.consume();
                return;
            }

            String currentText = field.getText();

            // Block multiple dots
            if (character.equals(".") && currentText.contains(".")) {
                e.consume();
                return;
            }

            // Block typing more than 2 decimals
            if (currentText.contains(".")) {
                int dotIndex = currentText.indexOf(".");
                String decimals = currentText.substring(dotIndex + 1);
                if (decimals.length() >= 2 && field.getSelection().getLength() == 0) {
                    // Only block if no selection (allows replacing selected text)
                    e.consume();
                }
            }
        });

        // 2️⃣ Apply formatter for decimals and non-zero values
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            if (newText.isEmpty()) return change; // allow clearing field

            // Allow only digits and optional single dot
            if (!newText.matches("\\d*(\\.\\d{0,2})?")) return null;

            // Reject zero values
            try {
                double value = Double.parseDouble(newText);
                if (value == 0) return null;
            } catch (NumberFormatException e) {
                return null;
            }

            return change;
        };

        field.setTextFormatter(new TextFormatter<>(filter));
    }           




    public static void applyLettersOnly(TextField textField) {
        textField.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if (!e.getCharacter().matches("[a-zA-Z ]")) e.consume();
        });
        textField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z ]*")) textField.setText(oldValue);
        });
    }

    // Numbers only (for pax or phone fields)
    public static void applyNumbersOnly(TextField textField) {
        textField.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if (!e.getCharacter().matches("[0-9]")) e.consume();
        });
        textField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("[0-9]*")) textField.setText(oldValue);
        });
    }

    // Optional: email format checker
    public static boolean isValidEmail(TextField field) {
        String email = field.getText();
        markInvalid(field);
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    }

    public static boolean isNotEmpty(TextField field) {
        String text = field.getText();
        return text != null && !text.trim().isEmpty();
    }
    public static void markInvalid(TextField field) {
        field.setStyle("-fx-border-color: #ff4d4d; -fx-border-width: 2;");
    }
}