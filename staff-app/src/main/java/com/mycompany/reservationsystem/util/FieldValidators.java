package com.mycompany.reservationsystem.util;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public class FieldValidators {

    public static boolean validateAmountPaid(MFXTextField field) {
        if (field == null) return false;

        String text = field.getText();
        if (text == null || text.isBlank()) return false;

        try {
            BigDecimal amount = new BigDecimal(text);
            return amount.compareTo(BigDecimal.ZERO) > 0
                    && amount.scale() <= 2; // max 2 decimal places
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean required(TextField field) {
        String text = field.getText();
        return text != null && !text.trim().isEmpty();

    }

    public static boolean numeric(TextField field) {
        return field.getText().matches("\\d+");

    }

    public static boolean minLength(TextField field, int length) {
        return field.getText().length() >= length;
    }

    public static boolean maxLength(TextField field, int length) {
        return field.getText().length() <= length;
    }

    public static boolean regex(TextField field, String pattern) {
        return field.getText().matches(pattern);
    }


    // --- STYLING HELPERS (ERROR / CLEAR) ---

    public static void markInvalid(TextField field) {
        field.setStyle("-fx-border-color: #ff4d4d; -fx-border-width: 2;");
    }

    public static void clear(TextField field) {
        field.setStyle("");
    }


    // --- COMBINED VALIDATION ---

    public static boolean validateRequired(TextField field) {
        if (!required(field)) {
            markInvalid(field);
            return false;
        }
        clear(field);
        return true;
    }

    public static boolean validateNumeric(TextField field) {
        if (!numeric(field)) {
            markInvalid(field);
            return false;
        }
        clear(field);
        return true;
    }

    public static boolean isNonZeroNumeric(TextField field) {
        String text = field.getText();

        if (text == null || text.isEmpty()) {
            markInvalid(field);
            return false;
        }

        // Only check for zero, parsing as integer (or double if needed)
        int intValue = Integer.parseInt(text);
        if (intValue != 0) {
            return true;
        } else {
            markInvalid(field);
            return false;
        }
    }
    public static boolean startsWith09(TextField textField) {
        if (textField == null) return false;

        String text = textField.getText();
        if (text == null) {
            markInvalid(textField);
            return false;
        }

        // Remove spaces
        String digits = text.replaceAll("\\s+", "");

        // Must start with 09
        if (!digits.startsWith("09")) {
            markInvalid(textField);
            return false;
        }

        // Must be at least 11 digits
        if (digits.length() < 11) {
            markInvalid(textField);
            return false;
        }

        clear(textField);
        return true;
    }

}

