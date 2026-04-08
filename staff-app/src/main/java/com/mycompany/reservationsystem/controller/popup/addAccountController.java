package com.mycompany.reservationsystem.controller.popup;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.model.User;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;

import static com.mycompany.reservationsystem.util.FieldRestrictions.applyLettersOnly;
import static com.mycompany.reservationsystem.util.FieldValidators.markInvalid;
import static com.mycompany.reservationsystem.util.FieldValidators.validateRequired;

@Component
public class addAccountController {
    @FXML
    private MFXButton addAccount;

    @FXML
    private MFXButton cancel;

    @FXML
    private MFXTextField firstname;

    @FXML
    private MFXTextField lastname;

    @FXML
    private MFXPasswordField password;

    @FXML
    private MFXComboBox<User.Position> position;

    @FXML
    private MFXTextField username;

    @FXML
    private Label messageLabel;

    private Stage dialogStage;

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
    private void initialize(){
        cancel.setOnAction(e -> dialogStage.close());
        applyLettersOnly(firstname);
        applyLettersOnly(lastname);

        position.setItems(FXCollections.observableArrayList(User.Position.values()));
        position.selectItem(User.Position.STAFF);

        addAccount.setOnAction(e ->{
            ProgressIndicator progress = new ProgressIndicator();

            if (!validateRequired(firstname)) {showError("Please Insert Firstname"); return;}
            if (!validateRequired(lastname)) {showError("Please Insert Lastname"); return;}
            if (!validateRequired(username)){showError("Please Insert Username"); return;}
            if (!validateRequired(password)){showError("Please Insert Password"); return;}
            if (position.getValue() == User.Position.ADMINISTRATOR) {
                markInvalid(position);
                showError("Cannot assign ADMINISTRATORISTRATOR role");
                return;
            }
            addAccount.setGraphic(progress);
            addAccount.setText(null);
            addAccount.setFocusTraversable(false);
            addAccount.setMouseTransparent(true);
            Task<User> task = new Task<User>() {
                @Override
                protected User call() throws Exception {
                    User newuser = new User();
                    newuser.setFirstname(firstname.getText());
                    newuser.setLastname(lastname.getText());
                    newuser.setUsername(username.getText());
                    newuser.setPassword(password.getText());
                    newuser.setPosition(position.getValue());
                    newuser.setStatus("Active");
                    
                    ApiClient.createUser(newuser);
                    return newuser;
                }
            };
            task.setOnSucceeded(f->{
                showSuccess("Account Added Successfully");
                addAccount.setFocusTraversable(false);
                addAccount.setMouseTransparent(true);
                addAccount.setText("Add Account");
                dialogStage.close();
            });
            task.setOnFailed(f->{
                task.getException().printStackTrace();
            });
            new Thread(task).start();
        });
    }
}
