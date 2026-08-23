package com.gestionpaiements.app.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gestionpaiements.app.MainApp;
import com.gestionpaiements.app.model.TypePaiement;
import com.gestionpaiements.app.service.ExcelFileManagerService;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.scene.control.Button;



@Component
public class MainViewController {

    @FXML
    private Label statusLabel;



    @FXML
    private Button addPaiementButton;   // ✅ ajouté

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
    @Autowired
    private ExcelFileManagerService excelFileManagerService;

    @FXML
    private void handleExport() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Exporter le fichier Excel actif");
            fileChooser.setInitialFileName("Paiements_export.xlsx");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));
            File destination = fileChooser.showSaveDialog(addPaiementButton.getScene().getWindow());
            if (destination != null) {
                excelFileManagerService.exportActiveFile(destination);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText(null);
                alert.setContentText("Le fichier a été exporté avec succès.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'exporter le fichier : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleArchives() {
        try {
            List<File> archives = excelFileManagerService.getArchiveFiles();
            StringBuilder sb = new StringBuilder();
            if (archives.isEmpty()) {
                sb.append("Aucun fichier archivé pour le moment.");
            } else {
                for (File f : archives) {
                    sb.append(f.getName()).append("\n");
                }
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fichiers archivés");
            alert.setHeaderText(null);
            alert.setContentText(sb.toString());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de lister les archives : " + e.getMessage());
            alert.showAndWait();
        }
    }

    }