package com.gestionpaiements.app.controller;

import com.gestionpaiements.app.model.TypePaiement;
import com.gestionpaiements.app.service.ExcelFileManagerService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

import com.gestionpaiements.app.model.TypePaiement;

@Component
public class ArchivesController {

    @Autowired
    private ExcelFileManagerService excelFileManagerService;

    @FXML private ListView<File> archivesListView;
    @FXML private Button exportButton;
    @FXML private Button backButton;
    @FXML private Button reactivateButton;

    private Runnable onBack;

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @FXML
    public void initialize() {
        archivesListView.setCellFactory(lv -> new javafx.scene.control.ListCell<File>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                setText(empty || file == null ? null : file.getName());
            }
        });
        loadArchives();
    }

    private void loadArchives() {
        try {
            List<File> archives = excelFileManagerService.getArchiveFiles();
            archivesListView.setItems(FXCollections.observableArrayList(archives));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la liste des archives : " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        File selected = archivesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner un fichier dans la liste avant d'exporter.");
            return;
        }
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter le fichier archivé");
            fileChooser.setInitialFileName(selected.getName());
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));
            File destination = fileChooser.showSaveDialog(archivesListView.getScene().getWindow());
            if (destination != null) {
                excelFileManagerService.exportFile(selected, destination);
                showAlert("Export réussi", "Le fichier a été exporté avec succès.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'exporter le fichier : " + e.getMessage());
        }
    }


    @FXML
    private void handleReactivate() {
        File selected = archivesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner un fichier archivé à réactiver.");
            return;
        }

        TypePaiement type = excelFileManagerService.deduireTypeDepuisNomFichier(selected.getName());
        if (type == null) {
            showAlert("Erreur", "Impossible de déterminer le type de paiement pour ce fichier.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous réactiver « " + selected.getName() + " » ?\n\n"
                + "Le fichier actif actuel de ce type (s'il existe) sera automatiquement archivé.");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    excelFileManagerService.reactiverFichierArchive(selected, type);
                    loadArchives();
                    showAlert("Fichier réactivé", "« " + selected.getName() + " » est maintenant le fichier actif pour ce type. "
                            + "Les nouveaux paiements de ce type s'ajouteront désormais dedans.");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Impossible de réactiver le fichier : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleBack() {
        if (onBack != null) {
            onBack.run();
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