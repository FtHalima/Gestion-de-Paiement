package com.gestionpaiements.app.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import com.gestionpaiements.app.MainApp;
import com.gestionpaiements.app.model.TypePaiement;

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
            loader.setControllerFactory(MainApp.staticApplicationContext::getBean);
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

    public void handleVacataire() {
        showListe(TypePaiement.VACATAIRE);
    }

    public void handleHeureSup() {
        showListe(TypePaiement.HEURE_SUP);
    }

    public void handleDeplacement() {
        showListe(TypePaiement.DEPLACEMENT);
    }

    private void showListe(TypePaiement type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionpaiements/app/fxml/PaiementList.fxml"));
            loader.setControllerFactory(MainApp.staticApplicationContext::getBean);
            Parent root = loader.load();
            PaiementListController controller = loader.getController();
            controller.setType(type);

            Stage stage = new Stage();
            String title = type.getLibellePdf() + "s"; // e.g., "Vacataires"
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}