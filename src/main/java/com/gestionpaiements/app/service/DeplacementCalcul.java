package com.gestionpaiements.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/** Barème des indemnités de déplacement selon le grade numérique (échelle). */
public final class DeplacementCalcul {
    private DeplacementCalcul() {}

    public static BigDecimal nombreTaux(LocalDate depart, LocalDate arrivee,
                                        LocalTime heureDepart, LocalTime heureRetour) {
        if (depart == null || arrivee == null || heureDepart == null || heureRetour == null
                || arrivee.isBefore(depart)) return null;
        int debut = heureDepart.toSecondOfDay();
        int fin = heureRetour.toSecondOfDay();
        long jours = ChronoUnit.DAYS.between(depart, arrivee);
        if (jours == 0) {
            return fin < debut ? null : BigDecimal.valueOf(compterCreneaux(debut, fin));
        }
        // Le déplacement est continu : premier et dernier jours partiels,
        // trois créneaux pour chaque journée entièrement passée en mission.
        return BigDecimal.valueOf(compterCreneaux(debut, 24 * 3600)
                + 3 * (jours - 1) + compterCreneaux(0, fin));
    }

    private static int compterCreneaux(int debut, int fin) {
        int taux = 0;
        for (int[] plage : new int[][]{{0, 5 * 3600}, {11 * 3600, 14 * 3600}, {18 * 3600, 21 * 3600}}) {
            if (debut < plage[1] && fin > plage[0] && fin > debut) taux++;
        }
        return taux;
    }

    public static BigDecimal tauxBase(Integer grade) {
        if (grade == null) return null;
        return switch (grade) {
            case 12 -> BigDecimal.valueOf(100);
            case 11 -> BigDecimal.valueOf(80);
            case 8, 9, 10 -> BigDecimal.valueOf(60);
            case 6, 7 -> BigDecimal.valueOf(40);
            case 5 -> BigDecimal.valueOf(30);
            default -> null;
        };
    }
}
