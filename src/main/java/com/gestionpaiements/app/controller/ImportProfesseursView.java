package com.gestionpaiements.app.controller;

import com.gestionpaiements.app.model.*;
import com.gestionpaiements.app.service.ImportProfesseursService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
public class ImportProfesseursView {
    private final ImportProfesseursService service;
    private final DateTimeFormatter date = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    public ImportProfesseursView(ImportProfesseursService service) { this.service = service; }

    public void dernier(Window owner) {
        service.dernier().ifPresentOrElse(lot -> afficher(owner, lot), () -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucun import enregistré. Les imports effectués avant cette fonctionnalité ne figurent pas dans l’historique.");
            alert.initOwner(owner); alert.setHeaderText(null); alert.showAndWait();
        });
    }

    public void historique(Window owner) {
        TableView<ImportProfesseurs> table = new TableView<>();
        table.getColumns().add(colonne("Fichier", ImportProfesseurs::getNomFichier, 300));
        table.getColumns().add(colonne("Date d’import", lot -> date.format(lot.getDateImport()), 180));
        table.getColumns().add(colonne("État du traitement", ImportProfesseurs::getEtat, 150,
                lot -> "Échec".equals(lot.getEtat()) || "Terminé avec erreurs".equals(lot.getEtat())));
        table.getColumns().add(colonne("Message", lot -> texte(lot.getErreur()), 380,
                lot -> !texte(lot.getErreur()).isBlank()));
        table.setItems(FXCollections.observableArrayList(service.historique()));
        table.setPlaceholder(new Label("Aucun historique disponible pour les imports antérieurs à cette fonctionnalité."));
        Button consulter = new Button("Voir les données de cet import");
        consulter.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        consulter.setOnAction(e -> afficher(table.getScene().getWindow(), table.getSelectionModel().getSelectedItem()));
        table.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) consulter.fire(); });
        Stage stage = ouvrir(owner, "Historique des imports", new VBox(12, new Label("Sélectionnez un fichier pour consulter ses données et corriger ses lignes."), table, consulter), 1080, 550);
        stage.focusedProperty().addListener((a,b,focused) -> { if (focused) table.setItems(FXCollections.observableArrayList(service.historique())); });
    }

    public void afficher(Window owner, ImportProfesseurs lot) {
        TableView<LigneImportProfesseur> table = new TableView<>();
        table.getColumns().add(colonne("Ligne Excel", l -> String.valueOf(l.getNumeroLigne()), 80));
        table.getColumns().add(colonne("Statut", LigneImportProfesseur::getStatut, 95));
        for (int i = 0; i < 10; i++) {
            final int index = i;
            table.getColumns().add(colonne(ImportProfesseursService.COLONNES.get(i), l -> service.valeurs(l)[index], i == 8 ? 180 : 105,
                    l -> ImportProfesseursService.colonneEnErreur(l, index)));
        }
        table.getColumns().add(colonne("Erreur / motif", l -> texte(l.getErreur()), 330,
                l -> !texte(l.getErreur()).isBlank()));
        table.getColumns().add(colonne("Dernière correction", l -> l.getDateCorrection() == null ? "" : date.format(l.getDateCorrection()), 170));
        table.setPlaceholder(new Label("Aucune ligne à afficher."));
        table.setRowFactory(t -> new TableRow<>() {
            @Override protected void updateItem(LigneImportProfesseur item, boolean empty) {
                super.updateItem(item, empty);
                setTooltip(!empty && item != null && item.getErreur() != null ? new Tooltip(item.getErreur()) : null);
            }
        });
        var rows = FXCollections.observableArrayList(service.lignes(lot.getId()));
        FilteredList<LigneImportProfesseur> filtered = new FilteredList<>(rows);
        table.setItems(filtered);
        TextField recherche = new TextField(); recherche.setPromptText("Rechercher par nom, CIN ou PPR"); recherche.setPrefWidth(340);
        CheckBox erreurs = new CheckBox("Lignes en erreur uniquement");
        Runnable filtrer = () -> filtered.setPredicate(l -> {
            String q = recherche.getText().trim().toLowerCase(Locale.ROOT);
            String[] v = service.valeurs(l);
            return (!erreurs.isSelected() || "Erreur".equals(l.getStatut())) &&
                    (v[0] + " " + v[1] + " " + v[2] + " " + v[3]).toLowerCase(Locale.ROOT).contains(q);
        });
        recherche.textProperty().addListener((a,b,c) -> filtrer.run());
        erreurs.selectedProperty().addListener((a,b,c) -> filtrer.run());
        Label bilan = new Label();
        Runnable actualiser = () -> {
            rows.setAll(service.lignes(lot.getId()));
            long bad = rows.stream().filter(l -> "Erreur".equals(l.getStatut())).count();
            long ok = rows.stream().filter(l -> l.getProfesseurId() != null).count();
            bilan.setText(rows.size() + " ligne(s) • " + ok + " enregistrée(s) • " + bad + " en erreur");
            filtrer.run();
        };
        actualiser.run();
        Label motif = new Label("Sélectionnez une ligne pour voir son état ou la corriger."); motif.setWrapText(true);
        table.getSelectionModel().selectedItemProperty().addListener((a,b,l) -> motif.setText(l == null ? "" :
                (l.getErreur() == null ? l.getStatut() : l.getErreur())));
        Button modifier = new Button("Modifier / corriger la ligne");
        modifier.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        modifier.setOnAction(e -> modifier(table.getScene().getWindow(), table.getSelectionModel().getSelectedItem(), actualiser));
        Label fichier = new Label(lot.getNomFichier() + " — " + date.format(lot.getDateImport()));
        fichier.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
        Label message = new Label(texte(lot.getErreur())); message.setWrapText(true);
        message.setStyle("-fx-text-fill: #b42318;");
        Button historique = new Button("Historique des imports");
        historique.getStyleClass().add("btn-secondary");
        historique.setOnAction(e -> historique(table.getScene().getWindow()));
        Region espace = new Region(); HBox.setHgrow(espace, Priority.ALWAYS);
        VBox content = new VBox(12, fichier, bilan, message, new HBox(15, recherche, erreurs), table, motif,
                new HBox(12, modifier, espace, historique));
        ouvrir(owner, "Données importées", content, 1180, 650);
    }

    private void modifier(Window owner, LigneImportProfesseur ligne, Runnable refresh) {
        Dialog<ButtonType> dialog = new Dialog<>(); dialog.initOwner(owner); dialog.setTitle("Corriger la ligne " + ligne.getNumeroLigne());
        ButtonType save = new ButtonType("Vérifier et enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        GridPane grid = new GridPane(); grid.setHgap(15); grid.setVgap(8);
        TextField[] fields = new TextField[10]; String[] values = service.valeurs(ligne);
        for (int i = 0; i < 10; i++) {
            fields[i] = new TextField(values[i]); fields[i].setPrefColumnCount(26);
            grid.addRow(i, new Label(ImportProfesseursService.COLONNES.get(i)), fields[i]);
        }
        Label information = new Label(ligne.getProfesseurId() == null ?
                "Une ligne valide sera ajoutée aux professeurs." : "La correction mettra à jour la fiche du professeur utilisée par l’application.");
        information.setWrapText(true);
        Label error = new Label(); error.setWrapText(true); error.setMaxWidth(490); error.setStyle("-fx-text-fill: #b42318;");
        dialog.getDialogPane().setContent(new VBox(12, information, grid, error));
        com.gestionpaiements.app.util.AppTheme.appliquer(dialog.getDialogPane());
        dialog.getDialogPane().lookupButton(save).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                String[] edited = new String[10]; for (int i = 0; i < 10; i++) edited[i] = fields[i].getText();
                service.corriger(ligne.getId(), edited); refresh.run();
            } catch (Exception ex) {
                event.consume(); error.setText(ex instanceof IllegalArgumentException ? ex.getMessage() : "Correction non enregistrée. Fermez puis rouvrez cet import et réessayez.");
            }
        });
        dialog.showAndWait();
    }

    private <T> TableColumn<T, String> colonne(String nom, Function<T, String> value, double width) {
        return colonne(nom, value, width, item -> false);
    }

    private <T> TableColumn<T, String> colonne(String nom, Function<T, String> value, double width, Predicate<T> enErreur) {
        TableColumn<T, String> col = new TableColumn<>(nom);
        col.setCellValueFactory(c -> new SimpleStringProperty(value.apply(c.getValue()))); col.setPrefWidth(width);
        col.setCellFactory(column -> new TableCell<>() {
            private final javafx.beans.value.ChangeListener<T> ligneChangee = (o, old, item) -> actualiserCouleur();
            {
                tableRowProperty().addListener((o, old, row) -> {
                    if (old != null) old.itemProperty().removeListener(ligneChangee);
                    if (row != null) row.itemProperty().addListener(ligneChangee);
                    actualiserCouleur();
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                actualiserCouleur();
            }
            private void actualiserCouleur() {
                T ligne = getTableRow() == null ? null : getTableRow().getItem();
                setStyle(!isEmpty() && ligne != null && enErreur.test(ligne)
                        ? "-fx-background-color: #ffe2e2; -fx-text-fill: #b42318;" : "");
            }
        });
        return col;
    }
    private String texte(String s) { return s == null ? "" : s; }
    private Stage ouvrir(Window owner, String title, VBox content, double width, double height) {
        Stage stage = new Stage(); stage.initOwner(owner); stage.initModality(Modality.WINDOW_MODAL); stage.setTitle(title);
        content.setPadding(new Insets(18));
        content.getChildren().stream().filter(n -> n instanceof TableView).forEach(n -> VBox.setVgrow(n, Priority.ALWAYS));
        Scene scene = new Scene(content, width, height);
        scene.getStylesheets().add(getClass().getResource("/com/gestionpaiements/app/fxml/dashboard.css").toExternalForm());
        com.gestionpaiements.app.util.AppTheme.appliquer(content);
        stage.setScene(scene); stage.show();
        return stage;
    }
}
