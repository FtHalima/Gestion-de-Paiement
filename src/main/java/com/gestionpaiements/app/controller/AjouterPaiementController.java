package com.gestionpaiements.app.controller;

import com.gestionpaiements.app.model.Professeur;
import com.gestionpaiements.app.model.TypePaiement;
import com.gestionpaiements.app.model.Paiement;
import com.gestionpaiements.app.service.ProfesseurService;
import com.gestionpaiements.app.service.PaiementService;
import com.gestionpaiements.app.service.PdfGenerationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gestionpaiements.app.service.ExcelFileManagerService;

@Component
public class AjouterPaiementController {

    @Autowired
    private ProfesseurService professeurService;

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private PdfGenerationService pdfGenerationService;

    @Autowired
    private ExcelFileManagerService excelFileManagerService;

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
    @FXML private Label ribCompletLabel;
    @FXML private Button deleteProfButton;
    @FXML private Button pdfButton;
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

    // New fields for Exercice, Code CGNC, Article, Par, Lig
    @FXML private TextField exerciceField;
    @FXML private TextField codeCgncField;
    @FXML private TextField articleField;
    @FXML private TextField parField;
    @FXML private TextField ligField;

    // Buttons
    @FXML private Button cancelButton;
    @FXML private Button resetButton;
    @FXML private Button saveButton;

    // State
    private Professeur currentProfesseur; // null if not yet searched or not found
    private boolean isNewProfessorMode = false;

    // Formatters
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Mapping of grade -> list of echelle per occurrence (to support duplicates in ComboBox)
    private final Map<String, List<Integer>> gradeEchelleMap = new HashMap<>();
    // Parallel list: echelle for each index in gradeField items
    private final List<Integer> echelleForIndex = new ArrayList<>();

    @FXML
    public void initialize() {
        // Populate comboboxes
        typePaiementCombo.setItems(FXCollections.observableArrayList(TypePaiement.values()));
        irCombo.setItems(FXCollections.observableArrayList("30", "34", "37"));
        irCombo.getSelectionModel().selectFirst(); // default 30%
        irCombo.setEditable(false);
        tauxField.setEditable(true);

        // Grade ComboBox with duplicates as requested
        ObservableList<String> grades = FXCollections.observableArrayList(
                "Professeur d'Enseignement Superieur",
                "Professeur Encadrant",
                "Professeur Qualifié",
                "Professeur Adjoint",
                "Inspecteur",
                "Personnel d'Enseignement",
                "Professeur Agrégé");
        gradeField.setItems(grades);

        // Build echelle mapping based on occurrence
        Map<String, Integer> occurrence = new HashMap<>();
        for (String g : grades) {
            occurrence.put(g, occurrence.getOrDefault(g, 0) + 1);
            int occ = occurrence.get(g);
            int echelle = echelleForGradeAndOccurrence(g, occ);
            gradeEchelleMap.computeIfAbsent(g, k -> new ArrayList<>()).add(echelle);
            echelleForIndex.add(echelle);
        }

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
        gradeField.valueProperty().addListener((obs, oldVal, newVal) -> handleGradeChange());
        echelleField.textProperty().addListener((obs, oldVal, newVal) -> handleEchelleChange());

        // Listener for typePaiement to update objet and reference labels
        typePaiementCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateObjetEtReference());

        // Listener for date validation
        dateDebutField.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());
        dateFinField.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());

        // Initial calculation to display correct amounts based on default values
        calculate();
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

        System.out.println("RECHERCHE PROFESSEUR : cin='" + cin + "', ppr='" + ppr + "'");
        Optional<Professeur> opt = professeurService.rechercherParCinOuPpr(cin, ppr);
        if (opt.isPresent()) {
            currentProfesseur = opt.get();
            System.out.println("PROFESSEUR TROUVÉ : id=" + currentProfesseur.getIdProfesseur()
                    + ", ribBanque=" + currentProfesseur.getRibBanque()
                    + ", ribVille=" + currentProfesseur.getRibVille()
                    + ", ribNumeroCompte=" + currentProfesseur.getRibNumeroCompte()
                    + ", ribCle=" + currentProfesseur.getRibCle()
                    + ", ribComplet=" + currentProfesseur.getRibComplet());
            isNewProfessorMode = false;
            // Load professor data into edit fields
            loadProfessorIntoEditFields(currentProfesseur);
            // Load latest paiement for this professor
            Optional<Paiement> latestPaiement = paiementService.trouverDernierPaiementParProfesseur(currentProfesseur);
            if (latestPaiement.isPresent()) {
                Paiement p = latestPaiement.get();
                typePaiementCombo.setValue(p.getTypePaiement());
                dateDebutField.setValue(p.getDateDebut());
                dateFinField.setValue(p.getDateFin());
                nombreHeuresField.setText(formatBigDecimal(p.getNombreHeures()));
                tauxField.setText(formatBigDecimal(p.getTaux()));
                String irStr = p.getTauxIr().stripTrailingZeros().toPlainString();
                irCombo.getSelectionModel().select(irStr);
                // Set new payment fields
                exerciceField.setText(p.getExercice() != null ? p.getExercice() : "");
                codeCgncField.setText(p.getCodeCgnc() != null ? p.getCodeCgnc() : "");
                articleField.setText(p.getArticle() != null ? p.getArticle() : "");
                parField.setText(p.getPar() != null ? p.getPar() : "");
                ligField.setText(p.getLig() != null ? p.getLig() : "");
                // Update calculated labels
                calculate();
            } else {
                // No previous paiement: clear payment fields
                typePaiementCombo.getSelectionModel().selectFirst();
                dateDebutField.setValue(null);
                dateFinField.setValue(null);
                nombreHeuresField.clear();
                tauxField.clear();
                irCombo.getSelectionModel().clearSelection();
                // Clear new fields
                exerciceField.clear();
                codeCgncField.clear();
                articleField.clear();
                parField.clear();
                ligField.clear();
                calculate(); // will set zero
            }
            // Show edit fields (allow editing)
            hideProfessorDisplay();
            showProfessorEdit();
            hideProfessorNotFound();
        } else {
            currentProfesseur = null;
            isNewProfessorMode = true;
            hideProfessorDisplay();
            hideProfessorEdit();
            showProfessorNotFound();
            // Pre-fill CIN and PPR already in fields; ready for creation
            System.out.println("PROFESSEUR NON TROUVÉ");
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
        // Clear payment fields
        typePaiementCombo.getSelectionModel().clearSelection();
        dateDebutField.setValue(null);
        dateFinField.setValue(null);
        nombreHeuresField.clear();
        tauxField.clear();
        irCombo.getSelectionModel().clearSelection();
        // Clear RIB fields
        banqueField.clear();
        villeField.clear();
        numeroCompteField.clear();
        cleField.clear();
        // Clear new fields
        exerciceField.clear();
        codeCgncField.clear();
        articleField.clear();
        parField.clear();
        ligField.clear();
        // Focus on nomField
        nomField.requestFocus();
        // Reset amount labels to zero
        calculate();
    }

    // UI helpers
    private void showProfesseurInfo(Professeur prof) {
        nomLabel.setText(prof.getNom());
        prenomLabel.setText(prof.getPrenom());
        ddrLabel.setText(prof.getDdr() != null ? prof.getDdr().format(dateFormatter) : "-");
        gradeLabel.setText(prof.getGrade());
        echelleLabel.setText(prof.getEchelle() != null ? prof.getEchelle().toString() : "-");
        affectationLabel.setText(prof.getAffectation());
        ribCompletLabel.setText(prof.getRibComplet());
    }

    private void loadProfessorIntoEditFields(Professeur prof) {
        // Set CIN and PPR (though they are already in search fields)
        cinField.setText(prof.getCin());
        pprField.setText(prof.getPpr());
        nomField.setText(prof.getNom());
        prenomField.setText(prof.getPrenom());
        ddrPicker.setValue(prof.getDdr());
        gradeField.setValue(prof.getGrade());
        echelleField.setText(prof.getEchelle() != null ? prof.getEchelle().toString() : "");
        affectationField.setText(prof.getAffectation());
        banqueField.setText(prof.getRibBanque());
        villeField.setText(prof.getRibVille());
        numeroCompteField.setText(prof.getRibNumeroCompte());
        cleField.setText(prof.getRibCle());
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

    // Grade change -> set echelle based on occurrence and compute taux/IR
    private void handleGradeChange() {
        String grade = gradeField.getValue();
        if (grade == null || grade.isEmpty()) {
            echelleField.clear();
            tauxField.clear();
            irCombo.getSelectionModel().clearSelection();
            return;
        }
        int idx = gradeField.getSelectionModel().getSelectedIndex();
        Integer echelle = echelleForIndex.get(idx);
        echelleField.setText(String.valueOf(echelle));
        computeTauxEtIR(grade, String.valueOf(echelle));
    }

    // Echelle change -> compute taux/IR based on current echelle (do not override echelleField)
    private void handleEchelleChange() {
        String grade = gradeField.getValue();
        String echelleStr = echelleField.getText().trim();
        if (grade == null || grade.isEmpty() || echelleStr.isEmpty()) {
            tauxField.clear();
            irCombo.getSelectionModel().clearSelection();
            return;
        }
        computeTauxEtIR(grade, echelleStr);
    }

    // Compute taux and IR based on grade and echelle via service
    private void computeTauxEtIR(String grade, String echelleStr) {
        Optional<ProfesseurService.TauxIR> opt = professeurService.trouverTauxEtIRParGradeEtEchelle(grade, echelleStr);
        if (opt.isPresent()) {
            ProfesseurService.TauxIR tirs = opt.get();
            tauxField.setText(tirs.getTaux().stripTrailingZeros().toPlainString());
            irCombo.getSelectionModel().select(tirs.getTauxIr().stripTrailingZeros().toPlainString());
        } else {
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
                objetReglementLabel.setText("VACATION");
                referenceReglementLabel.setText("VACATION");
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

    @FXML
    private void saveAction() {
        if (!validateForm()) {
            return;
        }

        // Prepare professeur
        Professeur professeurToSave;
        if (currentProfesseur != null && !isNewProfessorMode) {
            // Update existing professor with form values
            currentProfesseur.setCin(cinField.getText().trim());
            currentProfesseur.setPpr(pprField.getText().trim());
            currentProfesseur.setNom(nomField.getText().trim());
            currentProfesseur.setPrenom(prenomField.getText().trim());
            currentProfesseur.setDdr(ddrPicker.getValue());
            currentProfesseur.setGrade(gradeField.getValue());
            currentProfesseur.setEchelle(echelleField.getText().trim().isEmpty() ? null : Integer.valueOf(echelleField.getText().trim()));
            currentProfesseur.setAffectation(affectationField.getText().trim());
            currentProfesseur.setRibBanque(banqueField.getText());
            currentProfesseur.setRibVille(villeField.getText());
            currentProfesseur.setRibNumeroCompte(numeroCompteField.getText());
            currentProfesseur.setRibCle(cleField.getText());
            professeurToSave = currentProfesseur;
        } else {
            professeurToSave = new Professeur();
            professeurToSave.setCin(cinField.getText().trim());
            professeurToSave.setPpr(pprField.getText().trim());
            professeurToSave.setNom(nomField.getText().trim());
            professeurToSave.setPrenom(prenomField.getText().trim());
            professeurToSave.setDdr(ddrPicker.getValue());
            professeurToSave.setGrade(gradeField.getValue());
            professeurToSave.setEchelle(echelleField.getText().trim().isEmpty() ? null : Integer.valueOf(echelleField.getText().trim()));
            professeurToSave.setAffectation(affectationField.getText().trim());
            // Set RIB fields from the form
            professeurToSave.setRibBanque(banqueField.getText());
            professeurToSave.setRibVille(villeField.getText());
            professeurToSave.setRibNumeroCompte(numeroCompteField.getText());
            professeurToSave.setRibCle(cleField.getText());
        }

        // Log RIB before saving professor
        System.out.println("AVANT ENREGISTREMENT PROFESSEUR : ribBanque=" + professeurToSave.getRibBanque()
                + ", ribVille=" + professeurToSave.getRibVille()
                + ", ribNumeroCompte=" + professeurToSave.getRibNumeroCompte()
                + ", ribCle=" + professeurToSave.getRibCle());

        // Save professor (insert if new, update if existing)
        Professeur savedProf = professeurService.sauver(professeurToSave);

        // Log after saving professor
        System.out.println("APRÈS ENREGISTREMENT PROFESSEUR : idProfesseur=" + savedProf.getIdProfesseur()
                + ", ribBanque=" + savedProf.getRibBanque()
                + ", ribVille=" + savedProf.getRibVille()
                + ", ribNumeroCompte=" + savedProf.getRibNumeroCompte()
                + ", ribCle=" + savedProf.getRibCle());

        // Create paiement with saved professor
        Paiement paiement = new Paiement();
        paiement.setProfesseur(savedProf);
        paiement.setTypePaiement(typePaiementCombo.getSelectionModel().getSelectedItem());
        // Objet et référence règlement : convert "-" to null for cleaner DB
        String objetReglement = objetReglementLabel.getText();
        paiement.setObjetReglement("-".equals(objetReglement) || objetReglement.isEmpty() ? null : objetReglement);
        String referenceReglement = referenceReglementLabel.getText();
        paiement.setReferenceReglement("-".equals(referenceReglement) || referenceReglement.isEmpty() ? null : referenceReglement);
        paiement.setDateDebut(dateDebutField.getValue());
        paiement.setDateFin(dateFinField.getValue());
        paiement.setNombreHeures(parseBigDecimal(nombreHeuresField.getText()));
        paiement.setTaux(parseBigDecimal(tauxField.getText()));
        paiement.setTauxIr(parseBigDecimal(irCombo.getSelectionModel().getSelectedItem()));
        paiement.setModePaiement(modePaiementLabel.getText());
        paiement.setTypeReferenceReglement(typeReferenceReglementLabel.getText());
        paiement.setDatePaiement(datePaiementField.getValue());
        // Set new fields
        paiement.setExercice(exerciceField.getText().trim());
        paiement.setCodeCgnc(codeCgncField.getText().trim());
        paiement.setArticle(articleField.getText().trim());
        paiement.setPar(parField.getText().trim());
        paiement.setLig(ligField.getText().trim());

        // Log AVANT CONTROLLER
        System.out.println("AVANT CONTROLLER : taux=" + paiement.getTaux() + ", tauxIr=" + paiement.getTauxIr());

        // Save via service (this will persist paiement and compute amounts)
        Paiement savedPaiement = paiementService.enregistrerPaiement(paiement);

        // Optional: log final paiement ID
        System.out.println("PAIEMENT ENREGISTRÉ : idPaiement=" + savedPaiement.getIdPaiement());

        // Ajout automatique dans le fichier Excel actif
        try {
            excelFileManagerService.appendPaiement(savedPaiement);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Paiement enregistré en base, mais erreur lors de l'écriture dans Excel : " + e.getMessage());
        }

        // Show success
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Paiement enregistré");
        successAlert.setHeaderText(null);
        successAlert.setContentText("Paiement enregistré avec succès. ID: " + savedPaiement.getIdPaiement());
        successAlert.showAndWait();

        // Optionally reset form
        resetAction();
    }

    @FXML
    private void handleDeleteProf() {
        if (currentProfesseur != null && !isNewProfessorMode) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation de suppression");
            confirm.setHeaderText("Supprimer le professeur ?");
            confirm.setContentText("Voulez-vous vraiment supprimer le professeur " + currentProfesseur.getNom() + " " + currentProfesseur.getPrenom() + " ?");
            ButtonType ok = new ButtonType("OUI");
            ButtonType cancel = new ButtonType("NON", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(ok, cancel);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ok) {
                try {
                    professeurService.supprimerProfesseurAvecPaiements(currentProfesseur.getIdProfesseur());
                    // reset form to initial state
                    resetAction();
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Succès");
                    info.setHeaderText(null);
                    info.setContentText("Professeur et ses paiements associés supprimés avec succès.");
                    info.showAndWait();
                } catch (Exception e) {
                    showError("Erreur lors de la suppression : " + e.getMessage());
                }
            }
        } else {
            showError("Aucun professeur à supprimer ou vous êtes en mode création.");
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
        montantBrutLabel.setText("0,00 ");
        retenueIrLabel.setText("0,00 ");
        montantNetLabel.setText("0,00 ");
        modePaiementLabel.setText("VIREMENT");
        typeReferenceReglementLabel.setText("RIB");
        datePaiementField.setValue(null);
        // Reset new fields
        exerciceField.clear();
        codeCgncField.clear();
        articleField.clear();
        parField.clear();
        ligField.clear();
    }

    @FXML
    private void genererPdfAction(ActionEvent event) {
        try {
            // Validate required fields
            StringBuilder errors = new StringBuilder();

            // CIN/PPR required
            if (cinField.getText().trim().isEmpty()) {
                errors.append("- CIN obligatoire\n");
            }
            if (pprField.getText().trim().isEmpty()) {
                errors.append("- PPR obligatoire\n");
            }

            // If in new professor mode, validate professor fields
            if (isNewProfessorMode) {
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
                }
                if (affectationField.getText().trim().isEmpty()) {
                    errors.append("- Affectation obligatoire\n");
                }

                // Validate RIB fields (all or nothing)
                String banque = banqueField.getText().trim();
                String ville = villeField.getText().trim();
                String compte = numeroCompteField.getText().trim();
                String cle = cleField.getText().trim();

                boolean ribFieldsEmpty = banque.isEmpty() && ville.isEmpty() && compte.isEmpty() && cle.isEmpty();
                boolean ribFieldsFull = banque.length() == 3 && ville.length() == 3 && compte.length() == 16 && cle.length() == 2;

                if (!ribFieldsEmpty && !ribFieldsFull) {
                    errors.append("- RIB incomplet: Banque (3), Ville (3), Compte (16), Clé (2)\n");
                }
            }

            if (errors.length() > 0) {
                showError("Veuillez corriger les erreurs suivantes :\n" + errors.toString());
                return;
            }

            // Additional validation for payment fields
            if (typePaiementCombo.getSelectionModel().getSelectedItem() == null) {
                showError("Veuillez sélectionner un type de paiement");
                return;
            }

            if (dateDebutField.getValue() == null) {
                showError("Veuillez saisir une date de début");
                return;
            }

            if (dateFinField.getValue() == null) {
                showError("Veuillez saisir une date de fin");
                return;
            }

            if (dateFinField.getValue().isBefore(dateDebutField.getValue())) {
                showError("La date de fin doit être postérieure ou égale à la date de début");
                return;
            }

            try {
                BigDecimal nombreHeures = parseBigDecimal(nombreHeuresField.getText());
                if (nombreHeures == null || nombreHeures.compareTo(BigDecimal.ZERO) <= 0) {
                    showError("Veuillez saisir un nombre d'heures valide (supérieur à zéro)");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Veuillez saisir un nombre d'heures valide");
                return;
            }

            try {
                BigDecimal taux = parseBigDecimal(tauxField.getText());
                if (taux == null || taux.compareTo(BigDecimal.ZERO) < 0) {
                    showError("Veuillez saisir un taux valide (supérieur ou égal à zéro)");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Veuillez saisir un taux valide");
                return;
            }

            // Save professor (insert if new, update if existing)
            Professeur professeurToSave;
            if (currentProfesseur != null && !isNewProfessorMode) {
                // Update existing professor
                currentProfesseur.setNom(nomField.getText().trim());
                currentProfesseur.setPrenom(prenomField.getText().trim());
                currentProfesseur.setDdr(ddrPicker.getValue());
                currentProfesseur.setGrade(gradeField.getValue());
                currentProfesseur.setEchelle(echelleField.getText().trim().isEmpty() ? null : Integer.valueOf(echelleField.getText().trim()));
                currentProfesseur.setAffectation(affectationField.getText().trim());
                currentProfesseur.setRibBanque(banqueField.getText());
                currentProfesseur.setRibVille(villeField.getText());
                currentProfesseur.setRibNumeroCompte(numeroCompteField.getText());
                currentProfesseur.setRibCle(cleField.getText());
                professeurToSave = currentProfesseur;
            } else {
                // Create new professor
                professeurToSave = new Professeur();
                professeurToSave.setCin(cinField.getText().trim());
                professeurToSave.setPpr(pprField.getText().trim());
                professeurToSave.setNom(nomField.getText().trim());
                professeurToSave.setPrenom(prenomField.getText().trim());
                professeurToSave.setDdr(ddrPicker.getValue());
                professeurToSave.setGrade(gradeField.getValue());
                professeurToSave.setEchelle(echelleField.getText().trim().isEmpty() ? null : Integer.valueOf(echelleField.getText().trim()));
                professeurToSave.setAffectation(affectationField.getText().trim());
                professeurToSave.setRibBanque(banqueField.getText());
                professeurToSave.setRibVille(villeField.getText());
                professeurToSave.setRibNumeroCompte(numeroCompteField.getText());
                professeurToSave.setRibCle(cleField.getText());
            }

            // Log RIB before saving professor
            System.out.println("AVANT ENREGISTREMENT PROFESSEUR (PDF) : ribBanque=" + professeurToSave.getRibBanque()
                    + ", ribVille=" + professeurToSave.getRibVille()
                    + ", ribNumeroCompte=" + professeurToSave.getRibNumeroCompte()
                    + ", ribCle=" + professeurToSave.getRibCle());

            // Save professor (insert if new, update if existing)
            Professeur savedProf = professeurService.sauver(professeurToSave);

            // Log after saving professor
            System.out.println("APRÈS ENREGISTREMENT PROFESSEUR (PDF) : idProfesseur=" + savedProf.getIdProfesseur()
                    + ", ribBanque=" + savedProf.getRibBanque()
                    + ", ribVille=" + savedProf.getRibVille()
                    + ", ribNumeroCompte=" + savedProf.getRibNumeroCompte()
                    + ", ribCle=" + savedProf.getRibCle());

            // Create paiement with saved professor
            Paiement paiement = new Paiement();
            paiement.setProfesseur(savedProf);
            paiement.setTypePaiement(typePaiementCombo.getSelectionModel().getSelectedItem());
            // Objet et référence règlement : convert "-" to null for cleaner DB
            String objetReglement = objetReglementLabel.getText();
            paiement.setObjetReglement("-".equals(objetReglement) || objetReglement.isEmpty() ? null : objetReglement);
            String referenceReglement = referenceReglementLabel.getText();
            paiement.setReferenceReglement("-".equals(referenceReglement) || referenceReglement.isEmpty() ? null : referenceReglement);
            paiement.setDateDebut(dateDebutField.getValue());
            paiement.setDateFin(dateFinField.getValue());
            paiement.setNombreHeures(parseBigDecimal(nombreHeuresField.getText()));
            paiement.setTaux(parseBigDecimal(tauxField.getText()));
            paiement.setTauxIr(parseBigDecimal(irCombo.getSelectionModel().getSelectedItem()));
            paiement.setModePaiement(modePaiementLabel.getText());
            paiement.setTypeReferenceReglement(typeReferenceReglementLabel.getText());
            paiement.setDatePaiement(datePaiementField.getValue());
            // Set new fields
            paiement.setExercice(exerciceField.getText().trim());
            paiement.setCodeCgnc(codeCgncField.getText().trim());
            paiement.setArticle(articleField.getText().trim());
            paiement.setPar(parField.getText().trim());
            paiement.setLig(ligField.getText().trim());

            // Log AVANT CONTROLLER
            System.out.println("AVANT CONTROLLER (PDF) : taux=" + paiement.getTaux() + ", tauxIr=" + paiement.getTauxIr());

            // Save via service (this will persist paiement and compute amounts)
            Paiement savedPaiement = paiementService.enregistrerPaiement(paiement);

            // Optional: log final paiement ID
            System.out.println("PAIEMENT ENREGISTRÉ (PDF) : idPaiement=" + savedPaiement.getIdPaiement());

            // Ajout automatique dans le fichier Excel actif
            try {
                excelFileManagerService.appendPaiement(savedPaiement);
            } catch (Exception e) {
                e.printStackTrace();
                showError("Paiement enregistré en base, mais erreur lors de l'écriture dans Excel : " + e.getMessage());
            }

            // Generate PDF
            ByteArrayInputStream pdfInputStream = pdfGenerationService.genererPdfEstadoSums(savedPaiement.getIdPaiement());

            // Save PDF to file using FileChooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName("etat_sommes_dues_" + savedPaiement.getIdPaiement() + ".pdf");

            File file = fileChooser.showSaveDialog(
                    ((Button) event.getSource()).getScene().getWindow());

            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = pdfInputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }

                    showInfo("PDF généré avec succès et enregistré dans : " + file.getAbsolutePath());
                } catch (IOException ex) {
                    showError("Erreur lors de l'écriture du fichier PDF : " + ex.getMessage());
                }
            } else {
                showInfo("Génération du PDF annulée par l'utilisateur");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la génération du PDF : " + e.getMessage());
        }
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
        String objetReglement = objetReglementLabel.getText();
        if (objetReglement.equals("-") || objetReglement.isEmpty()) {
            errors.append("- Objet du règlement non déterminé\n");
        }
        String referenceReglement = referenceReglementLabel.getText();
        if (referenceReglement.equals("-") || referenceReglement.isEmpty()) {
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Returns the echelle for a given grade and occurrence number (1-based).
     * Occurrence corresponds to the position of the duplicate in the ComboBox list.
     */
    private int echelleForGradeAndOccurrence(String grade, int occurrence) {
        switch (grade) {
            case "Professeur d'Enseignement Superieur":
                return 12;
            case "Professeur Encadrant":
                return 12;
            case "Professeur Qualifié":
                return 12;
            case "Professeur Adjoint":
                return 12;
            case "Inspecteur":
                return occurrence == 1 ? 12 : 11;
            case "Personnel d'Enseignement":
                if (occurrence == 1) return 12;
                if (occurrence == 2) return 11;
                return 10; // occurrence == 3
            case "Professeur Agrégé":
                return 12;
            default:
                return 12; // fallback
        }
    }
}