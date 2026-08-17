package com.gestionpaiements.app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

/**
 * Controller for the main view.
 */
@Component
public class MainViewController {

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        statusLabel.setText("OK");
    }
}