package com.gestionpaiements.app.controller;

import com.gestionpaiements.app.model.Professeur;
import com.gestionpaiements.app.model.TypePaiement;
import com.gestionpaiements.app.service.ProfesseurService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class AjouterPaiementController {

    @Autowired
    private ProfesseurService professeurService;

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
    @FXML private TextField gradeField;
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
    @FXML private TextField objetReglementField;
    @FXML private TextField dateDebutField;
    @FXML private TextField dateFinField;
    @FXML private TextField nombreHeuresField;
    @FXML private TextField tauxField;
    @FXML private ComboBox<String> irCombo;
    @FXML private Label montantBrutLabel;
    @FXML private Label retenuIrLabel;
    @FXML private Label montantNetLabel;
    @FXML private Label modePaiementLabel;
    @FXML private TextField typeReferenceReglementField;
    @FXML private TextField referenceReglementField;
    @FXML private TextField datePaiementField;

    // Buttons
    @FXML private Button cancelButton;
    @FXML private Button resetButton;
    @FXML private Button saveButton;

    // State
    private Professeur currentProfesseur; // null if not yet searched or not found
    private boolean isNewProfessorMode = false;

    // Formatters
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final BigDecimal HUNDRED = new BigDecimal("100");

    @FXML
    public void initialize() {
        // Populate comboboxes
        typePaiementCombo.setItems(FXCollections.observableArrayList(TypePaiement.values()));
        irCombo.setItems(FXCollections.observableArrayList("30", "34", "37"));
        irCombo.getSelectionModel().selectFirst(); // default 30%

        // Set default date paiement to today
        datePaiementField.setText(LocalDate.now().format(dateFormatter));

        // Setup auto-focus for RIB fields
        setupRibAutoFocus();

        // Setup calculation listeners
        nombreHeuresField.textProperty().addListener((obs, oldVal, newVal) -> calculate());
        tauxField.textProperty().addListener((obs, oldVal, newVal) -> calculate());
        irCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> calculate());

        // Enter key to trigger search
        cinField.setOnKeyPressed(this::handleEnterKey);
        pprField.setOnKeyPressed(this::handleEnterKey);
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
        if (cin.isEmpty() || ppr.isEmpty()) {
            showError("Veuillez saisir le CIN et le PPR.");
            return;
        }

        Optional<Professeur> opt = professeurService.rechercherParCinPpr(cin, ppr);
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
        gradeField.clear();
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

    // Calculation
    private void calculate() {
        BigDecimal nombreHeures = parseBigDecimal(nombreHeuresField.getText());
        BigDecimal taux = parseBigDecimal(tauxField.getText());
        String irStr = irCombo.getSelectionModel().getSelectedItem();
        BigDecimal tauxIr = irStr != null ? new BigDecimal(irStr) : BigDecimal.ZERO;

        if (nombreHeures == null || taux == null) {
            clearAmounts();
            return;
        }

        BigDecimal montantBrut = nombreHeures.multiply(taux).setScale(2, RoundingMode.HALF_UP);
        BigDecimal retenuIr = montantBrut.multiply(tauxIr).divide(HUNDRED, 10, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal montantNet = montantBrut.subtract(retenuIr).setScale(2, RoundingMode.HALF_UP);

        montantBrutLabel.setText(formatBigDecimal(montantBrut) + " €");
        retenuIrLabel.setText(formatBigDecimal(retenuIr) + " €");
        montantNetLabel.setText(formatBigDecimal(montantNet) + " €");
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

    private void clearAmounts() {
        montantBrutLabel.setText("0,00 €");
        retenuIrLabel.setText("0,00 €");
        montantNetLabel.setText("0,00 €");
    }

    // Validation and save
    @FXML
    private void saveAction() {
        if (!validateForm()) {
            return;
        }
        // Build summary message
        StringBuilder summary = new StringBuilder("Récapitulatif du paiement :\n\n");
        if (currentProfesseur != null && !isNewProfessorMode) {
            summary.append("Professeur trouvé :\n")
                    .append("CIN: ").append(currentProfesseur.getCin()).append("\n")
                    .append("PPR: ").append(currentProfesseur.getPpr()).append("\n")
                    .append("Nom: ").append(currentProfesseur.getNom()).append("\n")
                    .append("Prénom: ").append(currentProfesseur.getPrenom()).append("\n\n");
        } else if (isNewProfessorMode) {
            summary.append("Nouveau professeur à créer :\n")
                    .append("CIN: ").append(cinField.getText().trim()).append("\n")
                    .append("PPR: ").append(pprField.getText().trim()).append("\n")
                    .append("Nom: ").append(nomField.getText().trim()).append("\n")
                    .append("Prénom: ").append(prenomField.getText().trim()).append("\n")
                    .append("DDR: ").append(ddrPicker.getValue() != null ? ddrPicker.getValue().format(dateFormatter) : "-").append("\n")
                    .append("Grade: ").append(gradeField.getText().trim()).append("\n")
                    .append("Échelle: ").append(echelleField.getText().trim()).append("\n")
                    .append("Affectation: ").append(affectationField.getText().trim()).append("\n\n");
        }

        summary.append("RIB: ")
                .append(banqueField.getText()).append(" ")
                .append(villeField.getText()).append(" ")
                .append(numeroCompteField.getText()).append(" ")
                .append(cleField.getText()).append("\n\n");

        summary.append("Type de paiement: ").append(typePaiementCombo.getSelectionModel().getSelectedItem()).append("\n")
                .append("Objet règlement: ").append(objetReglementField.getText().trim()).append("\n")
                .append("Date début: ").append(dateDebutField.getText().trim()).append("\n")
                .append("Date fin: ").append(dateFinField.getText().trim()).append("\n\n");

        summary.append("Nombre d'heures: ").append(nombreHeuresField.getText().trim()).append("\n")
                .append("Taux (€/h): ").append(tauxField.getText().trim()).append("\n")
                .append("IR %: ").append(irCombo.getSelectionModel().getSelectedItem()).append("\n")
                .append("Montant brut: ").append(montantBrutLabel.getText()).append("\n")
                .append("Retenue IR: ").append(retenuIrLabel.getText()).append("\n")
                .append("Montant net: ").append(montantNetLabel.getText()).append("\n\n");

        summary.append("Mode de paiement: ").append(modePaiementLabel.getText()).append("\n")
                .append("Type référence règlement: ").append(typeReferenceReglementField.getText().trim()).append("\n")
                .append("Référence: ").append(referenceReglementField.getText().trim()).append("\n")
                .append("Date paiement: ").append(datePaiementField.getText().trim());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Récapitulatif paiement");
        alert.setHeaderText(null);
        alert.setContentText(summary.toString());
        alert.showAndWait();
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
        objetReglementField.clear();
        dateDebutField.clear();
        dateFinField.clear();
        nombreHeuresField.clear();
        tauxField.clear();
        irCombo.getSelectionModel().selectFirst();
        clearAmounts();
        modePaiementLabel.setText("VIREMENT");
        typeReferenceReglementField.clear();
        referenceReglementField.clear();
        datePaiementField.setText(LocalDate.now().format(dateFormatter));
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

        // CIN/PPR required (but if we are in new professor mode we still need them)
        if (cinField.getText().trim().isEmpty()) {
            errors.append("- CIN obligatoire\n");
        }
        if (pprField.getText().trim().isEmpty()) {
            errors.append("- PPR obligatoire\n");
        }

        if (isNewProfessorMode) {
            if (nomField.getText().trim().isEmpty()) {
                errors.append("- Nom du professeur obligatoire\n");
            }
            if (prenomField.getText().trim().isEmpty()) {
                errors.append("- Prénom du professeur obligatoire\n");
            }
            if (ddrPicker.getValue() == null) {
                errors.append("- Date de naissance (DDR) obligatoire\n");
            }
            if (gradeField.getText().trim().isEmpty()) {
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

        // RIB length validation
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

        // Paiement info
        if (typePaiementCombo.getSelectionModel().getSelectedItem() == null) {
            errors.append("- Type de paiement obligatoire\n");
        }
        if (objetReglementField.getText().trim().isEmpty()) {
            errors.append("- Objet du règlement obligatoire\n");
        }
        if (!isValidDate(dateDebutField.getText())) {
            errors.append("- Date de début invalide (jj/mm/aaaa)\n");
        }
        if (!isValidDate(dateFinField.getText())) {
            errors.append("- Date de fin invalide (jj/mm/aaaa)\n");
        }
        if (isValidDate(dateDebutField.getText()) && isValidDate(dateFinField.getText())) {
            LocalDate debut = LocalDate.parse(dateDebutField.getText(), dateFormatter);
            LocalDate fin = LocalDate.parse(dateFinField.getText(), dateFormatter);
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

    private boolean isValidDate(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        try {
            LocalDate.parse(text, dateFormatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}