package com.gestionpaiements.app.util;

import javafx.scene.Parent;
import java.util.prefs.Preferences;

/** Le thème classique conserve la feuille de style d'origine. */
public final class AppTheme {
    // Cette version démarre en moderne, même si l'ancien thème avait été mémorisé.
    // Les changements explicites faits ensuite par l'utilisateur restent conservés.
    private static final String PREFERENCE_THEME = "themeModerne.v2";
    private static boolean moderne = lirePreference();
    private static final javafx.beans.property.ReadOnlyBooleanWrapper themeModerne =
            new javafx.beans.property.ReadOnlyBooleanWrapper(moderne);
    private AppTheme() {}

    private static boolean lirePreference() {
        try { return Preferences.userNodeForPackage(AppTheme.class).getBoolean(PREFERENCE_THEME, true); }
        catch (SecurityException e) { return true; }
    }

    public static boolean estModerne() { return moderne; }

    public static javafx.beans.property.ReadOnlyBooleanProperty moderneProperty() {
        return themeModerne.getReadOnlyProperty();
    }

    public static void appliquer(Parent root) {
        root.getStyleClass().remove("modern");
        if (moderne) root.getStyleClass().add("modern");
        String css = AppTheme.class.getResource("/com/gestionpaiements/app/fxml/modern.css").toExternalForm();
        if (!root.getStylesheets().contains(css)) root.getStylesheets().add(css);
    }

    public static void choisir(Parent root, boolean nouveauTheme) {
        moderne = nouveauTheme;
        themeModerne.set(nouveauTheme);
        appliquer(root);
        try { Preferences.userNodeForPackage(AppTheme.class).putBoolean(PREFERENCE_THEME, moderne); }
        catch (SecurityException ignored) { /* Le choix reste actif pour la session. */ }
    }
}
