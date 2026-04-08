package com.mycompany.reservationsystem.controller.popup;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.model.ManageTables;
import com.mycompany.reservationsystem.util.ButtonLoad;
import com.mycompany.reservationsystem.util.FieldRestrictions;
import com.mycompany.reservationsystem.util.FieldValidators;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class editTableDialogController {
    @FXML
    private ManageTables targetTable;

    public void setTargetTable(ManageTables TargetTable){
        this.targetTable = TargetTable;
        if (targetTable != null) {
            tableNumberField.setText(targetTable.getTableNo());
            tableCapacityField.setText(String.valueOf(targetTable.getCapacity()));
            if (targetTable.getLocation() != null) {
                tableLocationField.setText(targetTable.getLocation());
            }
            statusComboBox.selectItem(targetTable.getStatus());
        }
    }
    @FXML
    private MFXTextField tableCapacityField;

    @FXML
    private MFXTextField tableNumberField;

    @FXML
    private MFXTextField tableLocationField;

    @FXML
    private MFXComboBox<String> statusComboBox;

    @FXML
    private MFXButton addButton;

    @FXML
    private MFXButton cancelButton;

    @FXML
    private Label messageLabel;

    FieldValidators fieldValidators;
    FieldRestrictions fieldRestrictions;

    private Stage dialogStage;

    private void showError(String message) {
        messageLabel.getStyleClass().removeAll("popup-success", "popup-message-hidden");
        messageLabel.getStyleClass().add("popup-error");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.getStyleClass().removeAll("popup-error", "popup-message-hidden");
        messageLabel.getStyleClass().add("popup-success");
        messageLabel.setText(message);
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    public void initialize() {
        statusComboBox.getItems().addAll("Available", "Reserved", "Occupied");
        addButton.setDefaultButton(true);

        fieldRestrictions.applyNumbersOnly(tableCapacityField);
        fieldRestrictions.applyNumbersOnly(tableNumberField);

        tableNumberField.setDisable(true);

        cancelButton.setOnAction(e -> dialogStage.close());

        addButton.setOnAction(e -> {
            if (!fieldValidators.validateRequired(tableNumberField)) {showError("Please Insert Table Number"); return;}
            if (!fieldValidators.validateRequired(tableCapacityField)) {showError("Please Insert Table Capacity"); return;}
            if (!fieldValidators.validateRequired(tableLocationField)){showError("Please Insert Table Location"); return;}

            ButtonLoad load = new ButtonLoad();
            load.ButtonStart(addButton);
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    targetTable.setCapacity(Integer.parseInt(tableCapacityField.getText()));
                    targetTable.setTableNo(tableNumberField.getText());
                    targetTable.setLocation(tableLocationField.getText());
                    targetTable.setStatus(statusComboBox.getValue());
                    
                    ApiClient.updateTable(targetTable.getId(), targetTable);
                    return null;
                }
            };
            task.setOnSucceeded(f->{
                showSuccess("Table Changed Successfully");
                load.ButtonFinished(addButton);
                dialogStage.close();
            });
            task.setOnFailed(f->{
                task.getException().printStackTrace();
                load.ButtonFinished(addButton);
                showError("Please Check Your Internet Connection");
            });
            new Thread(task).start();
        });
    }
}
