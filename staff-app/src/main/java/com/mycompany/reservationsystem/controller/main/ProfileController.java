package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.model.User;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import static com.mycompany.reservationsystem.util.FieldRestrictions.applyLettersOnly;
import static com.mycompany.reservationsystem.util.FieldValidators.markInvalid;
import static com.mycompany.reservationsystem.util.FieldValidators.validateRequired;

@Component
public class ProfileController {

    private AdministratorUIController administratorUIController;

    private User currentuser;

    private String originalPassword;

    public void setCurrentuser(User currentuser) {
        this.currentuser = currentuser;
        position.setText(currentuser.getPosition().toString());
        firstname.setText(currentuser.getFirstname() != null ? currentuser.getFirstname() : "");
        lastname.setText(currentuser.getLastname() != null ? currentuser.getLastname() : "");
        username.setText(currentuser.getUsername() != null ? currentuser.getUsername() : "");
        originalPassword = currentuser.getPassword();
        password.setText("");
        password.setPromptText("Leave blank to keep current password");
    }
    private Stage DialogStage;

    public void setDialogStage(Stage stage) {
        this.DialogStage = stage;
    }

    @FXML
    private Label position;
    @FXML
    private MFXButton cancel;

    @FXML
    private MFXTextField firstname;

    @FXML
    private MFXTextField lastname;

    @FXML
    private MFXPasswordField password;

    @FXML
    private MFXButton save;

    @FXML
    private MFXTextField username;

    public void setAdministratorUIController(AdministratorUIController controller) {
        this.administratorUIController = controller;
    }

    @FXML
    private void initialize(){

        applyLettersOnly(firstname);
        applyLettersOnly(lastname);

        cancel.setOnAction(event -> DialogStage.close());

        save.setOnAction(event -> {
            if (!validateRequired(firstname)) {markInvalid(firstname); return;}
            if (!validateRequired(lastname)) {markInvalid(lastname); return;}
            if (!validateRequired(username)){markInvalid(username); return;}

            String fname = firstname.getText();
            String lname = lastname.getText();
            String uname = username.getText();
            
            currentuser.setFirstname(fname != null && !fname.isBlank() ? fname : currentuser.getFirstname());
            currentuser.setLastname(lname != null && !lname.isBlank() ? lname : currentuser.getLastname());
            currentuser.setUsername(uname != null && !uname.isBlank() ? uname : currentuser.getUsername());
            
            String newPassword = password.getText();
            if (newPassword != null && !newPassword.isEmpty()) {
                currentuser.setPassword(newPassword);
            }
            
            ApiClient.updateUser(currentuser.getId(), currentuser);
            administratorUIController.setUser(currentuser);
            DialogStage.close();
        });
    }
}
