# Fixes Applied

## 1. Fixed Hibernate "not-null property references a null or transient value" error
- **Root Cause**: The `Paiement` entity has non-null `@ManyToOne` references to `Professeur`, `Utilisateur`, and `Lot`. When saving a paiement, the `Utilisateur` (connected user) and `Lot` (active lot) were not being set, causing null references.
- **Fix**: 
  - In `PaiementService.enregistrerPaiement()`:
    - Set `paiement.setUtilisateur(sessionUtilisateur.getUtilisateurConnecte())`
    - Set `paiement.setLot(lotService.getOuCreerLotActif(utilisateur))`
  - The method is `@Transactional`, ensuring that if paiement persistence fails, any professor created is rolled back.
- **Files Modified**:
  - `src/main/java/com/gestionpaiements/app/service/PaiementService.java` (added `LotService` dependency, updated `enregistrerPaiement` method)
  - `src/main/java/com/gestionpaiements/app/controller/AjouterPaiementController.java` (added `LotService` and `Utilisateur` imports, set utilisateur and lot in `saveAction`)

## 2. Fixed NullPointerException in `AjouterPaiementController.calculate()`
- **Root Cause**: Controller field named `retenirIrLabel` (missing 'e') while FXML declared `fx:id="retenueIrLabel"`.
- **Fix**:
  - Changed controller field declaration from `@FXML private Label retenirIrLabel;` to `@FXML private Label retenueIrLabel;`
  - Updated usages in `calculate()` (line 420) and `resetAction()` (line 575) to use `retenueIrLabel`
- **Files Modified**:
  - `src/main/java/com/gestionpaiements/app/controller/AjouterPaiementController.java`

## Verification
- Project compiles successfully: `mvn compile` → BUILD SUCCESS
- FXML field names match controller fields (verified `fx:id="retenueIrLabel"` in `AjouterPaiement.fxml`)

## 3. Changed "Date de naissance" to "Date de recrutement"
- **Root Cause**: The label for the date of recruitment field incorrectly displayed "Date de naissance (DDR)".
- **Fix**: Updated the label text in `AjouterPaiement.fxml` to "Date de recrutement (DDR)" (lines 59 and 81).
- **Files Modified**:
  - `src/main/resources/com/gestionpaiements/app/fxml/AjouterPaiement.fxml`