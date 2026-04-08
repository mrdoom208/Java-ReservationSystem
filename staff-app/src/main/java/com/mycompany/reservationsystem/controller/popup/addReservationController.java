package com.mycompany.reservationsystem.controller.popup;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.model.Customer;
import com.mycompany.reservationsystem.util.PhoneFormatter;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static com.mycompany.reservationsystem.util.FieldRestrictions.*;
import static com.mycompany.reservationsystem.util.FieldValidators.isNonZeroNumeric;
import static com.mycompany.reservationsystem.util.FieldValidators.validateRequired;

@Component
public class addReservationController {

    @FXML
    private StackPane rootPane;
    @FXML private VBox loadingOverlay;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label loadingLabel;

    @FXML
    private MFXTextField CustomerName;

    @FXML
    private MFXTextField Email;

    @FXML
    private MFXTextField Pax;

    @FXML
    private TextField Phone;

    @FXML
    private MFXButton addButton;

    @FXML
    private MFXButton cancelButton;

    @FXML
    private MFXComboBox<String> statusComboBox;

    @FXML
    private Label messageLabel;

    private Stage dialogStage;
    private PhoneFormatter phoneFormatter;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
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

    private void showLoading(String message) {
        loadingLabel.setText(message);
        loadingOverlay.setVisible(true);
        loadingOverlay.setManaged(true);
    }

    private void hideLoading() {
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);
    }
    @FXML
    public void initialize() {
        statusComboBox.getItems().addAll("Pending", "Confirm");
        statusComboBox.setValue("Pending");

        applyLettersOnly(CustomerName);
        applyNumbersOnly(Pax);
        phoneFormatter = new PhoneFormatter("+63",Phone);
        applyEmailRestriction(Email);

        cancelButton.setOnAction(e -> dialogStage.close());

        addButton.setOnAction(e -> {
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
            addButton.setDisable(true);
            showLoading("Saving reservation...");

            Task<Void> saveTask = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        Customer newCustomer = new Customer();
                        newCustomer.setName(CustomerName.getText());
                        newCustomer.setPhone(phoneFormatter.getCleanPhone());
                        String emailText = Email.getText();
                        if (emailText != null && !emailText.isBlank()) {
                            newCustomer.setEmail(emailText);
                        }
                        
                        Map<String, Object> customerResult = ApiClient.createCustomer(newCustomer);
                        
                        Long customerId = null;
                        if (customerResult.containsKey("id")) {
                            customerId = ((Number) customerResult.get("id")).longValue();
                        }

                        long count = ApiClient.countReservations();
                        
                        Map<String, Object> reservationData = new java.util.HashMap<>();
                        reservationData.put("customerId", customerId);
                        reservationData.put("status", statusComboBox.getValue());
                        reservationData.put("date", LocalDate.now().toString());
                        reservationData.put("pax", Integer.parseInt(Pax.getText()));
                        reservationData.put("reference", String.format("RSV-%05d", count + 1));
                        reservationData.put("reservationPendingtime", LocalTime.now().toString());

                        ApiClient.createReservation(reservationData);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                    return null;
                }
            };

            saveTask.setOnSucceeded(ev -> {
                hideLoading();
                showSuccess("Reservation Added Successfully");
                dialogStage.close();
            });

            saveTask.setOnFailed(ev -> {
                hideLoading();
                addButton.setDisable(false);
                Throwable ex = saveTask.getException();
                showError("Failed to save reservation");
                ex.printStackTrace();
            });

            Thread thread = new Thread(saveTask);
            thread.setDaemon(true);
            thread.start();
        });
    }

}
