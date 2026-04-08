package com.mycompany.reservationsystem.controller.popup;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.util.ButtonLoad;
import com.mycompany.reservationsystem.util.FieldRestrictions;
import com.mycompany.reservationsystem.util.FieldValidators;
import com.mycompany.reservationsystem.model.ManageTables;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.util.List;
import java.util.Map;

public class addTableDialogController {

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
        statusComboBox.getItems().addAll("Available", "Occupied");
        statusComboBox.setValue("Available");
        addButton.setDefaultButton(true);

        fieldRestrictions.applyNumbersOnly(tableCapacityField);
        fieldRestrictions.applyNumbersOnly(tableNumberField);

        cancelButton.setOnAction(e -> dialogStage.close());

        addButton.setOnAction(e -> {
            if (!fieldValidators.validateRequired(tableNumberField)) {showError("Please Insert Table Number"); return;}
            if (!fieldValidators.validateRequired(tableCapacityField)) {showError("Please Insert Table Capacity"); return;}
            if (!fieldValidators.validateRequired(tableLocationField)){showError("Please Insert Table Location"); return;}
            
            try {
                List<Map<String, Object>> tables = ApiClient.getTables();
                if (tables != null) {
                    for (Map<String, Object> tableMap : tables) {
                        if (tableNumberField.getText().equals(String.valueOf(tableMap.get("tableNo")))) {
                            fieldValidators.markInvalid(tableNumberField);
                            showError("Table Number Already Exist");
                            return;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            ButtonLoad load = new ButtonLoad();
            load.ButtonStart(addButton);
            Task<ManageTables> task = new Task<ManageTables>() {
                @Override
                protected ManageTables call() throws Exception {
                    ManageTables newTable = new ManageTables();
                    newTable.setCapacity(Integer.parseInt(tableCapacityField.getText()));
                    newTable.setTableNo(tableNumberField.getText());
                    newTable.setStatus(statusComboBox.getValue());
                    newTable.setLocation(tableLocationField.getText());
                    
                    ApiClient.createTable(newTable);
                    return newTable;
                }
            };
            task.setOnSucceeded(f->{
                showSuccess("Table Added Successfully");
                dialogStage.close();
            });
            task.setOnFailed(f->{
                task.getException().printStackTrace();
                showError("Please Check your Internet Connection");
                load.ButtonFinished(addButton);
            });
            new Thread(task).start();
        });
    }
}
