error id: file:///C:/Users/PC/gest-paiement/gestion-paiements-v2/src/main/java/com/gestionpaiements/app/controller/PaiementListController.java:java/lang/Exception#
file:///C:/Users/PC/gest-paiement/gestion-paiements-v2/src/main/java/com/gestionpaiements/app/controller/PaiementListController.java
empty definition using pc, found symbol in pc: java/lang/Exception#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 12566
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

    private TypePaiement currentType;
    private ObservableList<Paiement> masterData = FXCollections.observableArrayList();
    private FilteredList<Paiement> filteredData;

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
    } catch (Excep@@tion e) {
        e.printStackTrace();
        showAlert("Erreur", "Impossible de lister les archives : " + e.getMessage());
    }
}

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

empty definition using pc, found symbol in pc: java/lang/Exception#