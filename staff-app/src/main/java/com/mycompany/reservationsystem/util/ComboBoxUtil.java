package com.mycompany.reservationsystem.util;

import com.mycompany.reservationsystem.model.Message;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.util.StringConverter;

public class ComboBoxUtil {
    private ComboBoxUtil(){}

    public static void formatMessageComboBox(MFXComboBox<Message> comboBox){
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Message message) {
                return message == null ? "" : message.getMessageLabel();
            }

            @Override
            public Message fromString(String string) {
                return null; // not used (non-editable)
            }
        });
    }
    public static <T> T selectTopItem(MFXComboBox<T> comboBox) {
        if (comboBox.getItems().isEmpty()) return null;

        T topItem = comboBox.getItems().get(0);
        comboBox.getSelectionModel().selectIndex(0);
        return topItem;
    }
    public static void selectMessageByLabel(MFXComboBox<Message> comboBox, String label) {
        comboBox.getItems().stream()
                .filter(m -> m.getMessageLabel().equalsIgnoreCase(label))
                .findFirst()
                .ifPresent(m -> comboBox.getSelectionModel().selectItem(m));
    }
}
