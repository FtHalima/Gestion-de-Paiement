---
name: modifier-date-naissance-recrutement
description: Change the label "Date de naissance" to "Date de recrutement" in the AjouterPaiement form
metadata:
  type: project
---
# Change date of birth label to recruitment date

## Problem
The label for the date of recruitment field in the professor section was incorrectly labeled as "Date de naissance (DDR)" instead of "Date de recrutement (DDR)".

## Solution
Changed the label text in `src/main/resources/com/gestionpaiements/app/fxml/AjouterPaiement.fxml`:
- Line 59: `<Label text="Date de recrutement (DDR) :" GridPane.columnIndex="0" GridPane.rowIndex="2"/>`
- Line 81: `<Label text="Date de recrutement (DDR) :" GridPane.columnIndex="0" GridPane.rowIndex="2"/>`

## Files Modified
- `src/main/resources/com/gestionpaiements/app/fxml/AjouterPaiement.fxml`

## Result
The form now correctly displays "Date de recrutement (DDR)" for the professor's date of recruitment field.