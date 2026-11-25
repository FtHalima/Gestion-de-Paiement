package com.gestionpaiements.app.service;

import com.gestionpaiements.app.dao.PaiementRepository;
import com.gestionpaiements.app.model.*;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.Optional;

/** Standalone regression checks: run main with the application's dependency classpath. */
public class DeplacementRegression {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        int[] expected = {30, 40, 40, 60, 60, 60, 80, 100};
        for (int grade = 5; grade <= 12; grade++)
            check(DeplacementCalcul.tauxBase(grade).intValueExact() == expected[grade - 5], "Grade " + grade);
        check(DeplacementCalcul.tauxBase(null) == null && DeplacementCalcul.tauxBase(4) == null, "Grade absent/hors barème");
        LocalDate date = LocalDate.of(2026, 9, 20);
        for (int[] example : new int[][]{{4,6,1}, {12,13,1}, {5,11,0}, {6,10,0}, {0,5,1}, {11,14,1}, {18,21,1}, {14,18,0}, {4,22,3}, {12,12,0}})
            check(DeplacementCalcul.nombreTaux(date, date, LocalTime.of(example[0],0), LocalTime.of(example[1],0))
                    .intValueExact() == example[2], "Créneau " + example[0] + "-" + example[1]);
        check(DeplacementCalcul.nombreTaux(date, date.plusDays(8), LocalTime.of(4,0), LocalTime.of(22,0))
                .intValueExact() == 27, "Exemple 9 jours = 27 taux");
        LocalDate octobre = LocalDate.of(2026, 10, 1);
        check(DeplacementCalcul.nombreTaux(octobre, octobre.plusDays(2), LocalTime.of(4,0), LocalTime.of(15,0))
                .intValueExact() == 8, "1-3 octobre : 3 + 3 + 2 = 8 taux");
        check(DeplacementCalcul.nombreTaux(date, date.plusDays(2), LocalTime.of(15,0), LocalTime.of(10,0))
                .intValueExact() == 5, "Jours partiels : 1 + 3 + 1 = 5 taux");
        check(DeplacementCalcul.nombreTaux(date, date.plusDays(1), LocalTime.of(22,0), LocalTime.of(4,0))
                .intValueExact() == 1, "Une nuit : seul le créneau du lendemain compte");
        check(DeplacementCalcul.nombreTaux(date, date.plusDays(1), LocalTime.of(21,0), LocalTime.MIDNIGHT)
                .intValueExact() == 0, "Retour à minuit : pas de créneau le dernier jour");
        check(DeplacementCalcul.nombreTaux(date, date.plusDays(1), LocalTime.of(5,0), LocalTime.of(11,0))
                .intValueExact() == 3, "Bornes : 2 taux au départ et 1 au retour");
        check(DeplacementCalcul.nombreTaux(date, date, LocalTime.of(15,0), LocalTime.of(4,0)) == null,
                "Retour avant le départ le même jour");
        check(DeplacementCalcul.nombreTaux(date, date.minusDays(1), LocalTime.NOON, LocalTime.NOON) == null, "Dates inversées");
        check(DeplacementCalcul.nombreTaux(date, date, null, LocalTime.NOON) == null, "Heure manquante");

        Paiement paiement = new Paiement();
        paiement.setTypePaiement(TypePaiement.DEPLACEMENT);
        paiement.setExercice("2026");
        Professeur prof = new Professeur();
        prof.setNom("EXEMPLE"); prof.setPrenom("Professeur");
        prof.setCin("TEST123"); prof.setPpr("123456");
        prof.setGrade("Professeur d'Enseignement Superieur"); prof.setEchelle(12);
        prof.setAffectation("Oujda"); paiement.setProfesseur(prof);
        for (int i = 1; i <= 5; i++) {
            LigneDeplacement ligne = new LigneDeplacement();
            ligne.setDateDepart(date); ligne.setDateArrivee(date.plusDays(8));
            ligne.setParcours("Oujda - Destination " + i + " - Oujda");
            ligne.setHeureDepart(LocalTime.of(4,0)); ligne.setHeureRetour(LocalTime.of(22,0));
            ligne.setNombreTauxBase(BigDecimal.valueOf(27)); ligne.setTauxBaseApplique(BigDecimal.valueOf(100));
            ligne.setMontant(BigDecimal.valueOf(2700)); paiement.getLignesDeplacement().add(ligne);
        }
        paiement.getLignesDeplacement().add(new LigneDeplacement());
        DeplacementPdfGenerationService service = new DeplacementPdfGenerationService();
        var repo = Proxy.newProxyInstance(PaiementRepository.class.getClassLoader(), new Class[]{PaiementRepository.class},
                (proxy, method, arguments) -> Optional.of(paiement));
        var field = DeplacementPdfGenerationService.class.getDeclaredField("paiementRepository");
        field.setAccessible(true); field.set(service, repo);
        field = DeplacementPdfGenerationService.class.getDeclaredField("pdfGenerationService");
        field.setAccessible(true); field.set(service, new PdfGenerationService());
        byte[] bytes = service.genererPdfDeplacement(1L).readAllBytes();
        Path output = Path.of("tmp/pdfs/deplacement-regression.pdf");
        Files.createDirectories(output.getParent()); Files.write(output, bytes);
        try (PdfDocument pdf = new PdfDocument(new PdfReader(new java.io.ByteArrayInputStream(bytes)))) {
            check(pdf.getNumberOfPages() == 6, "1 état et 5 ordres : " + pdf.getNumberOfPages());
            String etat = PdfTextExtractor.getTextFromPage(pdf.getPage(1));
            check(etat.contains("20/09/2026") && etat.contains("28/09/2026"), "Dates réelles conservées dans l'état");
            for (int page = 2; page <= 6; page++) {
                String text = PdfTextExtractor.getTextFromPage(pdf.getPage(page));
                check(text.contains("Ordre de mission") && text.contains("Le Directeur") && text.contains("Raison de service"), "Contenu ordre");
                check(!text.contains("EXERCICE") && !text.contains("Total Général"), "Pas de budget ni total dans l'ordre");
                check(text.contains("19/09/2026") && text.contains("28/09/2026") && !text.contains("20/09/2026"), "Seul le départ de l'ordre est avancé d'un jour");
                for (int i = 1; i <= 5; i++)
                    check(text.contains("Destination " + i) == (i == page - 1), "Un seul trajet par ordre");
            }
        }
        var format = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (LocalDate depart : new LocalDate[]{LocalDate.of(2026,10,18), LocalDate.of(2026,11,1), LocalDate.of(2027,1,1), LocalDate.of(2028,3,1)}) {
            LigneDeplacement trajet = paiement.getLignesDeplacement().get(0);
            paiement.getLignesDeplacement().clear(); paiement.getLignesDeplacement().add(trajet);
            LocalDate retour = depart.plusDays(3);
            trajet.setDateDepart(depart); trajet.setDateArrivee(retour);
            try (PdfDocument pdf = new PdfDocument(new PdfReader(service.genererPdfDeplacement(1L)))) {
                check(pdf.getNumberOfPages() == 2, "État et ordre pour un seul trajet");
                String etat = PdfTextExtractor.getTextFromPage(pdf.getPage(1));
                String ordre = PdfTextExtractor.getTextFromPage(pdf.getPage(2));
                check(etat.contains(depart.format(format)) && etat.contains(retour.format(format)), "Dates de déplacement inchangées");
                check(ordre.contains(depart.minusDays(1).format(format)) && ordre.contains(retour.format(format)), "Dates de l'ordre : " + depart);
                check(trajet.getDateDepart().equals(depart) && trajet.getDateArrivee().equals(retour), "Données du trajet non modifiées");
            }
        }
        System.out.println("OK : barèmes, créneaux, 27 et 8 taux, jours partiels et PDF de 6 pages.");
    }
}
