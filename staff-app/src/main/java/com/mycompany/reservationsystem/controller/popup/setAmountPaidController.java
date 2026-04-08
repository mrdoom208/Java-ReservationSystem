package com.mycompany.reservationsystem.controller.popup;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.util.ButtonLoad;
import com.mycompany.reservationsystem.util.FieldRestrictions;
import com.mycompany.reservationsystem.util.FieldValidators;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class setAmountPaidController {
    @FXML
    private MFXTextField AmountPaid;

    @FXML
    private MFXButton cancelButton;

    @FXML
    private Label messageLabel;

    @FXML
    private MFXTextField reference;

    @FXML
    private MFXButton setAmount;

    private Stage dialogStage;

    private boolean cancelled = true;
    private FieldValidators fieldValidators;
    private FieldRestrictions fieldRestrictions;

    public boolean isCancelled() {
        return cancelled;
    }
    public BigDecimal getAmount(){ return new BigDecimal(AmountPaid.getText());}

    public void setReference(String Reference){
        reference.setText(Reference);
    }
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

    @FXML
    public void initialize() {
        FieldRestrictions.applyNumericDecimalNonZeroFilter(AmountPaid);

        cancelButton.setOnAction(e -> {
            cancelled = true;
            dialogStage.close();
        });
        setAmount.setOnAction(event -> {
            if(!FieldValidators.validateRequired(AmountPaid)) {showError("Please Insert Amount Paid"); return;}

            ButtonLoad load = new ButtonLoad();
            load.ButtonStart(setAmount);
                BigDecimal amount = new BigDecimal(AmountPaid.getText());

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    showError("Amount must be greater than zero");
                    return;
                }

                DecimalFormat df = new DecimalFormat("0.00");
                String formatted = df.format(amount);

            Task<Void>task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    AmountPaid.setText(formatted);
                    
                    try {
                        String ref = reference.getText();
                        Map<String, Object> reservationData = ApiClient.getReservationByReference(ref);
                        if (reservationData != null && !reservationData.containsKey("error")) {
                            Reservation reservation = new Reservation();
                            reservation.setId(((Number) reservationData.getOrDefault("id", 0)).longValue());
                            reservation.setReference(ref);
                            reservation.setSales(amount);
                            reservation.setStatus("Complete");
                            ApiClient.updateReservationByReference(ref, reservation);
                            ApiClient.finishReservation(ref, formatted, null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            task.setOnSucceeded(e->{
                showSuccess("Amount Paid Inserted Successfully");
                load.ButtonFinished(setAmount);
                cancelled = false;
                dialogStage.close();
            });
            task.setOnFailed(e->{
                showError("Invalid amount format");
                load.ButtonFinished(setAmount);
                task.getException().printStackTrace();
            });
            new Thread(task).start();
        });
    }
}
