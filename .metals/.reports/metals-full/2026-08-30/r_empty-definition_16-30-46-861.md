error id: file:///C:/Users/PC/gest-paiement/gestion-paiements-v2/src/main/java/com/gestionpaiements/app/controller/AjouterPaiementController.java:_empty_/Paiement#getCreanceDOrigine#
file:///C:/Users/PC/gest-paiement/gestion-paiements-v2/src/main/java/com/gestionpaiements/app/controller/AjouterPaiementController.java
empty definition using pc, found symbol in pc: _empty_/Paiement#getCreanceDOrigine#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 13802
uri: file:///C:/Users/PC/gest-paiement/gestion-paiements-v2/src/main/java/com/gestionpaiements/app/controller/AjouterPaiementController.java
text:
```scala
package com.gestionpaiements.app.controller;

import com.gestionpaiements.app.model.Professeur;
import com.gestionpaiements.app.model.TypePaiement;
import com.gestionpaiements.app.model.Paiement;
import com.gestionpaiements.app.service.ProfesseurService;
import com.gestionpaiements.app.service.PaiementService;
import com.gestionpaiements.app.service.PdfGenerationService;
import com.gestionpaiements.app.service.DeplacementPdfGenerationService;
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

import com.gestionpaiements.app.service.DeplacementPdfGenerationService;
import com.gestionpaiements.app.service.ExcelFileManagerService;

import java.time.LocalTime;

import javafx.scene.control.cell.TextFieldTableCell;
import javafx.collections.ObservableList;
import com.gestionpaiements.app.model.LigneDeplacement;

import javafx.collections.FXCollections;

import java.math.RoundingMode;

import com.gestionpaiements.app.service.DeplacementPdfGenerationService;

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

    @Autowired
    private DeplacementPdfGenerationService deplacementPdfGenerationService;

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
    @FXML private TextField creanceDOrigineField;
    @FXML private TextField codeCgncField;
    @FXML private TextField articleField;
    @FXML private TextField parField;
    @FXML private TextField ligField;

    // Buttons
    @FXML private Button cancelButton;
    @FXML private Button resetButton;
    @FXML private Button saveButton;

        //deplacement 
    @FXML private TitledPane deplacementSection;
    @FXML private TextField motifDeplacementField;

    @FXML private Label nombreHeuresTitleLabel;
    @FXML private Label tauxTitleLabel;
    @FXML private Label irTitleLabel;
    @FXML private Label montantBrutTitleLabel;
    @FXML private Label retenueIrTitleLabel;
    @FXML private Label montantNetTitleLabel;

    @FXML private TitledPane calculSection;
    @FXML private TableView<LigneDeplacementUI> trajetsTable;
    @FXML private TableColumn<LigneDeplacementUI, String> colDateDepart;
    @FXML private TableColumn<LigneDeplacementUI, String> colDateArrivee;
    @FXML private TableColumn<LigneDeplacementUI, String> colParcours;
    @FXML private TableColumn<LigneDeplacementUI, String> colHeureDepart;
    @FXML private TableColumn<LigneDeplacementUI, String> colHeureRetour;
    @FXML private TableColumn<LigneDeplacementUI, String> colNombreTaux;
    @FXML private TableColumn<LigneDeplacementUI, String> colTauxApplique;
    @FXML private TableColumn<LigneDeplacementUI, String> colMontantLigne;
    @FXML private Button addTrajetButton;
    @FXML private Button removeTrajetButton;
    @FXML private Label totalDeplacementLabel;

    // State
    private Professeur currentProfesseur; // null if not yet searched or not found
    private boolean isNewProfessorMode = false;
    private Paiement currentPaiement; // paiement existant en cours de modification, null si nouveau paiement

    // Formatters
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm"); // format interne de secours
    private final DateTimeFormatter heureDisplayFormatter = DateTimeFormatter.ofPattern("H'H'"); // format affiché : 8H, 23H

    // Mapping of grade -> list of echelle per occurrence (to support duplicates in ComboBox)
    private final Map<String, List<Integer>> gradeEchelleMap = new HashMap<>();
    // Parallel list: echelle for each index in gradeField items
    private final List<Integer> echelleForIndex = new ArrayList<>();

    //deplacement
    private final ObservableList<LigneDeplacementUI> trajetsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Populate comboboxes
        typePaiementCombo.setItems(FXCollections.observableArrayList(TypePaiement.values()));
        typePaiementCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateFormForType(newVal));
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
        //deplacement
        setupTrajetsTable();
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
                currentPaiement = p; // ✅ on garde la référence pour faire un UPDATE à l'enregistrement
                typePaiementCombo.setValue(p.getTypePaiement());
                dateDebutField.setValue(p.getDateDebut());
                dateFinField.setValue(p.getDateFin());
                nombreHeuresField.setText(formatBigDecimal(p.getNombreHeures()));
                tauxField.setText(formatBigDecimal(p.getTaux()));
                String irStr = p.getTauxIr().stripTrailingZeros().toPlainString();
                irCombo.getSelectionModel().select(irStr);
                // Set new payment fields
                motifDeplacementField.setText(p.getMotifDeplacement() != null ? p.getMotifDeplacement() : "");
                trajetsData.clear();
                if (p.getTypePaiement() == TypePaiement.DEPLACEMENT) {
                    for (LigneDeplacement l : p.getLignesDeplacement()) {
                        LigneDeplacementUI row = new LigneDeplacementUI();
                        row.setDateDepart(l.getDateDepart() != null ? l.getDateDepart().format(dateFormatter) : "");
                        row.setDateArrivee(l.getDateArrivee() != null ? l.getDateArrivee().format(dateFormatter) : "");
                        row.setParcours(l.getParcours() != null ? l.getParcours() : "");
                        row.setHeureDepart(l.getHeureDepart() != null ? l.getHeureDepart().format(heureDisplayFormatter) : "");
                        row.setHeureRetour(l.getHeureRetour() != null ? l.getHeureRetour().format(heureDisplayFormatter) : "");
                        row.setNombreTauxBase(l.getNombreTauxBase() != null ? l.getNombreTauxBase().stripTrailingZeros().toPlainString() : "");
                        row.setTauxBaseApplique(l.getTauxBaseApplique() != null ? l.getTauxBaseApplique().stripTrailingZeros().toPlainString() : "");
                        row.setMontant(l.getMontant() != null ? formatBigDecimal(l.getMontant()) : "0,00");
                        trajetsData.add(row);
                    }
                }
                recalcTotalDeplacement();

                exerciceField.setText(p.getExercice() != null ? p.getExercice() : "");
                creanceDOrigineField.setText(p.getCreanceDOrigine() != null ? p.@@getCreanceDOrigine() : "");
                codeCgncField.setText(p.getCodeCgnc() != null ? p.getCodeCgnc() : "");
                articleField.setText(p.getArticle() != null ? p.getArticle() : "");
                parField.setText(p.getPar() != null ? p.getPar() : "");
                ligField.setText(p.getLig() != null ? p.getLig() : "");
                // Update calculated labels
                calculate();
            } else {
                currentPaiement = null; // ✅ pas de paiement existant -> le prochain enregistrement sera un nouveau paiement
                // No previous paiement: clear payment fields
                typePaiementCombo.getSelectionModel().selectFirst();
                dateDebutField.setValue(null);
                dateFinField.setValue(null);
                nombreHeuresField.clear();
                tauxField.clear();
                irCombo.getSelectionModel().clearSelection();
                // Clear new fields
                motifDeplacementField.clear();
                trajetsData.clear();
                recalcTotalDeplacement();

                exerciceField.clear();
                creanceDOrigineField.clear();
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
        currentPaiement = null; // ✅ nouveau professeur => toujours un nouveau paiement
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

        motifDeplacementField.clear();
        trajetsData.clear();
        recalcTotalDeplacement();
        
        exerciceField.clear();
        creanceDOrigineField.clear();
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

        private void setupTrajetsTable() {
        colDateDepart.setCellValueFactory(d -> d.getValue().dateDepartProperty());
        colDateArrivee.setCellValueFactory(d -> d.getValue().dateArriveeProperty());
        colParcours.setCellValueFactory(d -> d.getValue().parcoursProperty());
        colHeureDepart.setCellValueFactory(d -> d.getValue().heureDepartProperty());
        colHeureRetour.setCellValueFactory(d -> d.getValue().heureRetourProperty());
        colNombreTaux.setCellValueFactory(d -> d.getValue().nombreTauxBaseProperty());
        colTauxApplique.setCellValueFactory(d -> d.getValue().tauxBaseAppliqueProperty());
        colMontantLigne.setCellValueFactory(d -> d.getValue().montantProperty());

        colDateDepart.setCellFactory(createEditableCell());
        colDateArrivee.setCellFactory(createEditableCell());
        colParcours.setCellFactory(createEditableCell());
        colHeureDepart.setCellFactory(createEditableCell());
        colHeureRetour.setCellFactory(createEditableCell());
        colNombreTaux.setCellFactory(createEditableCell());
        colTauxApplique.setCellFactory(createEditableCell());
        // colMontantLigne reste en lecture seule (pas de setCellFactory éditable)

        colDateDepart.setOnEditCommit(e -> e.getRowValue().setDateDepart(e.getNewValue()));
        colDateArrivee.setOnEditCommit(e -> e.getRowValue().setDateArrivee(e.getNewValue()));
        colParcours.setOnEditCommit(e -> e.getRowValue().setParcours(e.getNewValue()));
        colHeureDepart.setOnEditCommit(e -> e.getRowValue().setHeureDepart(e.getNewValue()));
        colHeureRetour.setOnEditCommit(e -> e.getRowValue().setHeureRetour(e.getNewValue()));
        colNombreTaux.setOnEditCommit(e -> {
            e.getRowValue().setNombreTauxBase(e.getNewValue());
            recalcLigneMontant(e.getRowValue());
        });
        colTauxApplique.setOnEditCommit(e -> {
            e.getRowValue().setTauxBaseApplique(e.getNewValue());
            recalcLigneMontant(e.getRowValue());
        });

        trajetsTable.setItems(trajetsData);
    }
    //
    private javafx.util.Callback<TableColumn<LigneDeplacementUI, String>, TableCell<LigneDeplacementUI, String>> createEditableCell() {
        return column -> new TableCell<LigneDeplacementUI, String>() {
            private final TextField textField = new TextField();

            {
                textField.setOnAction(e -> commitEdit(textField.getText()));
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        commitEdit(textField.getText());
                    }
                });
            }

            @Override
            public void startEdit() {
                super.startEdit();
                textField.setText(getItem() == null ? "" : getItem());
                setGraphic(textField);
                setText(null);
                textField.requestFocus();
                textField.selectAll();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);
                setText(newValue);
                setGraphic(null);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (isEditing()) {
                    textField.setText(item);
                    setGraphic(textField);
                    setText(null);
                } else {
                    setText(item);
                    setGraphic(null);
                }
            }
        };
    }

    private void recalcLigneMontant(LigneDeplacementUI row) {
        BigDecimal nombre = parseBigDecimal(row.getNombreTauxBase());
        BigDecimal taux = parseBigDecimal(row.getTauxBaseApplique());
        BigDecimal montant = (nombre != null && taux != null)
                ? nombre.multiply(taux).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        row.setMontant(formatBigDecimal(montant));
        recalcTotalDeplacement();
    }

    private void recalcTotalDeplacement() {
        BigDecimal total = BigDecimal.ZERO;
        for (LigneDeplacementUI row : trajetsData) {
            BigDecimal m = parseBigDecimal(row.getMontant());
            if (m != null) total = total.add(m);
        }
        totalDeplacementLabel.setText("Total : " + formatBigDecimal(total) + " DH");
    }

    @FXML
    private void handleAddTrajet() {
        trajetsData.add(new LigneDeplacementUI());
    }

    @FXML
    private void handleRemoveTrajet() {
        LigneDeplacementUI selected = trajetsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            trajetsData.remove(selected);
            recalcTotalDeplacement();
        }
    }

    private LocalDate parseLocalDate(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(text.trim(), dateFormatter);
        } catch (Exception e) {
            return null;
        }
    }

    // Grade change -> set echelle based on occurrence and compute taux/IR
    private void handleGradeChange() {
        String grade = gradeField.getValue();
        boolean isDeplacement = typePaiementCombo.getSelectionModel().getSelectedItem() == TypePaiement.DEPLACEMENT;
        if (grade == null || grade.isEmpty()) {
            echelleField.clear();
            if (!isDeplacement) {
                tauxField.clear();
                irCombo.getSelectionModel().clearSelection();
            }
            return;
        }
        int idx = gradeField.getSelectionModel().getSelectedIndex();
        Integer echelle = echelleForIndex.get(idx);
        echelleField.setText(String.valueOf(echelle));
        if (!isDeplacement) {
            computeTauxEtIR(grade, String.valueOf(echelle));
        }
    }

    // Echelle change -> compute taux/IR based on current echelle (do not override echelleField)
    private void handleEchelleChange() {
        String grade = gradeField.getValue();
        String echelleStr = echelleField.getText().trim();
        boolean isDeplacement = typePaiementCombo.getSelectionModel().getSelectedItem() == TypePaiement.DEPLACEMENT;
        if (grade == null || grade.isEmpty() || echelleStr.isEmpty()) {
            if (!isDeplacement) {
                tauxField.clear();
                irCombo.getSelectionModel().clearSelection();
            }
            return;
        }
        if (!isDeplacement) {
            computeTauxEtIR(grade, echelleStr);
        }
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

    private void updateFormForType(TypePaiement type) {
        boolean isDeplacement = (type == TypePaiement.DEPLACEMENT);

        deplacementSection.setVisible(isDeplacement);
        deplacementSection.setManaged(isDeplacement);

        calculSection.setVisible(!isDeplacement);
        calculSection.setManaged(!isDeplacement);

        irTitleLabel.setVisible(!isDeplacement);
        irTitleLabel.setManaged(!isDeplacement);
        irCombo.setVisible(!isDeplacement);
        irCombo.setManaged(!isDeplacement);

        retenueIrTitleLabel.setVisible(!isDeplacement);
        retenueIrTitleLabel.setManaged(!isDeplacement);
        retenueIrLabel.setVisible(!isDeplacement);
        retenueIrLabel.setManaged(!isDeplacement);

        montantNetTitleLabel.setVisible(!isDeplacement);
        montantNetTitleLabel.setManaged(!isDeplacement);
        montantNetLabel.setVisible(!isDeplacement);
        montantNetLabel.setManaged(!isDeplacement);

        nombreHeuresTitleLabel.setText(isDeplacement ? "Nombre de taux de base :" : "Nombre d'heures :");
        tauxTitleLabel.setText(isDeplacement ? "Taux de base appliqué :" : "Taux :");
        montantBrutTitleLabel.setText(isDeplacement ? "Montant :" : "Montant brut :");

        tauxField.setEditable(isDeplacement);
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

    private LocalTime parseLocalTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String raw = text.trim();

        // Format principal attendu : "8H", "23H", "08H" (heure seule, minutes = 00)
        if (raw.matches("(?i)\\d{1,2}\\s*h")) {
            int hour = Integer.parseInt(raw.replaceAll("(?i)h", "").trim());
            if (hour >= 0 && hour <= 23) {
                return LocalTime.of(hour, 0);
            }
            return null;
        }

        // Formats de secours (anciennes saisies au format HH:mm, 8h00, 0800, etc.)
        String cleaned = raw.replace("h", ":").replace("H", ":");
        if (cleaned.matches("\\d{3,4}")) {
            if (cleaned.length() == 3) cleaned = "0" + cleaned;
            cleaned = cleaned.substring(0, 2) + ":" + cleaned.substring(2);
        }
        if (cleaned.matches("\\d:\\d{2}")) {
            cleaned = "0" + cleaned;
        }
        try {
            return LocalTime.parse(cleaned, timeFormatter);
        } catch (Exception e) {
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
        // Create or update paiement with saved professor
        Paiement paiement = (currentPaiement != null) ? currentPaiement : new Paiement();
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
        paiement.setCreanceDOrigine(creanceDOrigineField.getText().trim());
        paiement.setCodeCgnc(codeCgncField.getText().trim());
        paiement.setArticle(articleField.getText().trim());
        paiement.setPar(parField.getText().trim());
        paiement.setLig(ligField.getText().trim());

        paiement.setMotifDeplacement(motifDeplacementField.getText().trim());

        if (paiement.getTypePaiement() == TypePaiement.DEPLACEMENT) {
            paiement.getLignesDeplacement().clear();
            BigDecimal total = BigDecimal.ZERO;
            for (LigneDeplacementUI row : trajetsData) {
                LigneDeplacement ligne = new LigneDeplacement();
                ligne.setPaiement(paiement);
                ligne.setDateDepart(parseLocalDate(row.getDateDepart()));
                ligne.setDateArrivee(parseLocalDate(row.getDateArrivee()));
                ligne.setParcours(row.getParcours());
                ligne.setHeureDepart(parseLocalTime(row.getHeureDepart()));
                ligne.setHeureRetour(parseLocalTime(row.getHeureRetour()));
                BigDecimal nombre = parseBigDecimal(row.getNombreTauxBase());
                BigDecimal tauxLigne = parseBigDecimal(row.getTauxBaseApplique());
                BigDecimal montantLigne = (nombre != null && tauxLigne != null)
                        ? nombre.multiply(tauxLigne).setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                ligne.setNombreTauxBase(nombre);
                ligne.setTauxBaseApplique(tauxLigne);
                ligne.setMontant(montantLigne);
                paiement.getLignesDeplacement().add(ligne);
                total = total.add(montantLigne);
            }
            paiement.setMontantBrut(total);
            paiement.setRetenueIr(BigDecimal.ZERO);
            paiement.setMontantNet(total);
        }

        // Log AVANT CONTROLLER
        System.out.println("AVANT CONTROLLER : taux=" + paiement.getTaux() + ", tauxIr=" + paiement.getTauxIr());

        // Save via service (this will persist paiement and compute amounts)
        Paiement savedPaiement = paiementService.enregistrerPaiement(paiement);
        currentPaiement = savedPaiement; // ✅ garde la référence à jour pour un éventuel prochain enregistrement

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
        currentPaiement = null;

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
        motifDeplacementField.clear();
        trajetsData.clear();
        recalcTotalDeplacement();
        exerciceField.clear();
        creanceDOrigineField.clear();
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
//
            boolean isDeplacementType = typePaiementCombo.getSelectionModel().getSelectedItem() == TypePaiement.DEPLACEMENT;
            
            if (!isDeplacementType) {
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
            }
            //

            

            if (!isDeplacementType) {
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
            } else {
                if (motifDeplacementField.getText().trim().isEmpty()) {
                    showError("Veuillez saisir le motif de déplacement");
                    return;
                }
                if (trajetsData.isEmpty()) {
                    showError("Veuillez ajouter au moins un trajet");
                    return;
                }
            }
//
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
            // Create or update paiement with saved professor
            Paiement paiement = (currentPaiement != null) ? currentPaiement : new Paiement();
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
            paiement.setCreanceDOrigine(creanceDOrigineField.getText().trim());
        
            paiement.setCodeCgnc(codeCgncField.getText().trim());
            paiement.setArticle(articleField.getText().trim());
            paiement.setPar(parField.getText().trim());
            paiement.setLig(ligField.getText().trim());

            paiement.setMotifDeplacement(motifDeplacementField.getText().trim());

            if (paiement.getTypePaiement() == TypePaiement.DEPLACEMENT) {
                paiement.getLignesDeplacement().clear();
                BigDecimal total = BigDecimal.ZERO;
                for (LigneDeplacementUI row : trajetsData) {
                    LigneDeplacement ligne = new LigneDeplacement();
                    ligne.setPaiement(paiement);
                    ligne.setDateDepart(parseLocalDate(row.getDateDepart()));
                    ligne.setDateArrivee(parseLocalDate(row.getDateArrivee()));
                    ligne.setParcours(row.getParcours());
                    ligne.setHeureDepart(parseLocalTime(row.getHeureDepart()));
                    ligne.setHeureRetour(parseLocalTime(row.getHeureRetour()));
                    BigDecimal nombre = parseBigDecimal(row.getNombreTauxBase());
                    BigDecimal tauxLigne = parseBigDecimal(row.getTauxBaseApplique());
                    BigDecimal montantLigne = (nombre != null && tauxLigne != null)
                            ? nombre.multiply(tauxLigne).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    ligne.setNombreTauxBase(nombre);
                    ligne.setTauxBaseApplique(tauxLigne);
                    ligne.setMontant(montantLigne);
                    paiement.getLignesDeplacement().add(ligne);
                    total = total.add(montantLigne);
                }
                paiement.setMontantBrut(total);
                paiement.setRetenueIr(BigDecimal.ZERO);
                paiement.setMontantNet(total);
            }

            // Log AVANT CONTROLLER
            System.out.println("AVANT CONTROLLER (PDF) : taux=" + paiement.getTaux() + ", tauxIr=" + paiement.getTauxIr());

            // Save via service (this will persist paiement and compute amounts)
            Paiement savedPaiement = paiementService.enregistrerPaiement(paiement);
            currentPaiement = savedPaiement; // ✅ garde la référence à jour

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
            // Generate PDF (format dédié pour Déplacement, format standard pour les autres types)
            ByteArrayInputStream pdfInputStream = (savedPaiement.getTypePaiement() == TypePaiement.DEPLACEMENT)
                    ? deplacementPdfGenerationService.genererPdfDeplacement(savedPaiement.getIdPaiement())
                    : pdfGenerationService.genererPdfEstadoSums(savedPaiement.getIdPaiement());

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
        resetAction();

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
        //
        boolean isDeplacementType = typePaiementCombo.getSelectionModel().getSelectedItem() == TypePaiement.DEPLACEMENT;

        if (!isDeplacementType) {
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
        }
        
        if (typePaiementCombo.getSelectionModel().getSelectedItem() == TypePaiement.DEPLACEMENT) {
            if (motifDeplacementField.getText().trim().isEmpty()) {
                errors.append("- Motif de déplacement obligatoire\n");
            }
            if (trajetsData.isEmpty()) {
                errors.append("- Au moins un trajet doit être ajouté\n");
            } else {
                int rowNum = 1;
                for (LigneDeplacementUI row : trajetsData) {
                   if (!row.getHeureDepart().isEmpty() && parseLocalTime(row.getHeureDepart()) == null) {
                        errors.append("- Trajet " + rowNum + " : heure de départ invalide (ex: 8H)\n");
                    }
                    if (!row.getHeureRetour().isEmpty() && parseLocalTime(row.getHeureRetour()) == null) {
                        errors.append("- Trajet " + rowNum + " : heure de retour invalide (ex: 18H)\n");
                    }
                    rowNum++;
                }
            }
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
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Paiement#getCreanceDOrigine#