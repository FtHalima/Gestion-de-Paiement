package com.gestionpaiements.app.util;

import javafx.scene.Parent;

/** Apparence moderne unique, indépendante des anciennes préférences enregistrées. */
public final class AppTheme {
    private AppTheme() {}

    public static void appliquer(Parent root) {
        if (!root.getStyleClass().contains("modern")) root.getStyleClass().add("modern");
        String css = AppTheme.class.getResource("/com/gestionpaiements/app/fxml/modern.css").toExternalForm();
        if (!root.getStylesheets().contains(css)) root.getStylesheets().add(css);
    }
}
