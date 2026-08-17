package com.gestionpaiements.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * JavaFX main application class that initializes the Spring context.
 */
@SpringBootApplication
public class MainApp extends Application {

    private ApplicationContext applicationContext;

    @Override
    public void init() {
        // Initialize Spring context
        applicationContext = new SpringApplicationBuilder()
                .sources(MainApp.class)
                .run(getParameters().getRaw().toArray(new String[0]));
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