package com.mycompany.reservationsystem.controller.popup;

import com.mycompany.reservationsystem.App;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ConnectionFailedController {
    
    private Stage dialogStage;
    private Runnable onReconnect;
    private Runnable onLogout;
    
    @FXML
    private javafx.scene.control.Button reconnectBtn;
    @FXML
    private javafx.scene.control.Button logoutBtn;
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setOnReconnect(Runnable onReconnect) {
        this.onReconnect = onReconnect;
    }
    
    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }
    
    @FXML
    private void reconnect() {
        if (onReconnect != null) {
            onReconnect.run();
        }
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
    
    @FXML
    private void logout() {
        if (onLogout != null) {
            onLogout.run();
        }
    }
    
    public static void show(Runnable onReconnect, Runnable onLogout) {
        Platform.runLater(() -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    ConnectionFailedController.class.getResource("/fxml/popup/ConnectionFailed.fxml")
                );
                Parent root = loader.load();
                
                ConnectionFailedController controller = loader.getController();
                
                Stage stage = new Stage();
                controller.setDialogStage(stage);
                controller.setOnReconnect(() -> {
                    stage.close();
                    onReconnect.run();
                });
                controller.setOnLogout(() -> {
                    stage.close();
                    onLogout.run();
                });
                
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.initModality(Modality.APPLICATION_MODAL);
                
                if (App.primaryStage != null) {
                    stage.initOwner(App.primaryStage);
                }
                
                stage.setResizable(false);
                stage.centerOnScreen();
                
                Scene scene = new Scene(root);
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                stage.setScene(scene);
                stage.show();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
