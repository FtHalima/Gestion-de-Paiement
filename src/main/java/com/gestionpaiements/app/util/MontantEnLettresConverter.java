package com.gestionpaiements.app.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Utility class to convert monetary amounts to French words.
 * Supports amounts with two decimal places (dirhams and centimes).
 */
public class MontantEnLettresConverter {

    private static final String[] UNITES = {
        "", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf",
        "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize", "dix-sept",
        "dix-huit", "dix-neuf"
    };

    private static final String[] DIZAINES = {
        "", "", "vingt", "trente", "quarante", "cinquante", "soixante",
        "soixante-dix", "quatre-vingts", "quatre-vingt-dix"
    };

    private MontantEnLettresConverter() {
        // Utility class - prevent instantiation
    }

    /**
     * Converts a monetary amount to French words.
     * Format: X dirhams Y centimes (with proper French grammar)
     *
     * @param montant the amount to convert (null or negative treated as zero)
     * @return the amount in French words
     */
    public static String convertir(BigDecimal montant) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            return "zéro dirham";
        }

        // Split into dirhams and centimes
        BigDecimal dirhamsBig = montant.setScale(0, BigDecimal.ROUND_DOWN);
        BigDecimal centimesBig = montant.subtract(dirhamsBig)
                .multiply(new BigDecimal(100))
                .setScale(0, BigDecimal.ROUND_UP);

        long dirhams = dirhamsBig.longValue();
        int centimes = centimesBig.intValue();

        // Build the dirhams part
        String dirhamsEnLettres = convertirNombreEnLettres(dirhams);
        String dirhamsPart;
        if (dirhams == 1) {
            dirhamsPart = "un dirham";
        } else {
            dirhamsPart = dirhamsEnLettres + " dirhams";
        }

        // Build the centimes part if needed
        if (centimes == 0) {
            return dirhamsPart;
        }

        String centimesEnLettres = convertirNombreEnLettres(centimes);
        String centimesPart;
        if (centimes == 1) {
            centimesPart = "un centime";
        } else {
            centimesPart = centimesEnLettres + " centimes";
        }

        return dirhamsPart + " et " + centimesPart;
    }

    /**
     * Converts an integer to French words (supports 0 to 999,999,999).
     *
     * @param nombre the number to convert
     * @return the number in French words
     */
    private static String convertirNombreEnLettres(long nombre) {
        if (nombre == 0) {
            return "zéro";
        }

        StringBuilder resultat = new StringBuilder();

        // Handle billions (not needed for typical amounts but keeping for completeness)
        if (nombre >= 1_000_000_000) {
            long milliards = nombre / 1_000_000_000;
            resultat.append(convertirNombreEnLettres(milliards)).append(" milliard");
            if (milliards > 1) {
                resultat.append("s");
            }
            nombre %= 1_000_000_000;
            if (nombre > 0) {
                resultat.append(" ");
            }
        }

        // Handle millions
        if (nombre >= 1_000_000) {
            long millions = nombre / 1_000_000;
            if (millions > 1) {
                resultat.append(convertirNombreEnLettres(millions)).append(" millions");
            } else {
                resultat.append("un million");
            }
            nombre %= 1_000_000;
            if (nombre > 0) {
                resultat.append(" ");
            }
        }

        // Handle thousands
        if (nombre >= 1_000) {
            long milliers = nombre / 1_000;
            resultat.append(convertirNombreEnLettres(milliers)).append(" mille");
            nombre %= 1_000;
            if (nombre > 0) {
                resultat.append(" ");
            }
        }

        // Handle hundreds
        if (nombre >= 100) {
            long centaines = nombre / 100;
            if (centaines > 1) {
                resultat.append(convertirNombreEnLettres(centaines)).append(" cent");
            } else {
                resultat.append("cent");
            }
            nombre %= 100;
            if (nombre > 0) {
                resultat.append(" ");
            }
        }

        // Handle tens and units
        if (nombre >= 20 || nombre < 0) { // Actually nombre < 20 here
            if (nombre >= 20 && nombre < 70) {
                resultat.append(DIZAINES[(int) (nombre / 10)]);
                long reste = nombre % 10;
                if (reste > 0) {
                    resultat.append("-").append(UNITES[(int) reste]);
                }
            } else if (nombre >= 70 && nombre < 80) {
                resultat.append("soixante");
                long reste = nombre - 60;
                if (reste > 0) {
                    resultat.append("-").append(UNITES[(int) reste]);
                }
            } else if (nombre >= 80 && nombre < 90) {
                if (nombre == 80) {
                    resultat.append("quatre-vingts");
                } else {
                    resultat.append("quatre-vingt-");
                    resultat.append(UNITES[(int) (nombre - 80)]);
                }
            } else if (nombre >= 90 && nombre < 100) {
                resultat.append("quatre-vingt-dix");
                long reste = nombre - 90;
                if (reste > 0) {
                    resultat.append("-").append(UNITES[(int) reste]);
                }
            } else { // nombre < 20
                resultat.append(UNITES[(int) nombre]);
            }
        } else if (nombre > 0) { // nombre between 1 and 19
            resultat.append(UNITES[(int) nombre]);
        }

        return resultat.toString().trim();
    }
}