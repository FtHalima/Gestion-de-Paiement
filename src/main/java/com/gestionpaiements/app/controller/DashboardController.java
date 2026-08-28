package com.gestionpaiements.app.controller;

import com.gestionpaiements.app.model.Paiement;
import com.gestionpaiements.app.model.TypePaiement;
import com.gestionpaiements.app.service.PaiementService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class DashboardController {

    @Autowired
    private PaiementService paiementService;

    @FXML private Label countVacataire;
    @FXML private Label countHeureSup;
    @FXML private Label countDeplacement;
    @FXML private Label countTotal;

    @FXML private TableView<Paiement> recentsTable;
    @FXML private TableColumn<Paiement, String> colDate;
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

        colDate.setCellValueFactory(data -> {
            Paiement p = data.getValue();
            String dateStr = p.getDatePaiement() != null ? p.getDatePaiement().format(dateFormatter) : "-";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        colNom.setCellValueFactory(data -> {
            Paiement p = data.getValue();
            String nom = p.getProfesseur() != null
                    ? p.getProfesseur().getNom() + " " + p.getProfesseur().getPrenom()
                    : "-";
            return new javafx.beans.property.SimpleStringProperty(nom);
        });
        colType.setCellValueFactory(data -> {
            TypePaiement type = data.getValue().getTypePaiement();
            return new javafx.beans.property.SimpleStringProperty(type != null ? type.getLibellePdf() : "-");
        });
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
    }
}