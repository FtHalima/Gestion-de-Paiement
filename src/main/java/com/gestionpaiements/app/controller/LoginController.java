package com.gestionpaiements.app.controller;

import com.gestionpaiements.app.model.Utilisateur;
import com.gestionpaiements.app.service.SessionUtilisateur;
import com.gestionpaiements.app.service.UtilisateurService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import com.gestionpaiements.app.MainApp;

/**
 * Contrôleur de la vue de connexion.
 */
@Component
public class LoginController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private SessionUtilisateur sessionUtilisateur;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField motDePasseField;

    @FXML
    private Button loginButton;
    

    @FXML
    private void handleLogin() {
        String login = loginField.getText().trim();
        String motDePasse = motDePasseField.getText();

        if (login.isEmpty() || motDePasse.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        utilisateurService.authentifier(login, motDePasse)
                .ifPresentOrElse(
                        utilisateur -> {
                            sessionUtilisateur.setUtilisateurConnecte(utilisateur);
                            // Navigate to main view
                            try {
                                // Close login window
                                Stage loginStage = (Stage) loginButton.getScene().getWindow();
                                // Load main view
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionpaiements/app/fxml/MainView.fxml"));
                                loader.setControllerFactory(MainApp.staticApplicationContext::getBean);  // ✅ ligne ajoutée
                                Parent root = loader.load();
                                com.gestionpaiements.app.util.AppTheme.appliquer(root);
                                Stage mainStage = new Stage();
                                mainStage.setTitle("Gestion Paiements");
                                mainStage.setScene(new Scene(root));
                                mainStage.show();
                                mainStage.setMaximized(true);
                                loginStage.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                                showError("Erreur lors du chargement de l'interface principale : " + e.getMessage());
                            }
                        },
                        () -> showError("Nom d'utilisateur ou mot de passe incorrect.")
                );
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de connexion");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
