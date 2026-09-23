package com.gestionpaiements.app.service;

import com.gestionpaiements.app.model.LigneDeplacement;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class DeplacementDisponibilite {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DeplacementDisponibilite() {}

    // Les dates de départ et de retour sont incluses dans la période occupée.
    public static boolean chevauche(LigneDeplacement a, LigneDeplacement b) {
        return a.getDateDepart() != null && a.getDateArrivee() != null
                && b.getDateDepart() != null && b.getDateArrivee() != null
                && !a.getDateDepart().isAfter(b.getDateArrivee())
                && !b.getDateDepart().isAfter(a.getDateArrivee());
    }

    public static void verifier(List<LigneDeplacement> trajets, List<LigneDeplacement> existants) {
        for (int i = 0; i < trajets.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (chevauche(trajets.get(i), trajets.get(j))) refuser(i, trajets.get(j));
            }
            for (LigneDeplacement existant : existants) {
                if (chevauche(trajets.get(i), existant)) refuser(i, existant);
            }
        }
    }

    private static void refuser(int index, LigneDeplacement occupe) {
        throw new IllegalArgumentException("Trajet " + (index + 1)
                + " : ce professeur est déjà en déplacement du "
                + occupe.getDateDepart().format(DATE) + " au " + occupe.getDateArrivee().format(DATE)
                + ". Veuillez modifier la date de départ ou la date d’arrivée pour choisir une période libre.");
    }
}
