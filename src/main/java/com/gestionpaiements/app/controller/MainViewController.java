package com.gestionpaiements.app.controller;

import com.gestionpaiements.app.MainApp;
import com.gestionpaiements.app.model.TypePaiement;
import com.gestionpaiements.app.service.ExcelFileManagerService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

import javafx.stage.Stage;
import javafx.scene.Scene;
import com.gestionpaiements.app.service.SessionUtilisateur;

@Component
public class MainViewController {

    @Autowired
    private ExcelFileManagerService excelFileManagerService;

    @Autowired
    private SessionUtilisateur sessionUtilisateur;

    @FXML private StackPane contentArea;

    @FXML private Button navDashboard;
    @FXML private Button navAjouter;
    @FXML private Button navVacataire;
    @FXML private Button navHeureSup;
    @FXML private Button navDeplacement;

    @FXML
    public void initialize() {
        showDashboard();
    }

    private void loadIntoContent(String fxmlPath, Button activeButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(MainApp.staticApplicationContext::getBean);
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            setActiveNav(activeButton);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible de charger la vue : " + e.getMessage());
        }
    }

    private void loadListeIntoContent(TypePaiement type, Button activeButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionpaiements/app/fxml/ExcelFilesView.fxml"));
            loader.setControllerFactory(MainApp.staticApplicationContext::getBean);
            Parent view = loader.load();
            ExcelFilesController controller = loader.getController();
            controller.setType(type);
            controller.setMainViewController(this);
            contentArea.getChildren().setAll(view);
            setActiveNav(activeButton);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible de charger la liste : " + e.getMessage());
        }
    }

    private void setActiveNav(Button active) {
        for (Button b : new Button[]{navDashboard, navAjouter, navVacataire, navHeureSup, navDeplacement}) {
            b.getStyleClass().remove("nav-button-active");
        }
        if (active != null) {
            active.getStyleClass().add("nav-button-active");
        }
    }

    @FXML
    public void showDashboard() {
        loadIntoContent("/com/gestionpaiements/app/fxml/DashboardView.fxml", navDashboard);
    }

    @FXML
    public void showAjouterPaiement() {
        loadIntoContent("/com/gestionpaiements/app/fxml/AjouterPaiement.fxml", navAjouter);
    }

    @FXML
    public void showVacataire() {
        loadListeIntoContent(TypePaiement.VACATAIRE, navVacataire);
    }

    @FXML
    public void showHeureSup() {
        loadListeIntoContent(TypePaiement.HEURE_SUP, navHeureSup);
    }

    @FXML
    public void showDeplacement() {
        loadListeIntoContent(TypePaiement.DEPLACEMENT, navDeplacement);
    }

    @FXML
    private void handleLogout() {
        sessionUtilisateur.setUtilisateurConnecte(null);
        Stage currentStage = (Stage) contentArea.getScene().getWindow();
        currentStage.close();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionpaiements/app/fxml/login.fxml"));
            loader.setControllerFactory(MainApp.staticApplicationContext::getBean);
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Connexion");
            loginStage.setScene(new Scene(root));
            loginStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showAjouterPaiementAndSearch(String cin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionpaiements/app/fxml/AjouterPaiement.fxml"));
            loader.setControllerFactory(MainApp.staticApplicationContext::getBean);
            Parent view = loader.load();
            AjouterPaiementController controller = loader.getController();
            contentArea.getChildren().setAll(view);
            setActiveNav(navAjouter);
            controller.prefillAndSearch(cin);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }
}