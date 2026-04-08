package com.mycompany.reservationsystem.controller.popup;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.util.ButtonLoad;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import static com.mycompany.reservationsystem.model.User.Position.ADMINISTRATOR;
import static com.mycompany.reservationsystem.util.FieldRestrictions.applyLettersOnly;
import static com.mycompany.reservationsystem.util.FieldValidators.markInvalid;
import static com.mycompany.reservationsystem.util.FieldValidators.validateRequired;

@Component
public class editAccountController {
    private User editUser;

    public void setEdituser(User edituser){
        this.editUser = edituser;
        firstname.setText(editUser.getFirstname());
        lastname.setText(editUser.getLastname());
        username.setText(editUser.getUsername());
        password.setText("");
        password.setPromptText("Leave blank to keep current password");
        position.selectItem(editUser.getPosition());
        position.setDisable(editUser.getPosition() == ADMINISTRATOR);
    }

    @FXML
    private MFXButton submit;

    @FXML
    private MFXButton cancel;

    @FXML
    private MFXTextField firstname;

    @FXML
    private MFXTextField lastname;

    @FXML
    private Label messageLabel;

    @FXML
    private MFXPasswordField password;

    @FXML
    private MFXComboBox<User.Position> position;

    @FXML
    private MFXTextField username;

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
        cancel.setOnAction(e ->dialogStage.close());

        applyLettersOnly(firstname);
        applyLettersOnly(lastname);
        position.setItems(FXCollections.observableArrayList(User.Position.values()));

        submit.setOnAction(e ->{
            if (!validateRequired(firstname)) {showError("Please Insert Firstname"); return;}
            if (!validateRequired(lastname)) {showError("Please Insert Lastname"); return;}
            if (!validateRequired(username)){showError("Please Insert Username"); return;}
            if (!validateRequired(password)){showError("Please Insert Password"); return;}
            if (position.getValue() == User.Position.ADMINISTRATOR) {
                markInvalid(position);
                showError("Cannot assign ADMINISTRATOR role");
                return;
            }

            ButtonLoad load = new ButtonLoad();
            load.ButtonStart(submit);

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    editUser.setFirstname(firstname.getText());
                    editUser.setLastname(lastname.getText());
                    editUser.setUsername(username.getText());
                    String pw = password.getText();
                    if (pw != null && !pw.isEmpty()) {
                        editUser.setPassword(pw);
                    }
                    editUser.setPosition(position.getValue());
                    
                    ApiClient.updateUser(editUser.getId(), editUser);
                    return null;
                }
            };
            task.setOnSucceeded(f ->{
                showSuccess("Account Updated Successfully");
                dialogStage.close();
                load.ButtonFinished(submit);
            });
            task.setOnFailed(f->{
                showError("Please Check Your Internet Connection");
                load.ButtonFinished(submit);
            });
            new Thread(task).start();
        });
    }
}
