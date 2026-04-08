package com.mycompany.reservationsystem.controller.popup;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;


public class ConfirmationDialogController {
    @FXML
    private Button cancelBtn;

    @FXML
    private Label messagelabel;

    @FXML
    private Button sendBtn;

    private Runnable onConfirm;

    public void setMessage(String message) {
        messagelabel.setText(message);
    }

    public void setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
    }

    @FXML
    private void onCancel() {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }

    @FXML
    private void onConfirm() {
        if (onConfirm != null) {
            onConfirm.run();
        }
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }
}
