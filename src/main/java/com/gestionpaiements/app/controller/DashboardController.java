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
    private com.gestionpaiements.app.service.ImportProfesseursService importService;

    @Autowired private ImportProfesseursView importView;
    @FXML private Button dernierImportButton;

    @FXML private javafx.scene.control.SplitMenuButton importButton;

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
        configurerStructureImport();
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
                        case "VACATAIRE": display = "Vacation"; bg = "#e8f8ee"; fg = "#2e9e5b"; break;
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

        javafx.concurrent.Task<com.gestionpaiements.app.model.ImportProfesseurs> task = new javafx.concurrent.Task<>() {
            @Override protected com.gestionpaiements.app.model.ImportProfesseurs call() { return importService.importer(file); }
        };
        importButton.setDisable(true); dernierImportButton.setDisable(true);
        importButton.setText("Import en cours…");
        Runnable restore = () -> {
            importButton.setDisable(false); dernierImportButton.setDisable(false);
            importButton.setText("Importer des données");
        };
        task.setOnSucceeded(e -> { restore.run(); importView.afficher(importButton.getScene().getWindow(), task.getValue()); });
        task.setOnFailed(e -> { restore.run(); showAlert("Erreur", "Impossible d’enregistrer cet import. Vérifiez l’accès à la base de données."); });
        Thread thread = new Thread(task, "import-professeurs");
        thread.setDaemon(true); thread.start();
    }

    @FXML private void handleVoirImport() {
        importView.dernier(importButton.getScene().getWindow());
    }

    private void configurerStructureImport() {
        javafx.scene.layout.GridPane colonnes = new javafx.scene.layout.GridPane();
        colonnes.setHgap(18); colonnes.setVgap(8);
        String[] regles = {"Obligatoire · texte · unique", "Obligatoire · texte · unique",
                "Obligatoire", "Obligatoire", "Facultatif", "Entier positif, si renseigné",
                "3 chiffres", "3 chiffres", "16 chiffres · format texte", "2 chiffres"};
        colonnes.addRow(0, new Label("Colonne Excel"), new Label("En-tête"), new Label("Règle"));
        for (int i = 0; i < 10; i++) {
            Label position = new Label(String.valueOf((char) ('A' + i)));
            Label champ = new Label(com.gestionpaiements.app.service.ImportProfesseursService.COLONNES.get(i));
            champ.setStyle("-fx-font-weight: bold;");
            colonnes.addRow(i + 1, position, champ, new Label(regles[i]));
        }
        Label titre = new Label("Structure du fichier Excel à importer");
        titre.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Label aide = new Label("Fichier .xlsx · première feuille · une personne par ligne.\n"
                + "Placez ces en-têtes sur une même ligne, puis les données en dessous.\n"
                + "Le RIB doit être complet (banque, ville, compte et clé) ou entièrement vide.\n"
                + "Utilisez le format texte pour CIN, PPR et RIB afin de garder les zéros.");
        aide.setWrapText(true); aide.setMaxWidth(510);
        javafx.scene.layout.VBox contenu = new javafx.scene.layout.VBox(14, titre, colonnes, aide);
        contenu.setPadding(new javafx.geometry.Insets(14));
        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(contenu);
        scroll.setFitToWidth(true); scroll.setPrefViewportWidth(550); scroll.setPrefViewportHeight(440);
        javafx.scene.control.CustomMenuItem structure = new javafx.scene.control.CustomMenuItem(scroll, false);
        importButton.getItems().setAll(structure);
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
