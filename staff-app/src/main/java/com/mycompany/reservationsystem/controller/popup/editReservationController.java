package com.mycompany.reservationsystem.controller.popup;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.model.Customer;
import com.mycompany.reservationsystem.model.ManageTables;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.util.ButtonLoad;
import com.mycompany.reservationsystem.util.FieldValidators;
import com.mycompany.reservationsystem.util.PhoneFormatter;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mycompany.reservationsystem.util.FieldRestrictions.*;
import static com.mycompany.reservationsystem.util.FieldValidators.isNonZeroNumeric;
import static com.mycompany.reservationsystem.util.FieldValidators.validateRequired;

public class editReservationController {
    private Reservation targetReservation;

    public void setTargetReservation(Reservation targetReservation) {
        this.targetReservation = targetReservation;
        if (targetReservation.getCustomer() != null) {
            CustomerName.setText(targetReservation.getCustomer().getName());
            Phone.setText(targetReservation.getCustomer().getPhone());
            if (targetReservation.getCustomer().getEmail() != null) {
                Email.setText(targetReservation.getCustomer().getEmail());
            }
        }
        Pax.setText(String.valueOf(targetReservation.getPax()));
        statusComboBox.setValue(targetReservation.getStatus());
    }
    private Stage dialogStage;
    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    @FXML
    private MFXTextField CustomerName;

    @FXML
    private MFXTextField Email;

    @FXML
    private MFXTextField Pax;

    @FXML
    private TextField Phone;

    @FXML
    private MFXButton cancelButton,Submit,removeBtn;

    @FXML
    private Label messageLabel;

    @FXML
    private MFXComboBox<String> statusComboBox;

    @FXML
    private MFXComboBox<ManageTables>tableNoSelect;

    private ObservableList<ManageTables> tables;

    private PhoneFormatter phoneFormatter;

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

    private void loadTables() {
        Task<ObservableList<ManageTables>> task = new Task<>() {
            @Override
            protected ObservableList<ManageTables> call() {
                List<ManageTables> combined = new ArrayList<>();
                try {
                    List<Map<String, Object>> tablesList = ApiClient.getTables();
                    if (tablesList != null) {
                        for (Map<String, Object> tableMap : tablesList) {
                            ManageTables mt = new ManageTables();
                            mt.setId(((Number) tableMap.get("id")).longValue());
                            mt.setTableNo(String.valueOf(tableMap.get("tableNo")));
                            mt.setStatus(String.valueOf(tableMap.get("status")));
                            mt.setCapacity(((Number) tableMap.get("capacity")).intValue());
                            if ("Available".equals(mt.getStatus())) {
                                combined.add(mt);
                            }
                        }
                    }
                    if (targetReservation.getTable() != null) {
                        combined.add(targetReservation.getTable());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return javafx.collections.FXCollections.observableArrayList(combined);
            }
        };

        task.setOnSucceeded(e -> {
            tableNoSelect.setItems(task.getValue());
            if (targetReservation != null && targetReservation.getTable() != null) {
                for (ManageTables t : tableNoSelect.getItems()) {
                    if (t.getId().equals(targetReservation.getTable().getId())) {
                        tableNoSelect.selectItem(t);
                        break;
                    }
                }
            }
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            showError("Failed to load tables.");
        });

        new Thread(task).start();
    }

    @FXML
    private void initialize(){
        statusComboBox.getItems().addAll("Pending", "Confirm");
        statusComboBox.setDisable(true);
        applyEmailRestriction(Email);
        applyNumbersOnly(Pax);
        
        if (targetReservation != null && targetReservation.getCustomer() != null && targetReservation.getCustomer().getPhone() != null) {
            String phone = targetReservation.getCustomer().getPhone();
            Phone.setText(phone);
        }
        
        phoneFormatter = new PhoneFormatter("+63",Phone);
        applyLettersOnly(CustomerName);
        loadTables();
        removeBtn.setOnAction(e->tableNoSelect.clearSelection());

        cancelButton.setOnAction(e -> dialogStage.close());
        Submit.setOnAction(event -> {
            if (!validateRequired(CustomerName)) {showError("Please Insert Customer Name"); return;}
            if (!validateRequired(Pax)) {showError("Please Insert Pax Size"); return;}
            if (!isNonZeroNumeric(Pax)) {showError("Pax must be greater than zero."); return;}
            if (!validateRequired(Phone)){ showError("Please Insert Phone No."); return;}
            if (!phoneFormatter.isValid()) {showError("Please Insert Valid Phone No."); return;}
            if (Email.getText() != null && !Email.getText().trim().isEmpty()) {
                if (!isValidEmail(Email)) {
                    showError("Please Insert Valid Email");
                    return;
                }
            }
            ButtonLoad load = new ButtonLoad();
            load.ButtonStart(Submit);
            Task<Void>task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    targetReservation.getCustomer().setName(CustomerName.getText());
                    targetReservation.setPax(Integer.parseInt(Pax.getText()));
                    targetReservation.getCustomer().setPhone(phoneFormatter.getCleanPhone());
                    String emailText = Email.getText();
                    targetReservation.getCustomer().setEmail(emailText != null && !emailText.isBlank() ? emailText : null);

                    if(targetReservation.getTable() != null){
                        ManageTables oldTable = targetReservation.getTable();
                        oldTable.setStatus("Available");
                        ApiClient.updateTable(oldTable.getId(), oldTable);
                    }

                    ManageTables selectedTable = tableNoSelect.getSelectedItem();
                    if(selectedTable != null) {
                        selectedTable.setStatus("Reserved");
                        ApiClient.updateTable(selectedTable.getId(), selectedTable);
                        ApiClient.updateReservationTable(targetReservation.getReference(), selectedTable.getId());
                        targetReservation.setTable(selectedTable);
                    } else {
                        ApiClient.clearReservationTable(targetReservation.getReference());
                        targetReservation.setTable(null);
                    }

                    ApiClient.updateReservationByReference(targetReservation.getReference(), targetReservation);
                    return null;
                }
            };
            task.setOnSucceeded(e->{
                showSuccess("Reservation Changed Successfully");
                load.ButtonFinished(Submit);
                dialogStage.close();
            });
            task.setOnFailed(e->{
                task.getException().printStackTrace();
                load.ButtonFinished(Submit);
                showError("Please Check Your Internet Connection");
            });
            new Thread(task).start();
        });
    }
}
