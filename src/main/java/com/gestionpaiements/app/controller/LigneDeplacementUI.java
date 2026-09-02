package com.gestionpaiements.app.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LigneDeplacementUI {

    private final StringProperty dateDepart = new SimpleStringProperty("");
    private final StringProperty dateArrivee = new SimpleStringProperty("");
    private final StringProperty parcours = new SimpleStringProperty("");
    private final StringProperty heureDepart = new SimpleStringProperty("");
    private final StringProperty heureRetour = new SimpleStringProperty("");
    private final StringProperty nombreTauxBase = new SimpleStringProperty("");
    private final StringProperty tauxBaseApplique = new SimpleStringProperty("");
    private final StringProperty montant = new SimpleStringProperty("0,00");

    public StringProperty dateDepartProperty() { return dateDepart; }
    public String getDateDepart() { return dateDepart.get(); }
    public void setDateDepart(String v) { dateDepart.set(v); }

    public StringProperty dateArriveeProperty() { return dateArrivee; }
    public String getDateArrivee() { return dateArrivee.get(); }
    public void setDateArrivee(String v) { dateArrivee.set(v); }

    public StringProperty parcoursProperty() { return parcours; }
    public String getParcours() { return parcours.get(); }
    public void setParcours(String v) { parcours.set(v); }

    public StringProperty heureDepartProperty() { return heureDepart; }
    public String getHeureDepart() { return heureDepart.get(); }
    public void setHeureDepart(String v) { heureDepart.set(normalizeHeure(v)); }

    public StringProperty heureRetourProperty() { return heureRetour; }
    public String getHeureRetour() { return heureRetour.get(); }
    public void setHeureRetour(String v) { heureRetour.set(normalizeHeure(v)); }

    public StringProperty nombreTauxBaseProperty() { return nombreTauxBase; }
    public String getNombreTauxBase() { return nombreTauxBase.get(); }
    public void setNombreTauxBase(String v) { nombreTauxBase.set(v); }

    public StringProperty tauxBaseAppliqueProperty() { return tauxBaseApplique; }
    public String getTauxBaseApplique() { return tauxBaseApplique.get(); }
    public void setTauxBaseApplique(String v) { tauxBaseApplique.set(v); }

    public StringProperty montantProperty() { return montant; }
    public String getMontant() { return montant.get(); }
    public void setMontant(String v) { montant.set(v); }

    // Si l'utilisateur tape juste des chiffres (ex: "22"), ajoute automatiquement "H"
    private String normalizeHeure(String v) {
        if (v == null) return "";
        String trimmed = v.trim();
        if (trimmed.matches("\\d{1,2}")) {
            return trimmed + "H";
        }
        return trimmed;
    }
}