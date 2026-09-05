package com.gestionpaiements.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.gestionpaiements.app.service.UtilisateurService;
import com.gestionpaiements.app.dao.UtilisateurRepository;

/**
 * JavaFX main application class that initializes the Spring context.
 */
@SpringBootApplication
public class MainApp extends Application {

    private ApplicationContext applicationContext;
    public static ApplicationContext staticApplicationContext;

    @Override
    public void init() {
        // Garantit que le dossier de la base H2 existe avant que Spring ne tente d'y accéder
        java.io.File dbDir = new java.io.File(System.getProperty("user.home"), "GestionPaiements/data");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }

        applicationContext = new SpringApplicationBuilder()
                .sources(MainApp.class)
                .run(getParameters().getRaw().toArray(new String[0]));
        staticApplicationContext = applicationContext;
        // Crée un utilisateur par défaut au tout premier démarrage (base vide)
        UtilisateurRepository utilisateurRepository = applicationContext.getBean(UtilisateurRepository.class);
        if (utilisateurRepository.count() == 0) {
            UtilisateurService utilisateurService = applicationContext.getBean(UtilisateurService.class);
            utilisateurService.creerCompte("admin", "admin123");
            System.out.println(">>> Utilisateur par défaut créé : login=admin, mot de passe=admin123");
        }
    }

    @Override
    public void start(Stage stage) {
        try {
            // Load login.fxml using Spring's ApplicationContext as controller factory
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionpaiements/app/fxml/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Close Spring context
        if (applicationContext != null) {
            ((org.springframework.context.ConfigurableApplicationContext) applicationContext).close();
        }
    }
}