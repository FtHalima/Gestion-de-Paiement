package com.gestionpaiements.app.controller;

import com.gestionpaiements.app.model.Paiement;
import com.gestionpaiements.app.model.TypePaiement;
import com.gestionpaiements.app.service.PaiementService;
import com.gestionpaiements.app.service.ProfesseurService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

import java.io.File;

import javafx.scene.control.TableCell;

@Component
public class DashboardController {

    @Autowired
    private PaiementService paiementService;

        
    @Autowired
    private ProfesseurService professeurService;

    @FXML private Button importButton;

    @FXML private Label countVacataire;
    @FXML private Label countHeureSup;
    @FXML private Label countDeplacement;
    @FXML private Label countTotal;

    @FXML private TableView<Paiement> recentsTable;
    @FXML private TableColumn<Paiement, String> colCin;
    @FXML private TableColumn<Paiement, String> colNom;
    @FXML private TableColumn<Paiement, String> colType;
    @FXML private TableColumn<Paiement, String> colMontant;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        // ⚠️ Méthodes supposées existantes sur PaiementService — à vérifier/adapter
        long nbVacataire = paiementService.compterParType(TypePaiement.VACATAIRE);
        long nbHeureSup = paiementService.compterParType(TypePaiement.HEURE_SUP);
        long nbDeplacement = paiementService.compterParType(TypePaiement.DEPLACEMENT);

        countVacataire.setText(String.valueOf(nbVacataire));
        countHeureSup.setText(String.valueOf(nbHeureSup));
        countDeplacement.setText(String.valueOf(nbDeplacement));
        countTotal.setText(String.valueOf(nbVacataire + nbHeureSup + nbDeplacement));

        colCin.setCellValueFactory(data -> {
            Paiement p = data.getValue();
            String cin = (p.getProfesseur() != null && p.getProfesseur().getCin() != null)
                    ? p.getProfesseur().getCin() : "-";
            return new javafx.beans.property.SimpleStringProperty(cin);
        });
        colNom.setCellValueFactory(data -> {
            Paiement p = data.getValue();
            String nom = p.getProfesseur() != null
                    ? p.getProfesseur().getNom() + " " + p.getProfesseur().getPrenom()
                    : "-";
            return new javafx.beans.property.SimpleStringProperty(nom);
        });
        //
        colType.setCellValueFactory(data -> {
            TypePaiement type = data.getValue().getTypePaiement();
            return new javafx.beans.property.SimpleStringProperty(type != null ? type.name() : "-");
        });
        colType.setCellFactory(column -> new TableCell<Paiement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.equals("-")) {
                    setText(null);
                    setStyle("");
                } else {
                    String display, bg, fg;
                    switch (item) {
                        case "VACATAIRE": display = "Vacataire"; bg = "#e8f8ee"; fg = "#2e9e5b"; break;
                        case "HEURE_SUP": display = "Heure supplémentaire"; bg = "#e8f1fb"; fg = "#3576d6"; break;
                        case "DEPLACEMENT": display = "Déplacement"; bg = "#fdf1e4"; fg = "#d6822f"; break;
                        default: display = item; bg = "#f1eafc"; fg = "#8455d1";
                    }
                    setText(display);
                    setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg
                            + "; -fx-background-radius: 6; -fx-padding: 2 2; -fx-font-weight: bold; -fx-font-size: 11px; -fx-alignment: CENTER;");
                }
            }
        });
        //
        colMontant.setCellValueFactory(data -> {
            Paiement p = data.getValue();
            String montant = p.getMontantNet() != null
                    ? p.getMontantNet().stripTrailingZeros().toPlainString().replace(".", ",") + " DH"
                    : "-";
            return new javafx.beans.property.SimpleStringProperty(montant);
        });

        // ⚠️ Méthode supposée existante — à vérifier/adapter
        List<Paiement> recents = paiementService.trouverDerniers(4);
        recentsTable.setItems(FXCollections.observableArrayList(recents));
        recentsTable.setFixedCellSize(32);
        recentsTable.prefHeightProperty().bind(
                javafx.beans.binding.Bindings.size(recentsTable.getItems())
                        .multiply(recentsTable.getFixedCellSize()).add(34));
        recentsTable.setMaxHeight(javafx.scene.control.Control.USE_PREF_SIZE);
    }



    @FXML
    private void handleImportExcel() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Importer un fichier Excel de professeurs");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));
        File file = fileChooser.showOpenDialog(importButton.getScene().getWindow());
        if (file == null) return;

        int imported = 0;
        int skipped = 0;

        try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            int headerRowIndex = -1;
            for (int r = 0; r <= lastRow; r++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(r);
                if (row == null) continue;
                if (getCellString(row, 0).trim().equalsIgnoreCase("CIN")) {
                    headerRowIndex = r;
                    break;
                }
            }
            if (headerRowIndex == -1) {
                showAlert("Erreur", "En-tête introuvable : la colonne 'CIN' n'a pas été trouvée dans le fichier.");
                return;
            }

            for (int r = headerRowIndex + 1; r <= lastRow; r++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(r);
                if (row == null) continue;
                String cin = getCellString(row, 0);
                if (cin.isEmpty()) continue;

                try {
                    com.gestionpaiements.app.model.Professeur prof = new com.gestionpaiements.app.model.Professeur();
                    prof.setCin(cin);
                    prof.setPpr(getCellString(row, 1));
                    prof.setNom(getCellString(row, 2));
                    prof.setPrenom(getCellString(row, 3));
                    prof.setGrade(getCellString(row, 4));
                    String echelleStr = getCellString(row, 5);
                    if (!echelleStr.isEmpty()) {
                        try { prof.setEchelle(Integer.parseInt(echelleStr.trim())); } catch (Exception ignored) {}
                    }
                    prof.setRibBanque(getCellString(row, 6));
                    prof.setRibVille(getCellString(row, 7));
                    prof.setRibNumeroCompte(getCellString(row, 8));
                    prof.setRibCle(getCellString(row, 9));

                    professeurService.creerOuRecuperer(prof);
                    imported++;
                } catch (Exception rowEx) {
                    rowEx.printStackTrace();
                    skipped++;
                }
            }

            showAlert("Import terminé", imported + " professeur(s) importé(s), " + skipped + " ligne(s) ignorée(s).");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'importer le fichier : " + e.getMessage());
        }
    }

    private String getCellString(org.apache.poi.ss.usermodel.Row row, int colIndex) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(colIndex);
        if (cell == null) return "";
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
            double val = cell.getNumericCellValue();
            if (val == Math.floor(val)) return String.valueOf((long) val);
            return String.valueOf(val);
        }
        return cell.getStringCellValue() != null ? cell.getStringCellValue().trim() : "";
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}