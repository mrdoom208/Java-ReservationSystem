package com.mycompany.reservationsystem.util;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class PhoneFormatter {

    private final String prefix;
    private final TextField phoneInput;

    public PhoneFormatter(String prefix, TextField phoneInput) {
        this.prefix = prefix;
        this.phoneInput = phoneInput;
        initialize();
    }

    private void initialize() {
        // Set initial prefix
        phoneInput.setText(prefix);
        phoneInput.positionCaret(phoneInput.getText().length());

        // Handle typing/input
        phoneInput.addEventFilter(KeyEvent.KEY_TYPED, event -> formatInput());

        // Prevent deleting prefix
        phoneInput.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            int cursorPos = phoneInput.getCaretPosition();
            if (cursorPos <= prefix.length() &&
                    (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE)) {
                event.consume();
            }

            // Disable Ctrl+V / Command+V paste
            if ((event.isControlDown() || event.isMetaDown()) && event.getCode() == KeyCode.V) {
                event.consume();
            }
        });

        // Disable right-click paste
        phoneInput.setContextMenu(null);
    }

    // Format the input as +63 XXX XXX XXXX
    private void formatInput() {
        String value = phoneInput.getText();

        if (!value.startsWith(prefix)) {
            value = prefix;
        }

        String digits = value.substring(prefix.length()).replaceAll("\\D", "");
        if (digits.length() > 9) digits = digits.substring(0, 10);

        phoneInput.setText(formatDigits(digits));
        phoneInput.positionCaret(phoneInput.getText().length());
    }

    private String formatDigits(String digits) {
        StringBuilder formatted = new StringBuilder(prefix);

        if (digits.length() > 0) formatted.append(" ").append(digits.substring(0, Math.min(3, digits.length())));
        if (digits.length() > 3) formatted.append(" ").append(digits.substring(3, Math.min(6, digits.length())));
        if (digits.length() > 6) formatted.append(" ").append(digits.substring(6, Math.min(9, digits.length())));

        return formatted.toString();
    }

    /**
     * Returns a cleaned phone number with prefix included
     * Example: "+63 912 345 6789" -> "+639123456789"
     */
    public String getCleanPhone() {
        String value = phoneInput.getText().replaceAll("\\s", "");
        if (value.startsWith("+63")) {
            return value;
        } else if (value.startsWith("63")) {
            return "+" + value;
        } else if (value.startsWith("9")) {
            return "+63" + value;
        } else if (value.startsWith("09")) {
            return "+63" + value.substring(1);
        }
        return "+63" + value;
    }

    /**
     * Validates if the current input has exactly 10 digits after the prefix
     */
    public boolean isValid() {
        String digits = phoneInput.getText().substring(prefix.length()).replaceAll("\\D", "");
        if (digits.length() != 10) return false;
        return digits.charAt(0) == '9';

    }


    public static String normalizePHNumber(String number) {
        if (number == null) {
            throw new IllegalArgumentException("Phone number is null");
        }

        // Remove spaces, dashes, parentheses
        number = number.trim().replaceAll("[^0-9+]", "");

        // +639XXXXXXXXX → 639XXXXXXXXX
        if (number.startsWith("+639")) {
            return number.substring(1);
        }

        // 09XXXXXXXXX → 639XXXXXXXXX
        if (number.startsWith("09")) {
            return "639" + number.substring(2);
        }

        // Already correct
        if (number.startsWith("639") && number.length() == 12) {
            return number;
        }

        throw new IllegalArgumentException("Invalid PH mobile number: " + number);
    }

}
