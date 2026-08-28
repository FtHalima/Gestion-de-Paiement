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

@Component
public class MainViewController {

    @Autowired
    private ExcelFileManagerService excelFileManagerService;

    @FXML private StackPane contentArea;

    @FXML private Button navDashboard;
    @FXML private Button navAjouter;
    @FXML private Button navVacataire;
    @FXML private Button navHeureSup;
    @FXML private Button navDeplacement;
    @FXML private Button navArchives;

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

    private void setActiveNav(Button active) {
        for (Button b : new Button[]{navDashboard, navAjouter, navVacataire, navHeureSup, navDeplacement, navArchives}) {
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
    public void showAnciensFichiers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionpaiements/app/fxml/ArchivesView.fxml"));
            loader.setControllerFactory(MainApp.staticApplicationContext::getBean);
            Parent view = loader.load();
            ArchivesController controller = loader.getController();
            controller.setOnBack(this::showDashboard);
            contentArea.getChildren().setAll(view);
            setActiveNav(navArchives);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible de charger les archives : " + e.getMessage());
        }
    }



    @FXML
    private void handleLogout() {
        // À adapter selon ta logique de déconnexion existante
    }

    private void loadListeIntoContent(TypePaiement type, Button activeButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionpaiements/app/fxml/PaiementList.fxml"));
            loader.setControllerFactory(MainApp.staticApplicationContext::getBean);
            Parent view = loader.load();
            PaiementListController controller = loader.getController();
            controller.setType(type);
            controller.setOnBack(this::showDashboard);  // ← c'est cette ligne dont tu parles
            contentArea.getChildren().setAll(view);
            setActiveNav(activeButton);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible de charger la liste : " + e.getMessage());
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
}