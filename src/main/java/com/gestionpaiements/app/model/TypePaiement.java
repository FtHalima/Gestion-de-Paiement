package com.gestionpaiements.app.model;

public enum TypePaiement {
    VACATAIRE,
    HEURE_SUP,
    DEPLACEMENT;

    /**
     * Returns the PDF title corresponding to this payment type.
     * @return the PDF title as a String
     */
    public String getLibellePdf() {
        return switch (this) {
            case VACATAIRE -> "Vacataire";
            case HEURE_SUP -> "Heure Supérieure";
            case DEPLACEMENT -> "Déplacement";
        };
    }
}