package com.gestionpaiements.app;

/**
 * Simple launcher for JavaFX + Spring.
 * Does NOT extend Application to avoid 'missing JavaFX components' error when running with spring-boot:run.
 */
public class Launcher {
    public static void main(String[] args) {
        // Launch JavaFX application
        javafx.application.Application.launch(MainApp.class, args);
    }
}