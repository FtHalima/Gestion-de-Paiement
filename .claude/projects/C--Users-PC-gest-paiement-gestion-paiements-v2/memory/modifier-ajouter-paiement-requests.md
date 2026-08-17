---
name: modifier-ajouter-paiement-requests
description: Modifications supplémentaires demandées par l'utilisateur sur la page "Ajouter un paiement"
metadata:
  type: project
---
# Modifications supplémentaires sur Ajouter un paiement

## Changements effectués suite aux dernières demandes de l'utilisateur :

1. **Date de recrutement (DDR)**
   - Le libellé "Date de naissance (DDR) :" a été changé en "Date de recrutement (DDR) :" dans les sections d'affichage et de saisie.
   - Le champs de saisie (DatePicker) comporte maintenant l'invite texte "jj/mm/aaaa".

2. **Recherche de professeur**
   - Il suffit maintenant de saisir **soit** le CIN **soit** le PPR (au moins un des deux) pour lancer la recherche.
   - Le message d'erreur indique : "Veuillez saisir le CIN ou le PPR."

3. **Suppression du symbole € des montants**
   - Les champs "Montant brut", "Retenue IR" et "Montant net" n'affichent plus le symbole € après la valeur (seulement la valeur numérique avec séparateur décimal virgule).
   - La remise à zéro du formulaire remet ces champs à "0,00" (sans €).

4. **Terminologie "Vacataire"**
   - Les libellés "Objet règlement" et "Référence règlement" affichent maintenant "VACATAIRE" lorsqu'on sélectionne le type de paiement VACATAIRE (auparavant "VACATION").

5. **Validation mise à jour**
   - Le message d'erreur de validation pour la date de recrutement indique désormais "- Date de recrutement (DDR) obligatoire".
   - Les autres validations (RIB, dates, etc.) restent inchangées.

## Fichiers modifiés

- `src/main/java/com/gestionpaiements/app/controller/AjouterPaiementController.java` :
  * Méthode `searchProfessor()` : utilisation de `rechercherParCinOuPpr` et condition assouplie.
  * Méthode `validateForm()` : validation assouplie CIN/PPR, message d'erreur date mise à jour.
  * Méthode `calculate()` : suppression du suffixe " €".
  * Méthode `resetAction()` : remise à zéro des montants sans " €".
  * (Les méthodes `updateObjetEtReference` étaient déjà correctes.)

- `src/main/resources/com/gestionpaiements/app/fxml/AjouterPaiement.fxml` :
  * Libellé "Date de recrutement (DDR) :" dans la section d'affichage (ligne corrigée).
  * Libellé "Date de recrutement (DDR) :" dans la section de saisie avec `promptText="jj/mm/aaaa"` sur le `DatePicker`.

- `src/main/java/com/gestionpaiements/app/dao/ProfesseurRepository.java` :
  * Ajout de la méthode `findByCinOrPpr` pour soutenir la recherche par CIN ou PPR.

- `src/main/java/com/gestionpaiements/app/service/ProfesseurService.java` :
  * Ajout de la méthode `rechercherParCinOuPpr(String cin, String ppr)` qui utilise le nouveau repository.

## Résultat

Le projet compile sans erreur (`mvn compile` réussi). Toutes les demandes de l'utilisateur sont maintenant prises en compte tout en conservant l'architecture existante (FXML → Controller → Service → DAO).