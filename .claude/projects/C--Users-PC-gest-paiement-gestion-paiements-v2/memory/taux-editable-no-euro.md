---
name: taux-editable-no-euro
description: Rendre le champ Taux éditable et supprimer le symbole € des montants brut, retenue et net
metadata:
  type: project
---
# Modifications : champ Taux éditable et retrait du symbole €

## Changements effectués

1. **Champ Taux éditable**  
   - Dans `initialize()` du `AjouterPaiementController`, la ligne `tauxField.setEditable(false);` a été remplacée par `tauxField.setEditable(true);`.  
   - Cela permet à l'utilisateur de saisir manuellement un taux horaire, tout en conservant la mise à jour automatique via le service lorsque le grade ou l'échelle changent.

2. **Suppression du symbole € des montants**  
   - Dans la méthode `calculate()` : les lignes qui formattaient les montants avec `+ " €"` ont été modifiées pour n'afficher que la valeur formatée (via `formatBigDecimal`).  
   - Dans la méthode `resetAction()` : les libellés `montantBrutLabel`, `retenueIrLabel` et `montantNetLabel` sont désormais remis à `"0,00"` (sans le symbole €).  
   - Aucun autre ajout du symbole € n'est présent dans le contrôleur.

## Fichiers modifiés
- `src/main/java/com/gestionpaiements/app/controller/AjouterPaiementController.java` :  
  * Ligne 99 : `tauxField.setEditable(true);`  
  * Lignes 372‑374 : suppression du `+ " €"`  
  * Lignes 489‑491 : remise à zéro sans `+ " €"`

## Résultat
- Le projet compile sans erreur (`mvn compile` → BUILD SUCCESS).  
- L'utilisateur peut maintenant éditer le champ Taux.  
- Les montants affichés n'incluent plus le symbole €.

Cette modification répond directement à la demande : *« je ne peux pas ecrire dans le taux + supprimer l'icone d'euro dans montant net, retenut et brut »*.