package com.gestionpaiements.app.service;

import com.gestionpaiements.app.model.Paiement;
import com.gestionpaiements.app.model.Professeur;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelFileManagerService {

    private static final String BASE_DIR = "C:/Users/PC/gest-paiement/gestion-paiements-v2/excel";
    private static final String ARCHIVE_DIR = BASE_DIR + "/archive";
    private static final String ACTIVE_FILE_NAME = "Paiements_actif.xlsx"; // ⚠️ à changer ici plus tard si besoin

    private static final String[] LIGNE1_HEADERS = {
            "Acteur Dépense", "Type Dépense", "Sous Type Dépense", "Loi Finance", "Objet Dépense",
            "Nom Signataire", "Prénom Signataire", "Date Signature", "(Montant)NET", "Mode Engagement", "Rubrique"
    };

    private static final String[] LIGNE4_HEADERS = {
            "Grade/Echelle", "Date début", "Date fin", "Nombre", "Taux", "Brut", "Retenues",
            "Montant net", "CIN", "Nom", "Prénom", "DDR", "Mode de paiement",
            "Type référence règlement", "Banque", "Ville", "Numéro compte RIB", "Clé",
            "objet règlement", "Reference"
    };

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String getActiveFilePath() {
        return BASE_DIR + "/" + ACTIVE_FILE_NAME;
    }

    /**
     * Crée le dossier excel/ et excel/archive/ s'ils n'existent pas.
     */
    private void ensureDirectoriesExist() throws IOException {
        Files.createDirectories(Paths.get(BASE_DIR));
        Files.createDirectories(Paths.get(ARCHIVE_DIR));
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();

    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);

    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);

    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    style.setBorderTop(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);

    return style;
}

private CellStyle createDataStyle(Workbook workbook, boolean asText) {
    CellStyle style = workbook.createCellStyle();

    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);

    style.setFillPattern(FillPatternType.NO_FILL);

    style.setBorderTop(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);

    if (asText) {
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("@"));
    }

    return style;
}

    /**
     * Crée le fichier actif avec la structure à 4 lignes d'en-tête s'il n'existe pas déjà.
     */
    public void ensureActiveFileExists() throws IOException {
        ensureDirectoriesExist();
        File file = new File(getActiveFilePath());
        if (file.exists()) {
            return;
        }
        createNewActiveFile();
    }

    private void createNewActiveFile() throws IOException {
    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Paiements");

        CellStyle headerStyle = createHeaderStyle(workbook);

        // Ligne 1 : en-tête fixe (jamais rempli)
        Row row1 = sheet.createRow(0);
        for (int i = 0; i < LIGNE1_HEADERS.length; i++) {
            Cell cell = row1.createCell(i);
            cell.setCellValue(LIGNE1_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        // Lignes 2 et 3 : vides

        // Ligne 4 : en-têtes des 20 colonnes métier
        Row row4 = sheet.createRow(3);
        for (int i = 0; i < LIGNE4_HEADERS.length; i++) {
            Cell cell = row4.createCell(i);
            cell.setCellValue(LIGNE4_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        for (int i = 0; i < LIGNE4_HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fos = new FileOutputStream(getActiveFilePath())) {
            workbook.write(fos);
        }
    }
}

    /**
     * Ajoute une ligne de paiement au fichier actif, à partir de la ligne 5.
     * Seuls les 20 champs définis sont écrits.
     */
    public void appendPaiement(Paiement paiement) throws IOException {
    ensureActiveFileExists();

    File file = new File(getActiveFilePath());
    try (FileInputStream fis = new FileInputStream(file);
         XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

        Sheet sheet = workbook.getSheetAt(0);

        int targetRowIndex = Math.max(4, sheet.getLastRowNum() + 1);
        Row row = sheet.createRow(targetRowIndex);

        Professeur prof = paiement.getProfesseur();

        CellStyle dataStyle = createDataStyle(workbook, false);
        CellStyle textStyle = createDataStyle(workbook, true);

        int col = 0;

        // 1. Grade/Echelle
        String gradeEchelle = "";
        if (prof != null) {
            String grade = prof.getGrade() != null ? prof.getGrade() : "";
            String echelle = prof.getEchelle() != null ? prof.getEchelle().toString() : "";
            gradeEchelle = grade + (echelle.isEmpty() ? "" : " / " + echelle);
        }
        Cell c1 = row.createCell(col++);
        c1.setCellValue(gradeEchelle);
        c1.setCellStyle(dataStyle);

        // 2. Date début
        Cell c2 = row.createCell(col++);
        c2.setCellValue(paiement.getDateDebut() != null ? paiement.getDateDebut().format(DATE_FORMAT) : "");
        c2.setCellStyle(dataStyle);

        // 3. Date fin
        Cell c3 = row.createCell(col++);
        c3.setCellValue(paiement.getDateFin() != null ? paiement.getDateFin().format(DATE_FORMAT) : "");
        c3.setCellStyle(dataStyle);

        // 4. Nombre
        Cell c4 = row.createCell(col++);
        c4.setCellValue(paiement.getNombreHeures() != null ? paiement.getNombreHeures().doubleValue() : 0);
        c4.setCellStyle(dataStyle);

        // 5. Taux
        Cell c5 = row.createCell(col++);
        c5.setCellValue(paiement.getTaux() != null ? paiement.getTaux().doubleValue() : 0);
        c5.setCellStyle(dataStyle);

        // 6. Brut
        Cell c6 = row.createCell(col++);
        c6.setCellValue(paiement.getMontantBrut() != null ? paiement.getMontantBrut().doubleValue() : 0);
        c6.setCellStyle(dataStyle);

        // 7. Retenues
        Cell c7 = row.createCell(col++);
        c7.setCellValue(paiement.getRetenueIr() != null ? paiement.getRetenueIr().doubleValue() : 0);
        c7.setCellStyle(dataStyle);

        // 8. Montant net
        Cell c8 = row.createCell(col++);
        c8.setCellValue(paiement.getMontantNet() != null ? paiement.getMontantNet().doubleValue() : 0);
        c8.setCellStyle(dataStyle);

        // 9. CIN (texte forcé, préserve zéros)
        Cell c9 = row.createCell(col++);
        c9.setCellValue(prof != null && prof.getCin() != null ? prof.getCin() : "");
        c9.setCellStyle(textStyle);

        // 10. Nom
        Cell c10 = row.createCell(col++);
        c10.setCellValue(prof != null && prof.getNom() != null ? prof.getNom() : "");
        c10.setCellStyle(dataStyle);

        // 11. Prénom
        Cell c11 = row.createCell(col++);
        c11.setCellValue(prof != null && prof.getPrenom() != null ? prof.getPrenom() : "");
        c11.setCellStyle(dataStyle);

        // 12. DDR
        Cell c12 = row.createCell(col++);
        c12.setCellValue(prof != null && prof.getDdr() != null ? prof.getDdr().format(DATE_FORMAT) : "");
        c12.setCellStyle(dataStyle);

        // 13. Mode de paiement
        Cell c13 = row.createCell(col++);
        c13.setCellValue(paiement.getModePaiement() != null ? paiement.getModePaiement() : "");
        c13.setCellStyle(dataStyle);

        // 14. Type référence règlement
        Cell c14 = row.createCell(col++);
        c14.setCellValue(paiement.getTypeReferenceReglement() != null ? paiement.getTypeReferenceReglement() : "");
        c14.setCellStyle(dataStyle);

        // 15. Banque (texte forcé)
        Cell c15 = row.createCell(col++);
        c15.setCellValue(prof != null && prof.getRibBanque() != null ? prof.getRibBanque() : "");
        c15.setCellStyle(textStyle);

        // 16. Ville (texte forcé)
        Cell c16 = row.createCell(col++);
        c16.setCellValue(prof != null && prof.getRibVille() != null ? prof.getRibVille() : "");
        c16.setCellStyle(textStyle);

        // 17. Numéro compte RIB (texte forcé)
        Cell c17 = row.createCell(col++);
        c17.setCellValue(prof != null && prof.getRibNumeroCompte() != null ? prof.getRibNumeroCompte() : "");
        c17.setCellStyle(textStyle);

        // 18. Clé (texte forcé)
        Cell c18 = row.createCell(col++);
        c18.setCellValue(prof != null && prof.getRibCle() != null ? prof.getRibCle() : "");
        c18.setCellStyle(textStyle);

        // 19. objet règlement
        Cell c19 = row.createCell(col++);
        c19.setCellValue(paiement.getObjetReglement() != null ? paiement.getObjetReglement() : "");
        c19.setCellStyle(dataStyle);

        // 20. Reference
        Cell c20 = row.createCell(col);
        c20.setCellValue(paiement.getReferenceReglement() != null ? paiement.getReferenceReglement() : "");
        c20.setCellStyle(dataStyle);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
    }
}

    /**
     * Exporte une copie du fichier actif vers la destination choisie par l'utilisateur.
     */
    public void exportActiveFile(File destination) throws IOException {
        ensureActiveFileExists();
        Files.copy(Paths.get(getActiveFilePath()), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Liste les fichiers présents dans le dossier archive.
     */
    public List<File> getArchiveFiles() throws IOException {
        ensureDirectoriesExist();
        List<File> result = new ArrayList<>();
        File archiveDir = new File(ARCHIVE_DIR);
        File[] files = archiveDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".xlsx"));
        if (files != null) {
            for (File f : files) {
                result.add(f);
            }
        }
        return result;
    }

    /**
     * Archive le fichier actif (déplacement + horodatage) et en crée un nouveau.
     * Gardée pour une utilisation future — pas encore reliée à l'UI.
     */
    public void closeActiveFile() throws IOException {
        ensureActiveFileExists();
        String timestamp = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String archivedName = "Paiements_" + timestamp + ".xlsx";
        Path source = Paths.get(getActiveFilePath());
        Path target = Paths.get(ARCHIVE_DIR, archivedName);
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        createNewActiveFile();
    }
}