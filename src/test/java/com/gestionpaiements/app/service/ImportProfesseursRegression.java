package com.gestionpaiements.app.service;

import com.gestionpaiements.app.dao.*;
import com.gestionpaiements.app.model.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.*;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import java.nio.file.*;
import java.util.*;

/** Integration regression, run as a Java main with the Maven test classpath. Uses an isolated H2 database. */
public class ImportProfesseursRegression {
    @Configuration @EnableAutoConfiguration
    @EntityScan("com.gestionpaiements.app.model")
    @EnableJpaRepositories("com.gestionpaiements.app.dao")
    @Import(ImportProfesseursService.class)
    public static class Config {}
    private static final String[] HEADERS = {"CIN", "PPR", "Nom", "Prénom", "Grade", "Échelle", "Banque", "Ville", "Numéro compte RIB", "Clé"};
    private static String[] data(String cin, String ppr) { return new String[]{cin,ppr,"EXEMPLE","Test","Professeur","11","001","002","0000000000000001","03"}; }
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static void refuse(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Correction invalide acceptée");
    }
    private static java.io.File fichier(Path dir, String nom, String[][] rows) throws Exception {
        Path path = dir.resolve(nom);
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet();
            var header = sheet.createRow(2);
            for (int i = 0; i < HEADERS.length; i++) header.createCell(i).setCellValue(HEADERS[i]);
            for (int r = 0; r < rows.length; r++) {
                var row = sheet.createRow(r + 3);
                for (int c = 0; c < 10; c++) row.createCell(c).setCellValue(rows[r][c]);
            }
            try(var out = Files.newOutputStream(path)) { workbook.write(out); }
        }
        return path.toFile();
    }
    private static org.springframework.context.ConfigurableApplicationContext ouvrir(String db) {
        return new SpringApplicationBuilder(Config.class).web(WebApplicationType.NONE).run(
                "--spring.datasource.url=jdbc:h2:mem:" + db + ";DB_CLOSE_DELAY=-1",
                "--spring.jpa.hibernate.ddl-auto=update", "--spring.jpa.show-sql=false",
                "--spring.sql.init.mode=never", "--logging.level.root=ERROR");
    }
    public static void main(String[] args) throws Exception {
        Path dir = Files.createDirectories(Path.of("target/import-regression"));
        String db = "imports_" + UUID.randomUUID().toString().replace("-", "");
        Long firstId;
        try (var context = ouvrir(db)) {
            var service = context.getBean(ImportProfesseursService.class);
            var repo = context.getBean(ProfesseurRepository.class);
            check(service.dernier().isEmpty(), "Historique initial non vide");
            String[] invalid = data("BB2", "0002"); invalid[5] = "onze";
            String[] absentCin = data("", "0003");
            var lot = service.importer(fichier(dir, "premier.xlsx", new String[][]{data("AA1", "0001"), invalid, data("AA1", "0001"), absentCin}));
            firstId = lot.getId();
            check(lot.getEtat().equals("Terminé avec erreurs"), "Bilan erroné");
            var rows = service.lignes(lot.getId());
            check(rows.size() == 4 && repo.count() == 1, "Lignes perdues ou doublons insérés");
            check(rows.get(0).getNumeroLigne() == 4, "Numéro Excel incorrect");
            check(service.valeurs(rows.get(0))[8].equals("0000000000000001"), "Zéros du RIB perdus");
            check(rows.get(1).getErreur().contains("Échelle") && rows.get(3).getErreur().contains("CIN obligatoire"), "Erreurs absentes");
            for (int i = 0; i < 10; i++) {
                check(ImportProfesseursService.colonneEnErreur(rows.get(1), i) == (i == 5), "Coloration incorrecte pour l’échelle");
                check(ImportProfesseursService.colonneEnErreur(rows.get(2), i) == (i == 0 || i == 1), "Coloration incorrecte pour les doublons");
                check(ImportProfesseursService.colonneEnErreur(rows.get(3), i) == (i == 0), "Coloration incorrecte pour le CIN vide");
            }
            refuse(() -> service.corriger(rows.get(1).getId(), invalid));
            check(repo.count() == 1, "Correction invalide enregistrée");
            var corrected = service.corriger(rows.get(1).getId(), data("BB2", "0002"));
            check(corrected.getStatut().equals("Corrigé") && repo.count() == 2, "Correction non enregistrée");
            check(!ImportProfesseursService.colonneEnErreur(corrected, 5), "Couleur d’erreur conservée après correction");
            check(corrected.getDonneesOriginales().contains("onze"), "Original écrasé");
            String[] change = data("AA1", "0001"); change[2] = "CORRIGE";
            service.corriger(rows.get(0).getId(), change);
            check(repo.findByCinIgnoreCase("AA1").orElseThrow().getNom().equals("CORRIGE"), "Fiche non modifiée");
            var prof = repo.findByCinIgnoreCase("AA1").orElseThrow(); prof.setNom("PLUS RECENT"); repo.saveAndFlush(prof);
            refuse(() -> service.corriger(rows.get(0).getId(), change));
            var second = service.importer(fichier(dir, "second.xlsx", new String[][]{data("CC3", "0003")}));
            check(service.dernier().orElseThrow().getId().equals(second.getId()), "Dernier import incorrect");
            check(service.lignes(firstId).size() == 4, "Ancien import modifié");
            check(service.historique().size() == 2, "Historique incomplet");
            String[] ribInvalide = data("DD4", "0004"); ribInvalide[9] = "x";
            check(ImportProfesseursService.valider(ribInvalide).stream().anyMatch(s -> s.contains("Clé")), "RIB invalide accepté");
            service.corriger(rows.get(2).getId(), data("DD4", "0004"));
            service.corriger(rows.get(3).getId(), data("EE5", "0005"));
            check(service.historique().stream().filter(l -> l.getId().equals(firstId)).findFirst().orElseThrow().getEtat().equals("Terminé"), "Bilan non actualisé après corrections");
            Files.writeString(dir.resolve("illisible.xlsx"), "invalid");
            var bad = service.importer(dir.resolve("illisible.xlsx").toFile());
            check(bad.getEtat().equals("Échec") && bad.getErreur() != null, "Fichier invalide non signalé");
        }
        try (var context = ouvrir(db)) {
            var service = context.getBean(ImportProfesseursService.class);
            check(service.historique().size() == 3 && service.lignes(firstId).size() == 4, "Historique perdu après réouverture");
        }
        System.out.println("PASS : imports, doublons, erreurs, corrections, protection des modifications et persistance.");
    }
}
