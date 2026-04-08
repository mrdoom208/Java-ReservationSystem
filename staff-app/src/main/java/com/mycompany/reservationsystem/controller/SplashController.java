package com.mycompany.reservationsystem.controller;

import com.mycompany.reservationsystem.transition.SplashTransition;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class SplashController {

    @FXML
    private StackPane splashRoot;

    @FXML
    private ImageView logoImage;

    @FXML
    private Text titleText;

    @FXML
    private Text subtitleText;

    @FXML
    private VBox loadingContainer;

    @FXML
    public void initialize() {
        SplashTransition.animateSplash(splashRoot);
        SplashTransition.animateLogo(logoImage);
        SplashTransition.animateText(titleText, 300);
        SplashTransition.animateText(subtitleText, 500);
        SplashTransition.animateElement(loadingContainer, 700);
    }
}
