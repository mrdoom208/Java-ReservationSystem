package com.mycompany.reservationsystem.util;

import com.mycompany.reservationsystem.config.AppSettings;
import io.github.palexdev.materialfx.controls.MFXToggleButton;

public class ToggleButtonUtil {
    private static final String ON_TEXT = "ON";
    private static final String OFF_TEXT = "OFF";
    private static final String API_TEXT = "API";
    private static final String MODULE_TEXT = "MODULE";

    public static void setupToggle(MFXToggleButton toggle, String key) {
        boolean state = AppSettings.loadMessageEnabled(key);
        toggle.setSelected(state);
        updateText(toggle, state, ON_TEXT, OFF_TEXT);
        toggle.selectedProperty().addListener((obs, oldVal, newVal) ->
                updateText(toggle, newVal, ON_TEXT, OFF_TEXT)
        );
    }

    public static void setupMessage(MFXToggleButton toggle, String key) {
        boolean state = AppSettings.loadMessagePane(key);
        toggle.setSelected(state);
        updateText(toggle, state, API_TEXT, MODULE_TEXT);
        toggle.selectedProperty().addListener((obs, oldVal, newVal) ->
                updateText(toggle, newVal, API_TEXT, MODULE_TEXT)
        );
    }

    private static void updateText(MFXToggleButton toggle, boolean selected, String selectedText, String unselectedText) {
        toggle.setText(selected ? selectedText : unselectedText);
    }
}
