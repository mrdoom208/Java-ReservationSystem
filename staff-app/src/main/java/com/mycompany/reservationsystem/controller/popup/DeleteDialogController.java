package com.mycompany.reservationsystem.controller.popup;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DeleteDialogController {

    @FXML private Button cancelBtn;
    @FXML private Button deleteBtn;
    @FXML private Label messagelabel;

    private Runnable onDelete; // callback

    public void setOnDelete(Runnable action) {
        this.onDelete = action;
    }
    public void setMessage(String message) {
        messagelabel.setText(message);
    }

    @FXML
    private void initialize() {

        cancelBtn.setOnAction(e ->
            cancelBtn.getScene().getWindow().hide()
        );

        deleteBtn.setOnAction(e -> {
            if (onDelete != null) onDelete.run();
            deleteBtn.getScene().getWindow().hide();
        });
    }
}