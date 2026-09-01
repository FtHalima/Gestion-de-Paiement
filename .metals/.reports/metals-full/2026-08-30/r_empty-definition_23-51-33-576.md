error id: file:///C:/Users/PC/gest-paiement/gestion-paiements-v2/src/main/java/com/gestionpaiements/app/controller/PaiementListController.java:java/math/BigDecimal#ZERO.
file:///C:/Users/PC/gest-paiement/gestion-paiements-v2/src/main/java/com/gestionpaiements/app/controller/PaiementListController.java
empty definition using pc, found symbol in pc: java/math/BigDecimal#ZERO.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 18982
uri: file:///C:/Users/PC/gest-paiement/gestion-paiements-v2/src/main/java/com/gestionpaiements/app/controller/PaiementListController.java
text:
```scala
package com.gestionpaiements.app.controller;

import com.gestionpaiements.app.model.Paiement;
import com.gestionpaiements.app.model.Professeur;
import com.gestionpaiements.app.model.TypePaiement;
import com.gestionpaiements.app.service.PaiementService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import com.gestionpaiements.app.service.ExcelFileManagerService;
import java.io.File;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.gestionpaiements.app.model.LigneDeplacement;



@Component
public class PaiementListController {

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private ExcelFileManagerService excelFileManagerService;

    private Runnable onBack; // callback fourni par le parent pour "revenir"

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @FXML
    private TableView<Paiement> paiementTable;
    @FXML
    private TableColumn<Paiement, String> colCin;
    @FXML
    private TableColumn<Paiement, String> colPpr;
    @FXML
    private TableColumn<Paiement, String> colNom;
    @FXML
    private TableColumn<Paiement, String> colPrenom;
    @FXML
    private TableColumn<Paiement, String> colGrade;
    @FXML
    private TableColumn<Paiement, String> colEchelle;
    @FXML
    private TableColumn<Paiement, String> colRib;
    @FXML
    private TableColumn<Paiement, String> colObjetReglement;
    @FXML
    private TableColumn<Paiement, String> colDateDebut;
    @FXML
    private TableColumn<Paiement, String> colDateFin;
    @FXML
    private TableColumn<Paiement, String> colNombreHeures;
    @FXML
    private TableColumn<Paiement, String> colTaux;
    @FXML
    private TableColumn<Paiement, String> colIr;
    @FXML
    private TableColumn<Paiement, String> colMontantBrut;
    @FXML
    private TableColumn<Paiement, String> colRetenueIr;
    @FXML
    private TableColumn<Paiement, String> colMontantNet;
    @FXML
    private TableColumn<Paiement, String> colTypeReference;
    @FXML
    private TableColumn<Paiement, String> colReference;
    @FXML
    private TableColumn<Paiement, String> colDatePaiement;

    @FXML
    private Label emptyLabel;
    @FXML
    private TextField searchField;
    @FXML
    private Button refreshButton;
    @FXML
    private Button retourButton;
    @FXML
    private Button importButton;

    private TypePaiement currentType;
    private ObservableList<Paiement> masterData = FXCollections.observableArrayList();
    private FilteredList<Paiement> filteredData;


    @FXML
    private Button deleteButton;
    // Called after FXML is loaded
    @FXML
    private void initialize() {
        // Setup columns with lambdas to handle null values and compute RIB
        colCin.setCellValueFactory(cellData -> {
            Paiement paiement = cellData.getValue();
            Professeur prof = paiement.getProfesseur();
            String value = (prof != null && prof.getCin() != null) ? prof.getCin() : "";
            return new javafx.beans.property.SimpleStringProperty(value);
        });
        colPpr.setCellValueFactory(cellData -> {
            Paiement paiement = cellData.getValue();
            Professeur prof = paiement.getProfesseur();
            String value = (prof != null && prof.getPpr() != null) ? prof.getPpr() : "";
            return new javafx.beans.property.SimpleStringProperty(value);
        });
        colNom.setCellValueFactory(cellData -> {
            Paiement paiement = cellData.getValue();
            Professeur prof = paiement.getProfesseur();
            String value = (prof != null && prof.getNom() != null) ? prof.getNom() : "";
            return new javafx.beans.property.SimpleStringProperty(value);
        });
        colPrenom.setCellValueFactory(cellData -> {
            Paiement paiement = cellData.getValue();
            Professeur prof = paiement.getProfesseur();
            String value = (prof != null && prof.getPrenom() != null) ? prof.getPrenom() : "";
            return new javafx.beans.property.SimpleStringProperty(value);
        });
        colGrade.setCellValueFactory(cellData -> {
            Paiement paiement = cellData.getValue();
            Professeur prof = paiement.getProfesseur();
            String value = (prof != null && prof.getGrade() != null) ? prof.getGrade() : "";
            return new javafx.beans.property.SimpleStringProperty(value);
        });
        colEchelle.setCellValueFactory(cellData -> {
            Paiement paiement = cellData.getValue();
            Professeur prof = paiement.getProfesseur();
            Integer echelle = (prof != null) ? prof.getEchelle() : null;
            String value = (echelle != null) ? echelle.toString() : "";
            return new javafx.beans.property.SimpleStringProperty(value);
        });
        colRib.setCellValueFactory(cellData -> {
            Paiement paiement = cellData.getValue();
            Professeur prof = paiement.getProfesseur();
            if (prof == null) {
                System.out.println("DEBUG RIB: prof is null for paiement " + paiement.getIdPaiement());
                return new javafx.beans.property.SimpleStringProperty("");
            }
            String rib = prof.getRibComplet();
            System.out.println("DEBUG RIB: paiementId=" + paiement.getIdPaiement() + ", ribBanque=" + prof.getRibBanque() + ", ribVille=" + prof.getRibVille() + ", ribNumeroCompte=" + prof.getRibNumeroCompte() + ", ribCle=" + prof.getRibCle() + " => rib=" + rib);
            return new javafx.beans.property.SimpleStringProperty(rib);
        });
        colObjetReglement.setCellValueFactory(new PropertyValueFactory<>("objetReglement"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colNombreHeures.setCellValueFactory(new PropertyValueFactory<>("nombreHeures"));
        colTaux.setCellValueFactory(new PropertyValueFactory<>("taux"));
        colIr.setCellValueFactory(new PropertyValueFactory<>("tauxIr"));
        colMontantBrut.setCellValueFactory(new PropertyValueFactory<>("montantBrut"));
        colRetenueIr.setCellValueFactory(new PropertyValueFactory<>("retenueIr"));
        colMontantNet.setCellValueFactory(new PropertyValueFactory<>("montantNet"));
        colTypeReference.setCellValueFactory(new PropertyValueFactory<>("typeReferenceReglement"));
        colReference.setCellValueFactory(new PropertyValueFactory<>("referenceReglement"));
        colDatePaiement.setCellValueFactory(new PropertyValueFactory<>("datePaiement"));

        // For numeric columns, we may want to format; but leave as String (they are BigDecimal, will call toString)
        // For date columns, they are LocalDate; toString will produce ISO format; we can later format.

        //nv 
        paiementTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // Search
        filteredData = new FilteredList<>(masterData, p -> true);
        paiementTable.setItems(filteredData);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilter(newValue);
        });
    }

    /** Set the type of payments to display and load data */
    public void setType(TypePaiement type) {
        this.currentType = type;
        loadData();
    }

    private void loadData() {
        if (currentType == null) {
            masterData.clear();
            return;
        }
        List<Paiement> list = paiementService.listerParType(currentType);
        masterData.setAll(list);
        // Debug: print first few paiements rib
        System.out.println("LOADING DATA for type " + currentType + ", count=" + list.size());
        int limit = Math.min(5, list.size());
        for (int i = 0; i < limit; i++) {
            Paiement p = list.get(i);
            Professeur prof = p.getProfesseur();
            if (prof != null) {
                System.out.println("  Paiement[" + p.getIdPaiement() + "] profId=" + prof.getIdProfesseur() +
                        " ribBanque='" + prof.getRibBanque() +
                        "' ribVille='" + prof.getRibVille() +
                        "' ribNumeroCompte='" + prof.getRibNumeroCompte() +
                        "' ribCle='" + prof.getRibCle() +
                        "' ribComplet='" + prof.getRibComplet() + "'");
            } else {
                System.out.println("  Paiement[" + p.getIdPaiement() + "] prof is null");
            }
        }
        String query = "";
        if (searchField != null) {
            query = searchField.getText();
        }
        applyFilter(query);
    }

    private void applyFilter(String query) {
        if (query == null || query.isEmpty()) {
            filteredData.setPredicate(p -> true);
        } else {
            String lower = query.toLowerCase();
            filteredData.setPredicate(paiement -> {
                Professeur prof = paiement.getProfesseur();
                if (prof == null) return false;
                return (prof.getCin() != null && prof.getCin().toLowerCase().contains(lower))
 (prof.getPpr() != null && prof.getPpr().toLowerCase().contains(lower))
 (prof.getNom() != null && prof.getNom().toLowerCase().contains(lower))
 (prof.getPrenom() != null && prof.getPrenom().toLowerCase().contains(lower));
            });
        }
        // Update empty label visibility
        if (emptyLabel != null) {
            emptyLabel.setVisible(filteredData.isEmpty());
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleDeleteSelected() {
        List<Paiement> selected = new java.util.ArrayList<>(paiementTable.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            showAlert("Aucune sélection", "Veuillez sélectionner au moins un paiement à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer " + selected.size()
                + " paiement(s) sélectionné(s) ? Cette action est irréversible.");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    paiementService.supprimerPaiements(selected);
                    loadData();
                    showAlert("Suppression réussie", selected.size() + " paiement(s) supprimé(s).");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Impossible de supprimer : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleRetour() {
        if (onBack != null) {
            onBack.run();
        } else {
            // Repli si jamais cette page est encore ouverte en fenêtre séparée
            try {
                Stage stage = (Stage) retourButton.getScene().getWindow();
                stage.close();
            } catch (Exception ignored) {
            }
        }
    }

    @FXML
    private void handleExportExcel() {
        if (currentType == null) return;
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Exporter le fichier Excel actif");
            fileChooser.setInitialFileName(currentType.name() + "_export.xlsx");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));
            File destination = fileChooser.showSaveDialog(retourButton.getScene().getWindow());
            if (destination != null) {
                excelFileManagerService.exportActiveFile(currentType, destination);
                showAlert("Export réussi", "Le fichier a été exporté avec succès.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'exporter le fichier : " + e.getMessage());
        }
    }

    @FXML
    private void handleCloseExcel() {
        if (currentType == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment clôturer le fichier Excel actif de ce type ? "
                + "Il sera archivé et un nouveau fichier sera créé pour les prochains paiements.");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    excelFileManagerService.closeActiveFile(currentType);
                    showAlert("Fichier clôturé", "Le fichier a été archivé et un nouveau fichier actif a été créé.");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Impossible de clôturer le fichier : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleArchivesExcel() {
        if (currentType == null) return;
        try {
            List<File> archives = excelFileManagerService.getArchiveFiles(currentType);
            StringBuilder sb = new StringBuilder();
            if (archives.isEmpty()) {
                sb.append("Aucun fichier archivé pour ce type pour le moment.");
            } else {
                for (File f : archives) {
                    sb.append(f.getName()).append("\n");
                }
            }
            showAlert("Fichiers archivés", sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de lister les archives : " + e.getMessage());
        }
    }

    //
    @FXML
    private void handleImportExcel() {
        if (currentType == null) return;

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Importer un fichier Excel");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));
        File file = fileChooser.showOpenDialog(retourButton.getScene().getWindow());
        if (file == null) return;

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        int imported = 0;
        int skipped = 0;

        try (FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            for (int r = 4; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String cin = getCellString(row, 8);
                if (cin.isEmpty()) continue; // ligne vide, on saute

                try {
                    Professeur prof = new Professeur();
                    prof.setCin(cin);
                    prof.setNom(getCellString(row, 9));
                    prof.setPrenom(getCellString(row, 10));
                    String ddrStr = getCellString(row, 11);
                    if (!ddrStr.isEmpty()) {
                        try { prof.setDdr(LocalDate.parse(ddrStr, dateFormat)); } catch (Exception ignored) {}
                    }
                    String echelleStr = getCellString(row, 0);
                    if (!echelleStr.isEmpty()) {
                        try { prof.setEchelle(Integer.parseInt(echelleStr.trim())); } catch (Exception ignored) {}
                    }
                    prof.setRibBanque(getCellString(row, 14));
                    prof.setRibVille(getCellString(row, 15));
                    prof.setRibNumeroCompte(getCellString(row, 16));
                    prof.setRibCle(getCellString(row, 17));

                    Paiement paiement = new Paiement();
                    paiement.setProfesseur(prof);
                    paiement.setTypePaiement(currentType);

                    String dateDebutStr = getCellString(row, 1);
                    String dateFinStr = getCellString(row, 2);
                    LocalDate dateDebut = dateDebutStr.isEmpty() ? null : LocalDate.parse(dateDebutStr, dateFormat);
                    LocalDate dateFin = dateFinStr.isEmpty() ? null : LocalDate.parse(dateFinStr, dateFormat);
                    paiement.setDateDebut(dateDebut);
                    paiement.setDateFin(dateFin);

                    BigDecimal nombre = parseCellNumber(row, 3);
                    BigDecimal taux = parseCellNumber(row, 4);
                    BigDecimal brut = parseCellNumber(row, 5);
                    BigDecimal retenues = parseCellNumber(row, 6);
                    BigDecimal montantNet = parseCellNumber(row, 7);

                    paiement.setNombreHeures(nombre);
                    paiement.setTaux(taux);

                    String modePaiementExcel = getCellString(row, 12);
                    paiement.setModePaiement("VIR".equalsIgnoreCase(modePaiementExcel) ? "VIREMENT" : modePaiementExcel);
                    paiement.setTypeReferenceReglement(getCellString(row, 13));
                    paiement.setObjetReglement(getCellString(row, 18));
                    paiement.setReferenceReglement(getCellString(row, 19));

                    if (currentType == TypePaiement.DEPLACEMENT) {
                        // Reconstruction d'un trajet unique agrégé (voir limitation connue)
                        paiement.setMotifDeplacement("Importé depuis Excel");
                        LigneDeplacement ligne = new LigneDeplacement();
                        ligne.setPaiement(paiement);
                        ligne.setDateDepart(dateDebut);
                        ligne.setDateArrivee(dateFin);
                        ligne.setParcours("");
                        ligne.setNombreTauxBase(nombre);
                        ligne.setTauxBaseApplique(taux);
                        ligne.setMontant(montantNet != null ? montantNet : BigDecimal.ZE@@RO);
                        paiement.getLignesDeplacement().add(ligne);

                        paiement.setMontantBrut(brut != null ? brut : BigDecimal.ZERO);
                        paiement.setRetenueIr(BigDecimal.ZERO);
                        paiement.setMontantNet(montantNet != null ? montantNet : BigDecimal.ZERO);
                    } else {
                        BigDecimal tauxIr = BigDecimal.ZERO;
                        if (brut != null && brut.compareTo(BigDecimal.ZERO) > 0 && retenues != null) {
                            tauxIr = retenues.divide(brut, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                        }
                        paiement.setTauxIr(tauxIr);
                    }

                    paiementService.enregistrerPaiement(paiement);
                    imported++;

                } catch (Exception rowEx) {
                    rowEx.printStackTrace();
                    skipped++;
                }
            }

            loadData();
            showAlert("Import terminé", imported + " paiement(s) importé(s), " + skipped + " ligne(s) ignorée(s) (erreur ou incomplète).");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'importer le fichier : " + e.getMessage());
        }
    }

    private String getCellString(Row row, int colIndex) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(colIndex);
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC) {
            double val = cell.getNumericCellValue();
            if (val == Math.floor(val)) return String.valueOf((long) val);
            return String.valueOf(val);
        }
        return cell.getStringCellValue() != null ? cell.getStringCellValue().trim() : "";
    }

    private BigDecimal parseCellNumber(Row row, int colIndex) {
        String s = getCellString(row, colIndex).replace(",", ".").replace(" ", "");
        if (s.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(s);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }


//
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: java/math/BigDecimal#ZERO.