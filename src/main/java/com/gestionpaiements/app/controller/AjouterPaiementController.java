package com.gestionpaiements.app.controller;

import com.gestionpaiements.app.model.Professeur;
import com.gestionpaiements.app.model.Paiement;
import com.gestionpaiements.app.model.TypePaiement;
import com.gestionpaiements.app.model.Utilisateur;
import com.gestionpaiements.app.model.Lot;
import com.gestionpaiements.app.service.ProfesseurService;
import com.gestionpaiements.app.service.PaiementService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.gestionpaiements.app.service.LotService;
import com.gestionpaiements.app.service.SessionUtilisateur;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class AjouterPaiementController {

    @Autowired
    private ProfesseurService professeurService;

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private SessionUtilisateur sessionUtilisateur;

    @Autowired
    private LotService lotService;

    // Fields from FXML
    @FXML private TextField cinField;
    @FXML private TextField pprField;
    @FXML private Button searchButton;
    @FXML private Button createProfButton;
    @FXML private Label profNotFoundLabel;

    // Display labels (professor info)
    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label ddrLabel;
    @FXML private Label gradeLabel;
    @FXML private Label echelleLabel;
    @FXML private Label affectationLabel;
    @FXML private VBox profInfoDisplay;

    // Edit fields (for new professor)
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private DatePicker ddrPicker;
    @FXML private ComboBox<String> gradeField; // Changed to ComboBox
    @FXML private TextField echelleField;
    @FXML private TextField affectationField;
    @FXML private VBox profInfoEdit;

    // RIB fields
    @FXML private TextField banqueField;
    @FXML private TextField villeField;
    @FXML private TextField numeroCompteField;
    @FXML private TextField cleField;

    // Paiement info
    @FXML private ComboBox<TypePaiement> typePaiementCombo;
    @FXML private Label objetReglementLabel;
    @FXML private DatePicker dateDebutField;
    @FXML private Label referenceReglementLabel;
    @FXML private DatePicker dateFinField;
    @FXML private TextField nombreHeuresField;
    @FXML private TextField tauxField; // non-editable
    @FXML private ComboBox<String> irCombo; // non-editable, holds "%" values
    @FXML private Label montantBrutLabel;
    @FXML private Label retenueIrLabel;
    @FXML private Label montantNetLabel;
    @FXML private Label modePaiementLabel;
    @FXML private Label typeReferenceReglementLabel;
    @FXML private DatePicker datePaiementField;

    // Buttons
    @FXML private Button cancelButton;
    @FXML private Button resetButton;
    @FXML private Button saveButton;

    // State
    private Professeur currentProfesseur; // null if not yet searched or not found
    private boolean isNewProfessorMode = false;

    // Formatters
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        // Populate comboboxes
        typePaiementCombo.setItems(FXCollections.observableArrayList(TypePaiement.values()));
        irCombo.setItems(FXCollections.observableArrayList("30", "34", "37"));
        irCombo.getSelectionModel().selectFirst(); // default 30%
        irCombo.setEditable(false);
        tauxField.setEditable(true);

        // Grade ComboBox
        gradeField.setItems(FXCollections.observableArrayList("PRIMAIRE", "SUPERIEUR"));

        // Set default date paiement to empty (no default date)
        datePaiementField.setValue(null);
        datePaiementField.setPromptText("jj/mm/aaaa");

        // Set labels defaults
        objetReglementLabel.setText("-");
        referenceReglementLabel.setText("-");
        typeReferenceReglementLabel.setText("RIB");
        modePaiementLabel.setText("VIREMENT");

        // Setup auto-focus for RIB fields
        setupRibAutoFocus();

        // Setup calculation listeners
        nombreHeuresField.textProperty().addListener((obs, oldVal, newVal) -> calculate());
        tauxField.textProperty().addListener((obs, oldVal, newVal) -> calculate());
        irCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> calculate());

        // Enter key to trigger search
        cinField.setOnKeyPressed(this::handleEnterKey);
        pprField.setOnKeyPressed(this::handleEnterKey);

        // Listeners for grade and echelle to update taux and IR
        gradeField.valueProperty().addListener((obs, oldVal, newVal) -> updateTauxEtIR());
        echelleField.textProperty().addListener((obs, oldVal, newVal) -> updateTauxEtIR());

        // Listener for typePaiement to update objet and reference labels
        typePaiementCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateObjetEtReference());

        // Listener for date validation
        dateDebutField.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());
        dateFinField.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());

        // Apply date format and prompt to date pickers
        applyDateFormat(dateDebutField);
        applyDateFormat(dateFinField);
        applyDateFormat(ddrPicker);
        applyDateFormat(datePaiementField);
    }

    /**
     * Applique un format de date jj/mm/aaaa et un invite texte au DatePicker donné.
     */
    private void applyDateFormat(DatePicker picker) {
        picker.setPromptText("jj/mm/aaaa");
        StringConverter<LocalDate> converter = new StringConverter<>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };
        picker.setConverter(converter);
    }

    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            searchProfessor();
            event.consume();
        }
    }

    @FXML
    private void searchProfessor() {
        String cin = cinField.getText().trim();
        String ppr = pprField.getText().trim();
        if (cin.isEmpty() && ppr.isEmpty()) {
            showError("Veuillez saisir le CIN ou le PPR.");
            return;
        }

        Optional<Professeur> opt = professeurService.rechercherParCinOuPpr(cin, ppr);
        if (opt.isPresent()) {
            currentProfesseur = opt.get();
            isNewProfessorMode = false;
            showProfesseurInfo(currentProfesseur);
            hideProfessorNotFound();
            hideProfessorEdit();
            showProfessorDisplay();
        } else {
            currentProfesseur = null;
            isNewProfessorMode = true;
            hideProfessorDisplay();
            hideProfessorEdit();
            showProfessorNotFound();
            // Pre-fill CIN and PPR already in fields; ready for creation
        }
    }

    @FXML
    private void createNewProfessor() {
        // Switch to edit mode for new professor
        hideProfessorNotFound();
        hideProfessorDisplay();
        showProfessorEdit();
        // Clear edit fields (except CIN/PPR which are already set)
        nomField.clear();
        prenomField.clear();
        ddrPicker.setValue(null);
        gradeField.getSelectionModel().clearSelection();
        echelleField.clear();
        affectationField.clear();
        // Focus on nomField
        nomField.requestFocus();
    }

    // UI helpers
    private void showProfesseurInfo(Professeur prof) {
        nomLabel.setText(prof.getNom());
        prenomLabel.setText(prof.getPrenom());
        ddrLabel.setText(prof.getDdr() != null ? prof.getDdr().format(dateFormatter) : "-");
        gradeLabel.setText(prof.getGrade());
        echelleLabel.setText(prof.getEchelle() != null ? prof.getEchelle().toString() : "-");
        affectationLabel.setText(prof.getAffectation());
    }

    private void showProfessorDisplay() {
        profInfoDisplay.setVisible(true);
        profInfoDisplay.setManaged(true);
    }

    private void hideProfessorDisplay() {
        profInfoDisplay.setVisible(false);
        profInfoDisplay.setManaged(false);
    }

    private void showProfessorEdit() {
        profInfoEdit.setVisible(true);
        profInfoEdit.setManaged(true);
    }

    private void hideProfessorEdit() {
        profInfoEdit.setVisible(false);
        profInfoEdit.setManaged(false);
    }

    private void showProfessorNotFound() {
        profNotFoundLabel.setVisible(true);
        profNotFoundLabel.setManaged(true);
        createProfButton.setVisible(true);
        createProfButton.setManaged(true);
    }

    private void hideProfessorNotFound() {
        profNotFoundLabel.setVisible(false);
        profNotFoundLabel.setManaged(false);
        createProfButton.setVisible(false);
        createProfButton.setManaged(false);
    }

    // RIB auto-focus logic
    private void setupRibAutoFocus() {
        // Banque -> Ville (3 digits)
        banqueField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() == 3) {
                villeField.requestFocus();
            } else if (newVal.length() > 3) {
                banqueField.setText(oldVal); // revert
            }
        });
        banqueField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.BACK_SPACE && banqueField.getText().isEmpty()) {
                // Move focus to previous field? There is none; keep.
            }
        });

        // Ville -> Numéro de compte (3 digits)
        villeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() == 3) {
                numeroCompteField.requestFocus();
            } else if (newVal.length() > 3) {
                villeField.setText(oldVal);
            }
        });
        villeField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.BACK_SPACE && villeField.getText().isEmpty()) {
                banqueField.requestFocus();
                banqueField.positionCaret(banqueField.getText().length());
            }
        });

        // Numéro de compte -> Clé (16 digits)
        numeroCompteField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() == 16) {
                cleField.requestFocus();
            } else if (newVal.length() > 16) {
                numeroCompteField.setText(oldVal);
            }
        });
        numeroCompteField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.BACK_SPACE && numeroCompteField.getText().isEmpty()) {
                villeField.requestFocus();
                villeField.positionCaret(villeField.getText().length());
            }
        });

        // Clé -> (2 digits) no forward focus
        cleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 2) {
                cleField.setText(oldVal);
            }
        });
        cleField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.BACK_SPACE && cleField.getText().isEmpty()) {
                numeroCompteField.requestFocus();
                numeroCompteField.positionCaret(numeroCompteField.getText().length());
            }
        });

        // Allow only digits
        TextField[] ribFields = {banqueField, villeField, numeroCompteField, cleField};
        for (TextField tf : ribFields) {
            tf.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) {
                    tf.setText(newVal.replaceAll("[^\\d]", ""));
                }
            });
        }
    }

    // Update taux and IR based on grade and echelle via service
    private void updateTauxEtIR() {
        String grade = gradeField.getValue();
        String echelleStr = echelleField.getText().trim();
        if (grade == null || grade.isEmpty() || echelleStr.isEmpty()) {
            // Clear fields
            tauxField.clear();
            irCombo.getSelectionModel().clearSelection();
            return;
        }
        Optional<ProfesseurService.TauxIR> opt = professeurService.trouverTauxEtIRParGradeEtEchelle(grade, echelleStr);
        if (opt.isPresent()) {
            ProfesseurService.TauxIR tirs = opt.get();
            tauxField.setText(tirs.getTaux().stripTrailingZeros().toPlainString());
            irCombo.getSelectionModel().select(tirs.getTauxIr().stripTrailingZeros().toPlainString());
        } else {
            // No configuration available -> clear fields and maybe show placeholder
            tauxField.clear();
            irCombo.getSelectionModel().clearSelection();
        }
        // After setting taux and IR, recalc amounts
        calculate();
    }

    // Update objet règlement and référence règlement labels based on selected type of payment
    private void updateObjetEtReference() {
        TypePaiement type = typePaiementCombo.getSelectionModel().getSelectedItem();
        if (type == null) {
            objetReglementLabel.setText("-");
            referenceReglementLabel.setText("-");
            return;
        }
        switch (type) {
            case VACATAIRE:
                objetReglementLabel.setText("VACATAIRE");
                referenceReglementLabel.setText("VACATAIRE");
                break;
            case HEURE_SUP:
                objetReglementLabel.setText("HEURE SUPPLÉMENTAIRE");
                referenceReglementLabel.setText("HEURE SUPPLÉMENTAIRE");
                break;
            case DEPLACEMENT:
                objetReglementLabel.setText("DEPLACEMENT");
                referenceReglementLabel.setText("DEPLACEMENT");
                break;
            default:
                objetReglementLabel.setText("-");
                referenceReglementLabel.setText("-");
        }
    }

    // Validate that date fin >= date debut
    private void validateDates() {
        LocalDate debut = dateDebutField.getValue();
        LocalDate fin = dateFinField.getValue();
        if (debut != null && fin != null && fin.isBefore(debut)) {
            showError("La date de fin doit être postérieure ou égale à la date de début.");
        }
    }

    // Calculation using PaiementService
    private void calculate() {
        TypePaiement type = typePaiementCombo.getSelectionModel().getSelectedItem();
        BigDecimal nombreHeures = parseBigDecimal(nombreHeuresField.getText());
        BigDecimal taux = parseBigDecimal(tauxField.getText());
        String irStr = irCombo.getSelectionModel().getSelectedItem();
        BigDecimal tauxIr = irStr != null ? new BigDecimal(irStr) : BigDecimal.ZERO;

        PaiementService.TauxIRResult result = paiementService.calculerMontants(type, nombreHeures, taux, tauxIr);
        montantBrutLabel.setText(formatBigDecimal(result.getMontantBrut()));
        retenueIrLabel.setText(formatBigDecimal(result.getRetenueIr()));
        montantNetLabel.setText(formatBigDecimal(result.getMontantNet()));
    }

    private BigDecimal parseBigDecimal(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String cleaned = text.replace(",", ".").trim();
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatBigDecimal(BigDecimal bd) {
        if (bd == null) {
            return "0,00";
        }
        return bd.stripTrailingZeros().toPlainString().replace(".", ",");
    }

    // Enregistrement du paiement
    @FXML
    private void saveAction() {
        if (!validateForm()) {
            return;
        }

        try {
            String cin = cinField.getText().trim();
            String ppr = pprField.getText().trim();

            // Préparer l'objet Professeur (à créer ou à récupérer)
            Professeur prof = new Professeur();
            prof.setCin(cin);
            prof.setPpr(ppr);

            if (isNewProfessorMode) {
                // Création d'un nouveau professeur : remplir tous les champs
                prof.setNom(nomField.getText().trim());
                prof.setPrenom(prenomField.getText().trim());
                prof.setDdr(ddrPicker.getValue());
                prof.setGrade(gradeField.getValue());
                String echelleStr = echelleField.getText().trim();
                prof.setEchelle(echelleStr.isEmpty() ? null : Integer.parseInt(echelleStr));
                prof.setAffectation(affectationField.getText().trim());
                prof.setRibBanque(banqueField.getText());
                prof.setRibVille(villeField.getText());
                prof.setRibNumeroCompte(numeroCompteField.getText());
                prof.setRibCle(cleField.getText());
            }
            // Si le professeur existe déjà, seuls CIN et PPR sont nécessaires pour la recherche

            // Informations de paiement
            TypePaiement typePaiement = typePaiementCombo.getSelectionModel().getSelectedItem();
            String objetReglement = objetReglementLabel.getText();
            String referenceReglement = referenceReglementLabel.getText();
            LocalDate dateDebut = dateDebutField.getValue();
            LocalDate dateFin = dateFinField.getValue();

            BigDecimal nombreHeures = parseBigDecimal(nombreHeuresField.getText());
            BigDecimal taux = parseBigDecimal(tauxField.getText());
            BigDecimal tauxIr = new BigDecimal(irCombo.getSelectionModel().getSelectedItem()); // toujours sélectionné

            // Calcul des montants
            PaiementService.TauxIRResult result = paiementService.calculerMontants(typePaiement, nombreHeures, taux, tauxIr);

            // Création du objet Paiement
            Paiement paiement = new Paiement();
            paiement.setProfesseur(prof); // sera éventuellement remplacé par le service
            paiement.setTypePaiement(typePaiement);
            paiement.setObjetReglement(objetReglement);
            paiement.setDateDebut(dateDebut);
            paiement.setDateFin(dateFin);
            paiement.setNombreHeures(nombreHeures);
            paiement.setTaux(taux);
            paiement.setTauxIr(tauxIr);
            paiement.setMontantBrut(result.getMontantBrut());
            paiement.setRetenueIr(result.getRetenueIr());
            paiement.setMontantNet(result.getMontantNet());
            paiement.setModePaiement("VIREMENT");
            paiement.setTypeReferenceReglement("RIB");
            paiement.setReferenceReglement(referenceReglement);
            paiement.setDatePaiement(datePaiementField.getValue()); // peut être null
            // Utilisateur connecté
            Utilisateur utilisateur = sessionUtilisateur.getUtilisateurConnecte();
            paiement.setUtilisateur(utilisateur);
            // Lot actif (existant ou créé)
            Lot actifLot = lotService.getOuCreerLotActif(utilisateur);
            paiement.setLot(actifLot);

            // Enregistrement (transactionnel)
            Paiement saved = paiementService.enregistrerPaiement(paiement);

            // Confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Enregistrement réussi");
            alert.setHeaderText(null);
            alert.setContentText("Paiement enregistré avec succès (ID : " + saved.getIdPaiement() + ").");
            alert.showAndWait();

            // Réinitialiser le formulaire pour un nouveau paiement
            resetAction();
        } catch (Exception ex) {
            // Gestion d'erreur générique (les détails peuvent être loggés)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'enregistrement");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de l'enregistrement : " + ex.getMessage());
            alert.showAndWait();
            // Log détaillé (optionnel)
            ex.printStackTrace();
        }
    }

    @FXML
    private void resetAction() {
        // Reset form to initial state
        cinField.clear();
        pprField.clear();
        nomLabel.setText("-");
        prenomLabel.setText("-");
        ddrLabel.setText("-");
        gradeLabel.setText("-");
        echelleLabel.setText("-");
        affectationLabel.setText("-");
        profInfoDisplay.setVisible(false);
        profInfoDisplay.setManaged(false);
        profInfoEdit.setVisible(false);
        profInfoEdit.setManaged(false);
        profNotFoundLabel.setVisible(false);
        profNotFoundLabel.setManaged(false);
        createProfButton.setVisible(false);
        createProfButton.setManaged(false);
        currentProfesseur = null;
        isNewProfessorMode = false;

        // Reset RIB
        banqueField.clear();
        villeField.clear();
        numeroCompteField.clear();
        cleField.clear();

        // Reset paiement info
        typePaiementCombo.getSelectionModel().selectFirst();
        objetReglementLabel.setText("-");
        referenceReglementLabel.setText("-");
        dateDebutField.setValue(null);
        dateFinField.setValue(null);
        nombreHeuresField.clear();
        tauxField.clear();
        irCombo.getSelectionModel().selectFirst();
        montantBrutLabel.setText("0,00");
        retenueIrLabel.setText("0,00");
        montantNetLabel.setText("0,00");
        modePaiementLabel.setText("VIREMENT");
        typeReferenceReglementLabel.setText("RIB");
        datePaiementField.setValue(null);
    }

    @FXML
    private void cancelAction() {
        // Close the window (if opened as separate stage)
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    // Validation
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        String cin = cinField.getText().trim();
        String ppr = pprField.getText().trim();

        // CIN et PPR obligatoires
        if (cin.isEmpty()) {
            errors.append("- CIN obligatoire\n");
        }
        if (ppr.isEmpty()) {
            errors.append("- PPR obligatoire\n");
        }

        if (isNewProfessorMode) {
            // Champs obligatoires pour création de nouveau professeur
            if (nomField.getText().trim().isEmpty()) {
                errors.append("- Nom du professeur obligatoire\n");
            }
            if (prenomField.getText().trim().isEmpty()) {
                errors.append("- Prénom du professeur obligatoire\n");
            }
            if (ddrPicker.getValue() == null) {
                errors.append("- Date de recrutement (DDR) obligatoire\n");
            }
            if (gradeField.getValue() == null) {
                errors.append("- Grade obligatoire\n");
            }
            if (echelleField.getText().trim().isEmpty()) {
                errors.append("- Échelle obligatoire\n");
            } else {
                try {
                    Integer.parseInt(echelleField.getText().trim());
                } catch (NumberFormatException e) {
                    errors.append("- Échelle doit être un entier\n");
                }
            }
            if (affectationField.getText().trim().isEmpty()) {
                errors.append("- Affectation obligatoire\n");
            }
        }

        // Validation du RIB (toujours obligatoire)
        if (!banqueField.getText().matches("\\d{3}")) {
            errors.append("- Banque doit contenir exactement 3 chiffres\n");
        }
        if (!villeField.getText().matches("\\d{3}")) {
            errors.append("- Ville doit contenir exactement 3 chiffres\n");
        }
        if (!numeroCompteField.getText().matches("\\d{16}")) {
            errors.append("- Numéro de compte doit contenir exactement 16 chiffres\n");
        }
        if (!cleField.getText().matches("\\d{2}")) {
            errors.append("- Clé doit contenir exactement 2 chiffres\n");
        }

        // Informations de paiement
        if (typePaiementCombo.getSelectionModel().getSelectedItem() == null) {
            errors.append("- Type de paiement obligatoire\n");
        }
        if (objetReglementLabel.getText().equals("-") || objetReglementLabel.getText().isEmpty()) {
            errors.append("- Objet du règlement non déterminé\n");
        }
        if (referenceReglementLabel.getText().equals("-") || referenceReglementLabel.getText().isEmpty()) {
            errors.append("- Référence du règlement non déterminé\n");
        }
        if (!isValidDate(dateDebutField.getValue())) {
            errors.append("- Date de début invalide (jj/mm/aaaa)\n");
        }
        if (!isValidDate(dateFinField.getValue())) {
            errors.append("- Date de fin invalide (jj/mm/aaaa)\n");
        }
        if (isValidDate(dateDebutField.getValue()) && isValidDate(dateFinField.getValue())) {
            LocalDate debut = dateDebutField.getValue();
            LocalDate fin = dateFinField.getValue();
            if (fin.isBefore(debut)) {
                errors.append("- La date de fin doit être postérieure ou égale à la date de début\n");
            }
        }
        if (nombreHeuresField.getText().trim().isEmpty()) {
            errors.append("- Nombre d'heures obligatoire\n");
        } else {
            BigDecimal nh = parseBigDecimal(nombreHeuresField.getText());
            if (nh == null || nh.compareTo(BigDecimal.ZERO) < 0) {
                errors.append("- Nombre d'heures doit être positif ou nul\n");
            }
        }
        if (tauxField.getText().trim().isEmpty()) {
            errors.append("- Taux obligatoire\n");
        } else {
            BigDecimal t = parseBigDecimal(tauxField.getText());
            if (t == null || t.compareTo(BigDecimal.ZERO) < 0) {
                errors.append("- Taux doit être positif ou nul\n");
            }
        }
        if (irCombo.getSelectionModel().getSelectedItem() == null) {
            errors.append("- Taux IR obligatoire\n");
        }

        if (errors.length() > 0) {
            showError("Erreurs de validation :\n" + errors.toString());
            return false;
        }
        return true;
    }

    private boolean isValidDate(LocalDate date) {
        return date != null;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}