# Gestion des Paiements — Application Desktop

Application desktop (JavaFX + Spring Boot) permettant de gérer les paiements des
professeurs pour trois catégories : **Vacataire**, **Heure Supplémentaire** et
**Déplacement**. L'application génère des documents PDF officiels, exporte les
données au format Excel selon une structure administrative précise, et permet
la gestion complète des professeurs et de leurs paiements. Elle est distribuée
sous forme d'un **exécutable Windows autonome** (`.exe`), sans dépendance
externe à installer.

---

## Sommaire

1. [Aperçu général](#aperçu-général)
2. [Fonctionnalités](#fonctionnalités)
3. [Architecture technique](#architecture-technique)
4. [Structure du projet](#structure-du-projet)
5. [Installation et lancement (développement)](#installation-et-lancement-développement)
6. [Configuration de la base de données](#configuration-de-la-base-de-données)
7. [Empaquetage en exécutable autonome (.exe)](#empaquetage-en-exécutable-autonome-exe)
8. [Fonctionnement des fichiers Excel](#fonctionnement-des-fichiers-excel)
9. [Génération des documents PDF](#génération-des-documents-pdf)
10. [Import de professeurs](#import-de-professeurs)
11. [Limitations connues](#limitations-connues)
12. [Pistes d'amélioration](#pistes-daméliorstion)

---

## Aperçu général

L'application s'adresse à un établissement (par exemple un centre de formation)
qui doit gérer le paiement de trois types d'indemnités pour ses professeurs :

- **Vacataire** — paiement calculé sur un nombre d'heures et un taux horaire,
  avec retenue IR (impôt sur le revenu).
- **Heure Supplémentaire** — même logique de calcul que Vacataire.
- **Déplacement** — paiement composé de **plusieurs trajets** (un même
  paiement peut regrouper plusieurs dates de déplacement), sans retenue IR,
  avec un formulaire et un PDF entièrement dédiés.

Chaque paiement est associé à un professeur (identifié par CIN et PPR), et
génère automatiquement une ligne dans un fichier Excel actif propre à son
type, ainsi qu'un document PDF officiel imprimable.

L'application fonctionne **entièrement en local** : chaque installation
dispose de sa propre base de données embarquée, sans nécessiter de serveur
de base de données séparé ni de connexion réseau.

---

## Fonctionnalités

### Authentification
- Écran de connexion (login / mot de passe) avant l'accès à l'application.
- Un compte administrateur par défaut est créé automatiquement au tout
  premier lancement (voir [Configuration de la base de données](#configuration-de-la-base-de-données)).

### Tableau de bord
- Statistiques en temps réel : nombre de paiements par type + total.
- Liste des derniers enregistrements (nom, CIN, type avec badge coloré,
  montant net).
- Bouton d'import de professeurs (voir section dédiée).

### Formulaire "Ajouter un paiement"
- Recherche d'un professeur existant par CIN ou PPR.
- Création d'un nouveau professeur si non trouvé, avec sélection du grade
  dans une liste extensible (bouton **+** pour ajouter un grade personnalisé
  à la volée).
- Calcul automatique du taux et du taux IR selon le grade et l'échelle
  (pour Vacataire / Heure Sup).
- Formulaire dynamique selon le type de paiement sélectionné :
  - **Vacataire / Heure Sup** : nombre d'heures, taux, IR, calcul du montant
    brut / retenue / net.
  - **Déplacement** : tableau éditable de trajets (dates via calendrier,
    heures au format `8H`/`23H` avec ajout automatique du "H"), avec calcul
    du montant total en temps réel.
- Informations bancaires (RIB : Banque / Ville / Numéro de compte / Clé).
- Génération de PDF officiel.
- Modification en place : rechercher un professeur puis modifier un
  paiement existant du **même type** met à jour l'enregistrement (pas de
  doublon) ; changer de type crée un nouveau paiement distinct (un
  professeur peut avoir un paiement actif par type simultanément).
- Suppression du professeur (et de ses paiements associés) depuis le
  formulaire.

### Pages Vacataire / Heure Sup / Déplacement
Chaque page liste les **fichiers Excel** (actifs et archivés) associés à ce
type de paiement, plutôt qu'une liste brute de paiements en base. Pour
chaque fichier, il est possible de :
- **Visualiser** son contenu (aperçu des professeurs qu'il contient).
- **Modifier** une ligne → ouvre directement le formulaire d'ajout de
  paiement, pré-rempli avec le CIN sélectionné.
- **Supprimer une ligne** → supprime la ligne à la fois du fichier Excel et
  de la base de données.
- **Supprimer le fichier entier** → supprime uniquement le fichier Excel
  (la base de données n'est pas affectée par cette action).
- **Renommer** le fichier.
- **Clôturer** un fichier actif (l'archiver, avec un nom personnalisable, et
  en créer un nouveau automatiquement).
- **Réactiver** un fichier archivé (il redevient le fichier actif ; l'ancien
  fichier actif est automatiquement archivé pour ne rien perdre).
- **Exporter** une copie du fichier vers un emplacement choisi.

### Génération PDF
- **Vacataire / Heure Sup** : document "État des sommes dues" standard,
  avec en-tête administratif, informations du professeur, tableau
  d'opérations, calcul du montant net à payer et signatures.
- **Déplacement** : document dédié reproduisant un modèle administratif
  spécifique, avec tableau multi-trajets (dates, parcours, heures, taux,
  montants), total général, et mentions légales.

### Import de données
- Import en masse de professeurs depuis un fichier Excel (CIN, PPR, Nom,
  Prénom, Grade, Échelle, RIB) — n'importe aucun paiement, uniquement les
  fiches professeur.

---

## Architecture technique

| Composant | Technologie |
|---|---|
| Interface graphique | JavaFX (FXML + CSS) |
| Backend / logique métier | Spring Boot |
| Persistance | Spring Data JPA (Hibernate) |
| Base de données | H2 (base de données embarquée, fichier local) |
| Génération PDF | iText 7 |
| Lecture/écriture Excel | Apache POI |
| Sécurité mot de passe | jBCrypt (hachage) |
| Build | Maven |
| Empaquetage | `jpackage` (JDK 21) |

L'application combine Spring Boot et JavaFX dans un seul processus : Spring
Boot démarre le contexte applicatif (services, repositories, base de
données), et JavaFX gère l'interface. Les contrôleurs JavaFX sont instanciés
par Spring (via `FXMLLoader.setControllerFactory(...)`), ce qui permet
l'injection de dépendances (`@Autowired`) dans les contrôleurs FXML.

### Interface à une seule fenêtre
L'application fonctionne comme un tableau de bord classique : une barre
latérale (sidebar) fixe à gauche, une zone de contenu centrale qui change
dynamiquement selon la page sélectionnée (`StackPane` dans `MainView.fxml`),
sans ouverture de fenêtres séparées pour chaque fonctionnalité.

---

## Structure du projet

```
src/main/java/com/gestionpaiements/app/
├── controller/          # Contrôleurs JavaFX (un par vue FXML)
│   ├── LoginController.java
│   ├── MainViewController.java
│   ├── DashboardController.java
│   ├── AjouterPaiementController.java
│   ├── ExcelFilesController.java
│   ├── LigneDeplacementUI.java       # Modèle d'édition pour le tableau de trajets
│   ├── ArchivesController.java       # Ancien contrôleur, conservé mais non utilisé
│   └── PaiementListController.java   # Ancien contrôleur, conservé mais non utilisé
├── model/               # Entités JPA
│   ├── Professeur.java
│   ├── Paiement.java
│   ├── LigneDeplacement.java        # Trajet individuel d'un paiement Déplacement
│   ├── TypePaiement.java            # Enum : VACATAIRE, HEURE_SUP, DEPLACEMENT
│   ├── Lot.java
│   └── Utilisateur.java
├── service/             # Logique métier
│   ├── PaiementService.java
│   ├── ProfesseurService.java
│   ├── UtilisateurService.java
│   ├── PdfGenerationService.java             # PDF Vacataire / Heure Sup
│   ├── DeplacementPdfGenerationService.java  # PDF Déplacement (format dédié)
│   ├── ExcelFileManagerService.java          # Gestion des fichiers Excel
│   ├── SessionUtilisateur.java
│   └── LotService.java
├── dao/                 # Repositories Spring Data JPA
└── MainApp.java          # Point d'entrée (Application JavaFX + Spring Boot)

src/main/resources/com/gestionpaiements/app/fxml/
├── login.fxml
├── MainView.fxml
├── DashboardView.fxml
├── AjouterPaiement.fxml
├── ExcelFilesView.fxml
├── ArchivesView.fxml     # Ancienne vue, conservée mais non liée à la navigation
├── PaiementList.fxml     # Ancienne vue, conservée mais non liée à la navigation
└── dashboard.css         # Feuille de style unique pour toute l'application
```

> Les fichiers `ArchivesController`/`ArchivesView.fxml` et
> `PaiementListController`/`PaiementList.fxml` correspondent à une version
> antérieure de la navigation (page "Archives" séparée, liste de paiements
> brute). Ils ont été conservés dans le projet à la demande explicite, mais
> ne sont plus référencés depuis `MainViewController` — leurs fonctionnalités
> ont été intégrées dans `ExcelFilesController`/`ExcelFilesView.fxml`.

---

## Installation et lancement (développement)

### Prérequis
- Java 21 (JDK Eclipse Temurin recommandé)
- Maven

> Aucune base de données externe à installer : H2 est embarquée et se
> configure automatiquement au premier lancement.

### Lancer en développement
```bash
mvn clean spring-boot:run
```

### Compiler uniquement
```bash
mvn clean compile
```

---

## Configuration de la base de données

La base de données utilisée est **H2**, en mode fichier, configurée dans
`src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:h2:file:${user.home}/GestionPaiements/data/gestion_paiements;AUTO_SERVER=TRUE
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

Le fichier de base de données est stocké dans le dossier personnel de
l'utilisateur (`C:\Users\<utilisateur>\GestionPaiements\data\`), afin d'éviter
tout problème de droits d'écriture si l'application est installée dans un
dossier protégé (ex. `C:\Program Files\`).

### Compte par défaut
Au tout premier lancement (base vide), un compte administrateur est créé
automatiquement par `MainApp.java` :

- **Login** : `admin`
- **Mot de passe** : `admin123`

Ce compte n'est créé qu'une seule fois (l'application vérifie qu'aucun
utilisateur n'existe déjà avant de le créer). Il n'existe actuellement pas
de fonctionnalité de changement de mot de passe en libre-service dans
l'application — une modification du mot de passe nécessiterait une
intervention directe en base de données ou l'ajout futur de cette
fonctionnalité (voir [Pistes d'amélioration](#pistes-daméliorstion)).

---

## Empaquetage en exécutable autonome (.exe)

L'application est distribuée sous forme d'un installeur Windows autonome,
généré avec `jpackage` (outil intégré au JDK 21). Le JDK est embarqué dans
l'installeur, donc **aucune installation de Java n'est requise** sur les
postes cibles.

### Étapes de génération

1. Construire le jar exécutable :
   ```bash
   mvn clean package
   ```

2. Générer l'installeur (depuis la racine du projet, avec `JAVA_HOME` pointant
   vers un JDK 21 et WiX Toolset v3 installé pour la génération `.exe`) :
   ```bash
   "%JAVA_HOME%\bin\jpackage" --type exe --name "Gestion Paiements" --input target --main-jar gestion-paiements-v2-0.0.1-SNAPSHOT.jar --icon icon.ico --runtime-image "%JAVA_HOME%" --dest dist --win-shortcut --win-menu --win-dir-chooser --app-version 1.0.0 --vendor "TonNomOuSociete"
   ```

3. L'installeur généré se trouve dans `dist\Gestion Paiements-1.0.0.exe`.

### Comportement à l'installation
- L'utilisateur choisit le dossier d'installation (`--win-dir-chooser`).
- Un raccourci bureau et une entrée dans le menu Démarrer sont créés.
- Au premier lancement, l'application crée automatiquement :
  - Le dossier et le fichier de base de données H2 (`GestionPaiements/data/`).
  - Le dossier des fichiers Excel (`GestionPaiements/excel/`).
  - Le compte administrateur par défaut.

Ce fonctionnement a été validé sur plusieurs machines, y compris des PC
n'ayant jamais eu Java ni MySQL installés au préalable.

---

## Fonctionnement des fichiers Excel

### Structure d'un fichier
Chaque fichier Excel généré par l'application suit une structure fixe à 4
lignes d'en-tête :
- **Ligne 1** : en-tête administratif fixe (Acteur Dépense, Type Dépense,
  etc.), jamais rempli.
- **Lignes 2-3** : vides.
- **Ligne 4** : en-têtes des 20 colonnes officielles (Grade/Echelle, Date
  début, Date fin, Nombre, Taux, Brut, Retenues, Montant net, CIN, Nom,
  Prénom, DDR *(réutilisé pour stocker le PPR)*, Mode de paiement, Type
  référence règlement, Banque, Ville, Numéro compte RIB, Clé, objet
  règlement, Reference).
- **Ligne 5 et suivantes** : une ligne par paiement.

Une **21ᵉ colonne cachée** (non visible dans Excel) stocke l'identifiant
technique du paiement en base de données — elle permet à l'application de
retrouver et mettre à jour la bonne ligne lors d'une modification ou d'une
suppression, sans jamais dupliquer une entrée.

### Fichiers actifs et archivés
- Chaque type de paiement (Vacataire, Heure Sup, Déplacement) possède **son
  propre fichier actif**, nommé `<Type>_<suffixe>.xlsx` (le suffixe est
  personnalisable par l'utilisateur à la clôture).
- Les nouveaux paiements s'ajoutent automatiquement au fichier actif de leur
  type.
- La clôture archive le fichier actif et en crée un nouveau vide.
- La réactivation permet de reprendre un ancien fichier archivé comme
  fichier actif (l'ancien actif est alors automatiquement archivé pour ne
  rien perdre).

### Synchronisation avec la base de données
- Modifier un paiement existant (même type) → **met à jour** la ligne Excel
  correspondante (pas de doublon).
- Supprimer une ligne dans la vue "aperçu" d'un fichier → supprime la ligne
  du fichier Excel **et** l'enregistrement correspondant en base.
- Supprimer un fichier entier → supprime uniquement le fichier physique ;
  les paiements en base ne sont **pas** supprimés automatiquement (action
  volontairement séparée pour éviter une suppression accidentelle massive).

---

## Génération des documents PDF

Deux services distincts génèrent les PDF, car les formats administratifs
sont structurellement différents :

- `PdfGenerationService` — Vacataire et Heure Sup (structure classique :
  en-tête, tableau d'opérations unique, bloc montant net à payer).
- `DeplacementPdfGenerationService` — Déplacement (en-tête différent,
  tableau multi-trajets avec total général, sans bloc de retenue IR,
  mentions légales spécifiques).

Les polices utilisées (Calibri, Times New Roman) sont chargées depuis
`C:/Windows/Fonts/` avec repli automatique sur des polices standard PDF si
elles sont introuvables sur la machine d'exécution — à garder en tête pour
un déploiement sur un système autre que Windows, ou sans ces polices
installées.

---

## Import de professeurs

Le bouton "Importer des données" (accessible depuis le tableau de bord)
importe des **professeurs uniquement** (aucun paiement) depuis un fichier
Excel structuré ainsi :

| Colonne | Contenu |
|---|---|
| A | CIN |
| B | PPR |
| C | Nom |
| D | Prénom |
| E | Grade |
| F | Échelle |
| G | Banque (RIB) |
| H | Ville (RIB) |
| I | Numéro de compte (RIB) |
| J | Clé (RIB) |

La ligne d'en-tête doit contenir le mot **"CIN"** dans sa première colonne
pour que l'import détecte automatiquement où commencent les données.

---

## Limitations connues

- **Chaque installation est indépendante.** L'application utilise une base
  de données locale embarquée (H2) — c'est un choix délibéré permettant une
  installation en un clic sans configuration réseau. Conséquence directe :
  les paiements enregistrés sur un poste ne sont visibles que sur ce poste.
  Seuls les **professeurs** peuvent être transférés d'un poste à l'autre via
  export/import Excel ; il n'existe pas de mécanisme équivalent pour les
  paiements.
- **Pas de fonctionnalité de changement de mot de passe.** Le compte
  administrateur par défaut (`admin` / `admin123`) ne peut être modifié que
  par une intervention directe en base de données.
- **Impression directe non fiable.** Une tentative d'impression directe du
  PDF (sans passer par une sauvegarde manuelle) via `java.awt.Desktop` a été
  testée mais s'est révélée non fonctionnelle dans cet environnement
  JavaFX + Spring Boot ; la génération de PDF passe donc par une sauvegarde
  manuelle suivie d'une impression depuis le lecteur PDF de l'utilisateur.
- **Anciennes lignes Excel sans identifiant technique.** Les paiements
  enregistrés avant l'ajout de la colonne technique cachée n'ont pas
  d'identifiant associé dans leur ligne Excel ; la suppression individuelle
  de ces lignes n'est pas possible depuis l'aperçu du fichier.
- **DDR (date de recrutement)** a été retirée du formulaire de saisie ; le
  champ reste en base pour les professeurs déjà existants mais n'est plus
  collecté ni affiché. Dans l'export Excel, la colonne historiquement
  nommée "DDR" contient désormais le **PPR** du professeur.
- **Dépendance aux polices Windows** pour le PDF Déplacement (voir
  [Génération des documents PDF](#génération-des-documents-pdf)).

---

## Pistes d'amélioration

- Export/import des **paiements** (pas seulement des professeurs) pour
  permettre un partage de données plus complet entre postes non connectés
  en réseau.
- Gestion des utilisateurs et changement de mot de passe en libre-service.
- Historique des modifications de paiements (actuellement, modifier un
  paiement écrase la version précédente sans conserver d'historique).
- Génération d'un installeur multiplateforme (actuellement Windows
  uniquement, via `jpackage --type exe`).
