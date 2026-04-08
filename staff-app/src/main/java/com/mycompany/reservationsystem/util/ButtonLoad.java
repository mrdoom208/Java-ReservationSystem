package com.mycompany.reservationsystem.util;

import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;

public class ButtonLoad {
    public ButtonLoad (){}
    String text;
    public void ButtonStart(Button button){
        text = button.getText();
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(20,20);
        button.setText("");
        button.setGraphic(progress);
        button.setMouseTransparent(true);
        button.setFocusTraversable(false);
    }
    public void ButtonFinished(Button button){
        button.setText(text);
        button.setGraphic(null);
        button.setMouseTransparent(false);
        button.setFocusTraversable(true);
    }
}
