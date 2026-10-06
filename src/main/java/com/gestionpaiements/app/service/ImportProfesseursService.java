package com.gestionpaiements.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestionpaiements.app.dao.*;
import com.gestionpaiements.app.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import java.io.*;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ImportProfesseursService {
    public static final List<String> COLONNES = List.of("CIN", "PPR", "Nom", "Prénom", "Grade", "Échelle",
            "Banque", "Ville", "Numéro de compte", "Clé");
    private final ImportProfesseursRepository imports;
    private final LigneImportProfesseurRepository lignes;
    private final ProfesseurRepository professeurs;
    private final TransactionTemplate transaction;
    private final ObjectMapper json = new ObjectMapper();

    public ImportProfesseursService(ImportProfesseursRepository imports, LigneImportProfesseurRepository lignes,
                                   ProfesseurRepository professeurs, PlatformTransactionManager manager) {
        this.imports = imports; this.lignes = lignes; this.professeurs = professeurs;
        transaction = new TransactionTemplate(manager);
    }
    public List<ImportProfesseurs> historique() { return imports.findAllByOrderByDateImportDescIdDesc(); }
    public Optional<ImportProfesseurs> dernier() { return imports.findFirstByOrderByDateImportDescIdDesc(); }
    public List<LigneImportProfesseur> lignes(Long id) { return lignes.findByImportIdOrderByNumeroLigneAsc(id); }
    public String[] valeurs(LigneImportProfesseur ligne) { return lire(ligne.getDonnees()); }

    /** Les erreurs de champ commencent par leur en-tête ; les erreurs générales restent dans le motif. */
    public static boolean colonneEnErreur(LigneImportProfesseur ligne, int index) {
        if (ligne.getErreur() == null || index < 0 || index >= COLONNES.size()) return false;
        String colonne = COLONNES.get(index);
        return Arrays.stream(ligne.getErreur().split(" ; "))
                .map(String::strip)
                .anyMatch(message -> message.startsWith(colonne + " ") || message.startsWith(colonne + ":"));
    }
    private String[] lire(String value) {
        try { return json.readValue(value, String[].class); }
        catch (Exception e) { throw new IllegalStateException("Données d'import illisibles.", e); }
    }
    private String serialiser(String[] values) {
        try { return json.writeValueAsString(values); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }

    public ImportProfesseurs importer(File fichier) {
        ImportProfesseurs lot = new ImportProfesseurs();
        lot.setNomFichier(fichier.getName()); lot.setDateImport(LocalDateTime.now()); lot.setEtat("En cours");
        lot = imports.saveAndFlush(lot);
        try (InputStream stream = new FileInputStream(fichier); XSSFWorkbook workbook = new XSSFWorkbook(stream)) {
            if (workbook.getNumberOfSheets() == 0) throw new IllegalArgumentException("Le fichier ne contient aucune feuille.");
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.FRANCE);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            int header = -1;
            int[] mapping = null;
            for (Row row : sheet) {
                int[] found = new int[10]; Arrays.fill(found, -1);
                for (Cell cell : row) {
                    int index = colonne(formatter.formatCellValue(cell));
                    if (index >= 0) found[index] = cell.getColumnIndex();
                }
                if (found[0] >= 0 && found[1] >= 0) { header = row.getRowNum(); mapping = found; break; }
            }
            if (mapping == null) throw new IllegalArgumentException("En-têtes CIN et PPR introuvables dans la première feuille.");
            List<String> manquantes = new ArrayList<>();
            for (int i = 0; i < 10; i++) if (mapping[i] < 0) manquantes.add(COLONNES.get(i));
            if (!manquantes.isEmpty()) throw new IllegalArgumentException("Colonnes manquantes : " + String.join(", ", manquantes));
            int count = 0;
            for (int n = header + 1; n <= sheet.getLastRowNum(); n++) {
                Row row = sheet.getRow(n);
                if (row == null) continue;
                String[] values = new String[10];
                List<String> excelErrors = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    Cell cell = row.getCell(mapping[i]);
                    try { values[i] = formatter.formatCellValue(cell, evaluator).trim(); }
                    catch (RuntimeException ex) { values[i] = formatter.formatCellValue(cell); excelErrors.add(COLONNES.get(i) + " : cellule Excel illisible"); }
                    if (cell != null && (cell.getCellType() == CellType.ERROR ||
                            (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.ERROR))) {
                        excelErrors.add(COLONNES.get(i) + " : erreur Excel");
                    }
                    if (i == 8 && cell != null && cell.getCellType() == CellType.NUMERIC && values[i].length() > 15) {
                        excelErrors.add("Numéro de compte numérique : vérifier les chiffres et saisir le compte au format texte");
                    }
                }
                if (Arrays.stream(values).allMatch(String::isBlank)) continue;
                LigneImportProfesseur ligne = new LigneImportProfesseur();
                ligne.setImportId(lot.getId()); ligne.setNumeroLigne(n + 1);
                ligne.setDonnees(serialiser(values)); ligne.setDonneesOriginales(ligne.getDonnees());
                ligne.setErreur("Ligne non traitée : reprendre avec Modifier / corriger.");
                ligne = lignes.saveAndFlush(ligne);
                enregistrer(ligne.getId(), values, false, excelErrors);
                count++;
            }
            lot.setEtat(lignes(lot.getId()).stream().anyMatch(l -> "Erreur".equals(l.getStatut())) ? "Terminé avec erreurs" : "Terminé");
            if (count == 0) lot.setErreur("Aucune ligne de données trouvée.");
        } catch (Exception e) {
            lot.setEtat("Échec");
            lot.setErreur(e instanceof IllegalArgumentException ? e.getMessage() :
                    "Import interrompu. Vérifiez que le fichier est un .xlsx valide et accessible. Les lignes déjà traitées restent consultables.");
        }
        return imports.saveAndFlush(lot);
    }

    public LigneImportProfesseur corriger(Long id, String[] values) {
        if (values == null || values.length != 10) throw new IllegalArgumentException("Les dix champs sont nécessaires.");
        return enregistrer(id, Arrays.stream(values).map(v -> v == null ? "" : v.trim()).toArray(String[]::new), true, List.of());
    }

    private LigneImportProfesseur enregistrer(Long id, String[] values, boolean correction, List<String> excelErrors) {
        try {
            return transaction.execute(status -> {
                LigneImportProfesseur ligne = lignes.findById(id).orElseThrow();
                List<String> errors = new ArrayList<>(excelErrors); errors.addAll(valider(values));
                professeurs.findByCinIgnoreCase(values[0]).filter(p -> !Objects.equals(p.getIdProfesseur(), ligne.getProfesseurId()))
                        .ifPresent(p -> errors.add("CIN déjà utilisé par un professeur existant"));
                professeurs.findByPpr(values[1]).filter(p -> !Objects.equals(p.getIdProfesseur(), ligne.getProfesseurId()))
                        .ifPresent(p -> errors.add("PPR déjà utilisé par un professeur existant"));
                Professeur prof = ligne.getProfesseurId() == null ? new Professeur() :
                        professeurs.findById(ligne.getProfesseurId()).orElseThrow(() -> new IllegalArgumentException("Ce professeur a été supprimé."));
                if (ligne.getProfesseurId() != null && !Arrays.equals(champs(prof), valeurs(ligne)))
                    errors.add("La fiche a changé depuis cet import. Modifiez la fiche actuelle pour ne pas écraser ses changements.");
                if (!errors.isEmpty()) {
                    if (correction) throw new IllegalArgumentException(String.join("\n", errors));
                    ligne.setErreur(String.join(" ; ", errors));
                    return lignes.saveAndFlush(ligne);
                }
                prof.setCin(values[0]); prof.setPpr(values[1]); prof.setNom(values[2]); prof.setPrenom(values[3]);
                prof.setGrade(values[4]); prof.setEchelle(values[5].isBlank() ? null : Integer.valueOf(values[5]));
                prof.setRibBanque(values[6]); prof.setRibVille(values[7]); prof.setRibNumeroCompte(values[8]); prof.setRibCle(values[9]);
                prof = professeurs.saveAndFlush(prof);
                ligne.setProfesseurId(prof.getIdProfesseur()); ligne.setDonnees(serialiser(champs(prof))); ligne.setErreur(null);
                if (correction) ligne.setDateCorrection(LocalDateTime.now());
                LigneImportProfesseur saved = lignes.saveAndFlush(ligne);
                if (correction) {
                    ImportProfesseurs lot = imports.findById(ligne.getImportId()).orElseThrow();
                    if (lot.getEtat().startsWith("Terminé")) {
                        lot.setEtat(lignes(lot.getId()).stream().anyMatch(l -> "Erreur".equals(l.getStatut())) ? "Terminé avec erreurs" : "Terminé");
                        imports.save(lot);
                    }
                }
                return saved;
            });
        } catch (DataIntegrityViolationException e) {
            if (correction) throw new IllegalArgumentException("Enregistrement refusé : CIN ou PPR déjà utilisé, ou valeur incompatible.");
            return transaction.execute(status -> {
                LigneImportProfesseur ligne = lignes.findById(id).orElseThrow();
                ligne.setErreur("Enregistrement refusé : vérifiez les doublons de CIN/PPR et les valeurs.");
                return lignes.saveAndFlush(ligne);
            });
        }
    }

    static List<String> valider(String[] v) {
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (i < 4 && v[i].isBlank()) errors.add(COLONNES.get(i) + " obligatoire");
            if (v[i].length() > 255) errors.add(COLONNES.get(i) + " trop long (255 caractères maximum)");
        }
        if (!v[5].isBlank()) {
            try { if (Integer.parseInt(v[5]) <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException e) { errors.add("Échelle : nombre entier positif attendu"); }
        }
        boolean ribPresent = Arrays.stream(v, 6, 10).anyMatch(s -> !s.isBlank());
        int[] tailles = {3, 3, 16, 2};
        if (ribPresent) for (int i = 6; i < 10; i++) {
            if (!v[i].matches("[0-9]{" + tailles[i - 6] + "}")) errors.add(COLONNES.get(i) + " : " + tailles[i - 6] + " chiffres attendus");
        }
        return errors;
    }

    private String[] champs(Professeur p) {
        return new String[]{s(p.getCin()), s(p.getPpr()), s(p.getNom()), s(p.getPrenom()), s(p.getGrade()),
                p.getEchelle() == null ? "" : p.getEchelle().toString(), s(p.getRibBanque()), s(p.getRibVille()), s(p.getRibNumeroCompte()), s(p.getRibCle())};
    }
    private String s(String value) { return value == null ? "" : value; }
    private int colonne(String texte) {
        String n = Normalizer.normalize(texte, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return switch (n) {
            case "cin" -> 0;
            case "ppr", "ddr" -> 1;
            case "nom" -> 2;
            case "prenom" -> 3;
            case "grade" -> 4;
            case "echelle" -> 5;
            case "banque", "ribbanque", "banquerib", "codebanque" -> 6;
            case "ville", "ribville", "villerib", "codeville" -> 7;
            case "numerodecompte", "numerocompte", "numerodecompterib", "numerocompterib", "ribnumerocompte", "compte", "ncompte", "compten" -> 8;
            case "cle", "clerib", "ribcle" -> 9;
            default -> -1;
        };
    }
}
