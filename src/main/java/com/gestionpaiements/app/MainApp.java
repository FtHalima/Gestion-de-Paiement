package com.gestionpaiements.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

/**
 * JavaFX main application class that initializes the Spring context.
 */
public class MainApp extends Application {

    private ApplicationContext applicationContext;

    @Override
    public void init() {
        // Initialize Spring context
        String[] args = getParameters().getUnnamed().toArray(new String[0]);
        applicationContext = new SpringApplicationBuilder()
                .sources(Launcher.class)
                .run(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Use Spring's ApplicationContext as the controller factory for FXMLLoader
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Gestion Paiements");
        stage.show();
    }

    @Override
    public void stop() {
        // Close Spring context
        // applicationContext.close(); // Spring Boot handles this automatically
    }

    public static void main(String[] args) {
        launch(args);
    }
}