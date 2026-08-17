package com.gestionpaiements.app.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainViewController {

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        statusLabel.setText("OK");
    }

    @FXML
    private void handleAddPaiement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionpaiements/app/fxml/AjouterPaiement.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un paiement");
            stage.setScene(new Scene(root));
            // Slightly reduce the height of the window
            stage.setHeight(600.0);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}