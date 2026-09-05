package com.gestionpaiements.app.controller;

import com.gestionpaiements.app.model.TypePaiement;
import com.gestionpaiements.app.service.ExcelFileManagerService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

import com.gestionpaiements.app.service.PaiementService;

@Component
public class ExcelFilesController {

    @Autowired
    private ExcelFileManagerService excelFileManagerService;

    @Autowired
    private PaiementService paiementService;

    @FXML private Label pageTitleLabel;
    @FXML private TableView<File> filesTable;
    @FXML private TableColumn<File, String> colFileName;
    @FXML private TableColumn<File, String> colFileStatus;

    @FXML private VBox browseSection;
    @FXML private VBox viewSection;
    @FXML private Label viewFileNameLabel;
    @FXML private TableView<ExcelFileManagerService.LigneApercu> apercuTable;
    @FXML private TableColumn<ExcelFileManagerService.LigneApercu, String> colCin;
    @FXML private TableColumn<ExcelFileManagerService.LigneApercu, String> colNom;
    @FXML private TableColumn<ExcelFileManagerService.LigneApercu, String> colPrenom;
    @FXML private TableColumn<ExcelFileManagerService.LigneApercu, String> colDateDebut;
    @FXML private TableColumn<ExcelFileManagerService.LigneApercu, String> colDateFin;
    @FXML private TableColumn<ExcelFileManagerService.LigneApercu, String> colMontantNet;
    @FXML private Button closeOrReactivateButton;
    @FXML private TableColumn<File, Number> colFileId;

    private TypePaiement currentType;
    private File selectedFile;

    public void setType(TypePaiement type) {
        this.currentType = type;
        pageTitleLabel.setText("Fichiers " + libelle(type));
        showBrowse();
        loadFiles();
    }

    private String libelle(TypePaiement type) {
        switch (type) {
            case VACATAIRE: return "Vacataire";
            case HEURE_SUP: return "Heure Sup";
            case DEPLACEMENT: return "Déplacement";
            default: return "";
        }
    }

    @FXML
    public void initialize() {
        colFileName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        colFileStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
           excelFileManagerService.isFileActive(data.getValue()) ? "Actif" : "Archivé"));
        colFileStatus.setCellFactory(col -> new TableCell<File, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("Actif".equals(item)
                            ? "-fx-text-fill: #2e9e5b; -fx-font-weight: bold;"
                            : "-fx-text-fill: #c0392b; -fx-font-weight: bold;");
                }
            }
        });

        colCin.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().cin));
        colNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().nom));
        colPrenom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().prenom));
        colDateDebut.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().dateDebut));
        colDateFin.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().dateFin));
        colMontantNet.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().montantNet));
        colFileId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(0));
        colFileId.setCellFactory(col -> new TableCell<File, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
    }

    private void loadFiles() {
        try {
            List<File> files = excelFileManagerService.listAllFilesForType(currentType);
            filesTable.setItems(FXCollections.observableArrayList(files));
            filesTable.setFixedCellSize(32);
            filesTable.prefHeightProperty().bind(
                    javafx.beans.binding.Bindings.size(filesTable.getItems())
                            .multiply(filesTable.getFixedCellSize()).add(34));
            filesTable.setMaxHeight(javafx.scene.control.Control.USE_PREF_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la liste des fichiers : " + e.getMessage());
        }
    }

    @FXML
    private void handleExportSelected() {
        File selected = filesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner un fichier.");
            return;
        }
        exportFile(selected);
    }

    @FXML
    private void handleViewSelected() {
        File selected = filesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner un fichier.");
            return;
        }
        this.selectedFile = selected;
        viewFileNameLabel.setText(selected.getName());
        boolean active = excelFileManagerService.isFileActive(selected);
        closeOrReactivateButton.setText(active ? "Clôturer" : "Réactiver");
        try {
            apercuTable.setItems(FXCollections.observableArrayList(excelFileManagerService.lireApercuFichier(selected)));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de lire le contenu du fichier : " + e.getMessage());
        }
        showView();
    }

    @FXML
    private void handleCloseOrReactivate() {
        if (selectedFile == null) return;
        boolean active = excelFileManagerService.isFileActive(selectedFile);
        if (active) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Clôturer le fichier");
            dialog.setHeaderText(null);
            dialog.setContentText("Nom pour l'archive (laisser vide pour un nom automatique) :");
            dialog.showAndWait().ifPresent(name -> {
                try {
                    excelFileManagerService.closeActiveFileAs(currentType, name);
                    showAlert("Fichier clôturé", "Le fichier a été archivé.");
                    showBrowse();
                    loadFiles();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Impossible de clôturer : " + e.getMessage());
                }
            });
        } else {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText(null);
            confirm.setContentText("Réactiver ce fichier ? Le fichier actif actuel sera archivé automatiquement.");
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        excelFileManagerService.reactiverFichierArchive(selectedFile, currentType);
                        showAlert("Fichier réactivé", "Ce fichier est maintenant actif.");
                        showBrowse();
                        loadFiles();
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Erreur", "Impossible de réactiver : " + e.getMessage());
                    }
                }
            });
        }
    }

    @FXML
    private void handleDeleteFile() {
        if (selectedFile == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer définitivement « " + selectedFile.getName() + " » ? "
                + "Cela supprimera aussi TOUS les paiements associés dans la base de données.");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    List<ExcelFileManagerService.LigneApercu> lignes = excelFileManagerService.lireApercuFichier(selectedFile);
                    for (ExcelFileManagerService.LigneApercu l : lignes) {
                        if (l.idPaiement != null) {
                            paiementService.supprimerParId(l.idPaiement);
                        }
                    }
                    excelFileManagerService.deleteFile(selectedFile);
                    showAlert("Fichier supprimé", "Le fichier et les paiements associés ont été supprimés.");
                    showBrowse();
                    loadFiles();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Impossible de supprimer : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleExportFromView() {
        if (selectedFile != null) exportFile(selectedFile);
    }

    @FXML
    private void handleRenameFile() {
        if (selectedFile == null) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Renommer le fichier");
        dialog.setHeaderText(null);
        dialog.setContentText("Nouveau nom (sans préfixe ni extension) :");
        dialog.showAndWait().ifPresent(name -> {
            try {
                excelFileManagerService.renameFile(selectedFile, name);
                showAlert("Fichier renommé", "Le fichier a été renommé.");
                showBrowse();
                loadFiles();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de renommer : " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleRetourList() {
        showBrowse();
        loadFiles();
    }

    @FXML
    private void handleModifySelected() {
        ExcelFileManagerService.LigneApercu selected = apercuTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner une ligne à modifier.");
            return;
        }
        if (mainViewController != null) {
            mainViewController.showAjouterPaiementAndSearch(selected.cin);
        }
    }

    private void exportFile(File file) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter le fichier");
            fileChooser.setInitialFileName(file.getName());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));
            File destination = fileChooser.showSaveDialog(filesTable.getScene().getWindow());
            if (destination != null) {
                excelFileManagerService.exportFile(file, destination);
                showAlert("Export réussi", "Le fichier a été exporté avec succès.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'exporter : " + e.getMessage());
        }
    }

    private void showBrowse() {
        browseSection.setVisible(true);
        browseSection.setManaged(true);
        viewSection.setVisible(false);
        viewSection.setManaged(false);
    }

    private void showView() {
        browseSection.setVisible(false);
        browseSection.setManaged(false);
        viewSection.setVisible(true);
        viewSection.setManaged(true);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private MainViewController mainViewController;

public void setMainViewController(MainViewController mvc) {
    this.mainViewController = mvc;
}


}