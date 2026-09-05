package com.gestionpaiements.app.service;

import com.gestionpaiements.app.model.LigneDeplacement;
import com.gestionpaiements.app.model.Paiement;
import com.gestionpaiements.app.model.Professeur;
import com.gestionpaiements.app.model.TypePaiement;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gestionpaiements.app.model.LigneDeplacement;

@Service
public class ExcelFileManagerService {

    private static final String BASE_DIR = System.getProperty("user.home") + "/GestionPaiements/excel";
    private static final String ARCHIVE_DIR = BASE_DIR + "/archive";

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
    private static final int COL_ID_INTERNAL = 20; // colonne U (0-indexée), usage interne uniquement

    private String getTypePrefix(TypePaiement type) {
        switch (type) {
            case VACATAIRE: return "Vacation";
            case HEURE_SUP: return "HeureSup";
            case DEPLACEMENT: return "Deplacement";
            default: throw new IllegalArgumentException("Type de paiement inconnu : " + type);
        }
    }

    private void ensureDirectoriesExist() throws IOException {
        Files.createDirectories(Paths.get(BASE_DIR));
        Files.createDirectories(Paths.get(ARCHIVE_DIR));
    }

    /**
     * Cherche le fichier actif existant pour ce type (dans excel/, pas dans archive/).
     * Retourne null s'il n'y en a pas.
     */
    private File findActiveFile(TypePaiement type) throws IOException {
        ensureDirectoriesExist();
        String prefix = getTypePrefix(type);
        File dir = new File(BASE_DIR);
        File[] files = dir.listFiles((d, name) -> name.startsWith(prefix + "_") && name.endsWith(".xlsx"));
        if (files != null && files.length > 0) {
            return files[0];
        }
        return null;
    }
//
    private int findExistingRowIndex(Sheet sheet, Long paiementId) {
        if (paiementId == null) return -1;
        int lastRow = sheet.getLastRowNum();
        for (int r = 4; r <= lastRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            Cell idCell = row.getCell(COL_ID_INTERNAL);
            if (idCell != null && idCell.getCellType() == CellType.NUMERIC) {
                long existingId = (long) idCell.getNumericCellValue();
                if (existingId == paiementId) {
                    return r;
                }
            }
        }
        return -1;
    }
//


    /**
     * Calcule le prochain numéro de séquence pour ce type, en regardant
     * à la fois le fichier actif éventuel et les fichiers déjà archivés.
     */
    private int computeNextSequence(TypePaiement type) throws IOException {
        ensureDirectoriesExist();
        String prefix = getTypePrefix(type);
        Pattern pattern = Pattern.compile("^" + Pattern.quote(prefix) + "_S(\\d+)\\.xlsx$");
        int max = 0;

        for (File dir : new File[]{new File(BASE_DIR), new File(ARCHIVE_DIR)}) {
            File[] files = dir.listFiles((d, name) -> pattern.matcher(name).matches());
            if (files != null) {
                for (File f : files) {
                    Matcher m = pattern.matcher(f.getName());
                    if (m.matches()) {
                        int n = Integer.parseInt(m.group(1));
                        if (n > max) max = n;
                    }
                }
            }
        }
        return max + 1;
    }

    /**
     * Garantit qu'un fichier actif existe pour ce type ; le crée si besoin.
     * Retourne le fichier actif.
     */
    public File ensureActiveFileExists(TypePaiement type) throws IOException {
        File existing = findActiveFile(type);
        if (existing != null) {
            return existing;
        }
        return createNewActiveFile(type);
    }

    private File createNewActiveFile(TypePaiement type) throws IOException {
        ensureDirectoriesExist();
        int seq = computeNextSequence(type);
        String fileName = getTypePrefix(type) + "_S" + seq + ".xlsx";
        File file = new File(BASE_DIR, fileName);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Paiements");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row row1 = sheet.createRow(0);
            for (int i = 0; i < LIGNE1_HEADERS.length; i++) {
                Cell cell = row1.createCell(i);
                cell.setCellValue(LIGNE1_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            Row row4 = sheet.createRow(3);
            for (int i = 0; i < LIGNE4_HEADERS.length; i++) {
                Cell cell = row4.createCell(i);
                cell.setCellValue(LIGNE4_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < LIGNE4_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.setColumnHidden(COL_ID_INTERNAL, true);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
        return file;
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
     * Ajoute une ligne dans le fichier actif correspondant au type du paiement.
     */
    public void appendPaiement(Paiement paiement) throws IOException {
        TypePaiement type = paiement.getTypePaiement();
        File file = ensureActiveFileExists(type);

        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int existingRowIndex = findExistingRowIndex(sheet, paiement.getIdPaiement());
            Row row = (existingRowIndex >= 0)
                    ? sheet.getRow(existingRowIndex)
                    : sheet.createRow(Math.max(4, sheet.getLastRowNum() + 1));

            Professeur prof = paiement.getProfesseur();

            CellStyle dataStyle = createDataStyle(workbook, false);
            CellStyle textStyle = createDataStyle(workbook, true);

            int col = 0;

            Cell c1 = row.createCell(col++);
            c1.setCellValue(prof != null && prof.getEchelle() != null ? prof.getEchelle().toString() : "");
            c1.setCellStyle(dataStyle);

            String dateDebutExcel = "";
            String dateFinExcel = "";
            double nombreExcel = 0;
            double tauxExcel = 0;

            if (paiement.getTypePaiement() == TypePaiement.DEPLACEMENT
                    && paiement.getLignesDeplacement() != null
                    && !paiement.getLignesDeplacement().isEmpty()) {

                LigneDeplacement premierTrajet = paiement.getLignesDeplacement().get(0);
                LigneDeplacement dernierTrajet = paiement.getLignesDeplacement()
                        .get(paiement.getLignesDeplacement().size() - 1);

                dateDebutExcel = premierTrajet.getDateDepart() != null
                        ? premierTrajet.getDateDepart().format(DATE_FORMAT) : "";
                dateFinExcel = dernierTrajet.getDateArrivee() != null
                        ? dernierTrajet.getDateArrivee().format(DATE_FORMAT) : "";

                BigDecimal sommeNombreTaux = BigDecimal.ZERO;
                for (LigneDeplacement l : paiement.getLignesDeplacement()) {
                    if (l.getNombreTauxBase() != null) {
                        sommeNombreTaux = sommeNombreTaux.add(l.getNombreTauxBase());
                    }
                }
                nombreExcel = sommeNombreTaux.doubleValue();
                tauxExcel = dernierTrajet.getTauxBaseApplique() != null ? dernierTrajet.getTauxBaseApplique().doubleValue() : 0;

            } else {
                dateDebutExcel = paiement.getDateDebut() != null ? paiement.getDateDebut().format(DATE_FORMAT) : "";
                dateFinExcel = paiement.getDateFin() != null ? paiement.getDateFin().format(DATE_FORMAT) : "";
                nombreExcel = paiement.getNombreHeures() != null ? paiement.getNombreHeures().doubleValue() : 0;
                tauxExcel = paiement.getTaux() != null ? paiement.getTaux().doubleValue() : 0;
            }

            Cell c2 = row.createCell(col++);
            c2.setCellValue(dateDebutExcel);
            c2.setCellStyle(dataStyle);

            Cell c3 = row.createCell(col++);
            c3.setCellValue(dateFinExcel);
            c3.setCellStyle(dataStyle);

            Cell c4 = row.createCell(col++);
            c4.setCellValue(nombreExcel);
            c4.setCellStyle(dataStyle);

            Cell c5 = row.createCell(col++);
            c5.setCellValue(tauxExcel);
            c5.setCellStyle(dataStyle);



            Cell c6 = row.createCell(col++);
            c6.setCellValue(paiement.getMontantBrut() != null ? paiement.getMontantBrut().doubleValue() : 0);
            c6.setCellStyle(dataStyle);

            Cell c7 = row.createCell(col++);
            c7.setCellValue(paiement.getRetenueIr() != null ? paiement.getRetenueIr().doubleValue() : 0);
            c7.setCellStyle(dataStyle);

            Cell c8 = row.createCell(col++);
            c8.setCellValue(paiement.getMontantNet() != null ? paiement.getMontantNet().doubleValue() : 0);
            c8.setCellStyle(dataStyle);

            Cell c9 = row.createCell(col++);
            c9.setCellValue(prof != null && prof.getCin() != null ? prof.getCin() : "");
            c9.setCellStyle(textStyle);

            Cell c10 = row.createCell(col++);
            c10.setCellValue(prof != null && prof.getNom() != null ? prof.getNom() : "");
            c10.setCellStyle(dataStyle);

            Cell c11 = row.createCell(col++);
            c11.setCellValue(prof != null && prof.getPrenom() != null ? prof.getPrenom() : "");
            c11.setCellStyle(dataStyle);

            Cell c12 = row.createCell(col++);
            c12.setCellValue(prof != null && prof.getPpr() != null ? prof.getPpr() : "");
            c12.setCellStyle(dataStyle);

            // Mode de paiement : VIREMENT -> VIR pour Excel uniquement
            Cell c13 = row.createCell(col++);
            String modeExcel = "VIREMENT".equalsIgnoreCase(paiement.getModePaiement()) ? "VIR" : paiement.getModePaiement();
            c13.setCellValue(modeExcel != null ? modeExcel : "");
            c13.setCellStyle(dataStyle);

            Cell c14 = row.createCell(col++);
            c14.setCellValue(paiement.getTypeReferenceReglement() != null ? paiement.getTypeReferenceReglement() : "");
            c14.setCellStyle(dataStyle);

            Cell c15 = row.createCell(col++);
            c15.setCellValue(prof != null && prof.getRibBanque() != null ? prof.getRibBanque() : "");
            c15.setCellStyle(textStyle);

            Cell c16 = row.createCell(col++);
            c16.setCellValue(prof != null && prof.getRibVille() != null ? prof.getRibVille() : "");
            c16.setCellStyle(textStyle);

            Cell c17 = row.createCell(col++);
            c17.setCellValue(prof != null && prof.getRibNumeroCompte() != null ? prof.getRibNumeroCompte() : "");
            c17.setCellStyle(textStyle);

            Cell c18 = row.createCell(col++);
            c18.setCellValue(prof != null && prof.getRibCle() != null ? prof.getRibCle() : "");
            c18.setCellStyle(textStyle);

            Cell c19 = row.createCell(col++);
            c19.setCellValue(paiement.getObjetReglement() != null ? paiement.getObjetReglement() : "");
            c19.setCellStyle(dataStyle);

            // Reference : toujours vide dans Excel
            Cell c20 = row.createCell(col);
            c20.setCellValue("");
            c20.setCellStyle(dataStyle);

            Cell idCell = row.createCell(COL_ID_INTERNAL);
            idCell.setCellValue(paiement.getIdPaiement());

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * Exporte une copie du fichier actif du type donné.
     */
    public void exportActiveFile(TypePaiement type, File destination) throws IOException {
        File active = ensureActiveFileExists(type);
        Files.copy(active.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Archive le fichier actif du type donné et en crée un nouveau (séquence incrémentée).
     */
    public void closeActiveFile(TypePaiement type) throws IOException {
        File active = ensureActiveFileExists(type);
        Path target = Paths.get(ARCHIVE_DIR, active.getName());
        Files.move(active.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        createNewActiveFile(type);
    }

    /**
     * Réactive un fichier archivé : archive le fichier actif actuel du même type
     * (s'il existe), puis déplace le fichier archivé choisi vers le dossier actif.
     *
     * @param archivedFile le fichier archivé à réactiver (doit être dans excel/archive/)
     * @param type le type de paiement concerné (déduit du préfixe du nom de fichier)
     */
    public void reactiverFichierArchive(File archivedFile, TypePaiement type) throws IOException {
        ensureDirectoriesExist();

        // Archive le fichier actif actuel s'il existe
        File currentActive = findActiveFile(type);
        if (currentActive != null) {
            String timestamp = java.time.LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String archivedName = currentActive.getName().replace(".xlsx", "") + "_archive_" + timestamp + ".xlsx";
            Path target = Paths.get(ARCHIVE_DIR, archivedName);
            Files.move(currentActive.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        }

        // Déplace le fichier archivé choisi vers le dossier actif
        Path destination = Paths.get(BASE_DIR, archivedFile.getName());
        Files.move(archivedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Déduit le type de paiement à partir du nom d'un fichier (préfixe avant "_S").
     */
    public TypePaiement deduireTypeDepuisNomFichier(String fileName) {
        if (fileName.startsWith("Vacation_")) return TypePaiement.VACATAIRE;
        if (fileName.startsWith("HeureSup_")) return TypePaiement.HEURE_SUP;
        if (fileName.startsWith("Deplacement_")) return TypePaiement.DEPLACEMENT;
        return null;
    }

    /**
     * Liste tous les fichiers archivés (tous types confondus).
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
     * Liste les fichiers archivés d'un type donné uniquement.
     */
    public List<File> getArchiveFiles(TypePaiement type) throws IOException {
        ensureDirectoriesExist();
        String prefix = getTypePrefix(type);
        List<File> result = new ArrayList<>();
        File archiveDir = new File(ARCHIVE_DIR);
        File[] files = archiveDir.listFiles((dir, name) -> name.startsWith(prefix + "_S") && name.endsWith(".xlsx"));
        if (files != null) {
            for (File f : files) {
                result.add(f);
            }
        }
        return result;
    }

        /**
     * Exporte une copie d'un fichier archivé donné vers la destination choisie.
     */
    public void exportFile(File source, File destination) throws IOException {
        Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    //NOUVEAU 
    public List<File> listAllFilesForType(TypePaiement type) throws IOException {
    ensureDirectoriesExist();
    String prefix = getTypePrefix(type);
    List<File> result = new ArrayList<>();
    File[] activeFiles = new File(BASE_DIR).listFiles((d, name) -> name.startsWith(prefix + "_") && name.endsWith(".xlsx"));
    if (activeFiles != null) {
        for (File f : activeFiles) result.add(f);
    }
    File[] archivedFiles = new File(ARCHIVE_DIR).listFiles((d, name) -> name.startsWith(prefix + "_") && name.endsWith(".xlsx"));
    if (archivedFiles != null) {
        for (File f : archivedFiles) result.add(f);
    }
    return result;
    }

    public boolean isFileActive(File file) {
        return file.getParentFile() != null
                && file.getParentFile().getAbsolutePath().equals(new File(BASE_DIR).getAbsolutePath());
    }

    private String sanitizeFileNamePart(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public void closeActiveFileAs(TypePaiement type, String customSuffix) throws IOException {
        File active = ensureActiveFileExists(type);
        String prefix = getTypePrefix(type);
        String safeSuffix = sanitizeFileNamePart(customSuffix);
        if (safeSuffix.isEmpty()) {
            safeSuffix = "S" + computeNextSequence(type);
        }
        String archivedName = prefix + "_" + safeSuffix + ".xlsx";
        Path target = Paths.get(ARCHIVE_DIR, archivedName);
        Files.move(active.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        createNewActiveFile(type);
    }

    public void renameFile(File file, String newSuffix) throws IOException {
        TypePaiement type = deduireTypeDepuisNomFichier(file.getName());
        if (type == null) {
            throw new IOException("Type de fichier inconnu, renommage impossible.");
        }
        String prefix = getTypePrefix(type);
        String safeSuffix = sanitizeFileNamePart(newSuffix);
        if (safeSuffix.isEmpty()) {
            throw new IOException("Le nom ne peut pas être vide.");
        }
        File newFile = new File(file.getParentFile(), prefix + "_" + safeSuffix + ".xlsx");
        Files.move(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public void deleteFile(File file) throws IOException {
        Files.deleteIfExists(file.toPath());
    }

    public static class LigneApercu {
        public Long idPaiement;
        public String cin;
        public String nom;
        public String prenom;
        public String dateDebut;
        public String dateFin;
        public String montantNet;
    }

    public List<LigneApercu> lireApercuFichier(File file) throws IOException {
        List<LigneApercu> result = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            for (int r = 4; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String cin = getCellStringPublic(row, 8);
                if (cin.isEmpty()) continue;
                LigneApercu l = new LigneApercu();
                l.cin = cin;
                Cell idCell = row.getCell(COL_ID_INTERNAL);
                if (idCell != null && idCell.getCellType() == CellType.NUMERIC) {
                    l.idPaiement = (long) idCell.getNumericCellValue();
                }
                l.nom = getCellStringPublic(row, 9);
                l.prenom = getCellStringPublic(row, 10);
                l.dateDebut = getCellStringPublic(row, 1);
                l.dateFin = getCellStringPublic(row, 2);
                l.montantNet = getCellStringPublic(row, 7);
                result.add(l);
            }
        }
        return result;
    }
    public void supprimerLigneParPaiementId(File file, Long paiementId) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowIndex = findExistingRowIndex(sheet, paiementId);
            if (rowIndex >= 0) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    for (int c = 0; c <= COL_ID_INTERNAL; c++) {
                        Cell cell = row.getCell(c);
                        if (cell != null) {
                            row.removeCell(cell);
                        }
                    }
                }
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    private String getCellStringPublic(Row row, int colIndex) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(colIndex);
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC) {
            double val = cell.getNumericCellValue();
            if (val == Math.floor(val)) return String.valueOf((long) val);
            return String.valueOf(val);
        }
        return cell.getStringCellValue() != null ? cell.getStringCellValue().trim() : "";
    }
}