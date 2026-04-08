package com.mycompany.reservationsystem.util.dialog;

import com.mycompany.reservationsystem.controller.popup.DeleteDialogController;
import com.mycompany.reservationsystem.transition.NodeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DeleteDialog {
    public static void show(String message, Runnable onDelete) {
        show(null, message, onDelete);
    }

    public static void show(Stage owner, String message, Runnable onDelete) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    DeleteDialog.class.getResource(
                            "/fxml/popup/deleteDialog.fxml"
                    )
            );


            Parent root = loader.load();
            DeleteDialogController controller = loader.getController();
            controller.setOnDelete(onDelete);
            controller.setMessage(message);


            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) {
                stage.initOwner(owner);
            }

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
            NodeTransition.showSmooth(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
