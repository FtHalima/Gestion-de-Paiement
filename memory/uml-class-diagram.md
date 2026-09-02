# UML Class Diagram for Gestion Paiements Application

## Domain Model

```mermaid
classDiagram
    %% Enumerations
    class TypePaiement {
        <<enumeration>>
        VACATAIRE
        HEURE_SUP
        DEPLACEMENT
        +getLibellePdf() String
    }
    
    class StatutLot {
        <<enumeration>>
        ACTIF
        CLOTURE
    }
    
    %% Main Entities
    class Professeur {
        -idProfesseur: Long
        -cin: String
        -ppr: String
        -nom: String
        -prenom: String
        -ddr: LocalDate
        -grade: String
        -echelle: Integer
        -affectation: String
        -ribBanque: String
        -ribVille: String
        -ribNumeroCompte: String
        -ribCle: String
        +getRibComplet(): String
        +getIdProfesseur(): Long
        +setIdProfesseur(Long): void
        +getCin(): String
        +setCin(String): void
        +getPpr(): String
        +setPpr(String): void
        +getNom(): String
        +setNom(String): void
        +getPrenom(): String
        +setPrenom(String): void
        +getDdr(): LocalDate
        +setDdr(LocalDate): void
        +getGrade(): String
        +setGrade(String): void
        +getEchelle(): Integer
        +setEchelle(Integer): void
        +getAffectation(): String
        +setAffectation(String): void
        +getRibBanque(): String
        +setRibBanque(String): void
        +getRibVille(): String
        +setRibVille(String): void
        +getRibNumeroCompte(): String
        +setRibNumeroCompte(String): void
        +getRibCle(): String
        +setRibCle(String): void
    }
    
    class Paiement {
        -idPaiement: Long
        -professeur: Professeur
        -lot: Lot
        -utilisateur: Utilisateur
        -typePaiement: TypePaiement
        -objetReglement: String
        -dateDebut: LocalDate
        -dateFin: LocalDate
        -nombreHeures: BigDecimal
        -taux: BigDecimal
        -tauxIr: BigDecimal
        -montantBrut: BigDecimal
        -retenueIr: BigDecimal
        -montantNet: BigDecimal
        -modePaiement: String
        -typeReferenceReglement: String
        -referenceReglement: String
        -datePaiement: LocalDate
        -exercice: String
        -creanceDOrigine: String
        -codeCgnc: String
        -article: String
        -par: String
        -lig: String
        -motifDeplacement: String
        -lignesDeplacement: List~LigneDeplacement~
        +getIdPaiement(): Long
        +setIdPaiement(Long): void
        +getProfesseur(): Professeur
        +setProfesseur(Professeur): void
        +getLot(): Lot
        +setLot(Lot): void
        +getUtilisateur(): Utilisateur
        +setUtilisateur(Utilisateur): void
        +getTypePaiement(): TypePaiement
        +setTypePaiement(TypePaiement): void
        +getObjetReglement(): String
        +setObjetReglement(String): void
        +getDateDebut(): LocalDate
        +setDateDebut(LocalDate): void
        +getDateFin(): LocalDate
        +setDateFin(LocalDate): void
        +getNombreHeures(): BigDecimal
        +setNombreHeures(BigDecimal): void
        +getTaux(): BigDecimal
        +setTaux(BigDecimal): void
        +getTauxIr(): BigDecimal
        +setTauxIr(BigDecimal): void
        +getMontantBrut(): BigDecimal
        +setMontantBrut(BigDecimal): void
        +getRetenueIr(): BigDecimal
        +setRetenueIr(BigDecimal): void
        +getMontantNet(): BigDecimal
        +setMontantNet(BigDecimal): void
        +getModePaiement(): String
        +setModePaiement(String): void
        +getTypeReferenceReglement(): String
        +setTypeReferenceReglement(String): void
        +getReferenceReglement(): String
        +setReferenceReglement(String): void
        +getDatePaiement(): LocalDate
        +setDatePaiement(LocalDate): void
        +getExercice(): String
        +setExercice(String): void
        +getCreanceDOrigine(): String
        +setCreanceDOrigine(String): void
        +getCodeCgnc(): String
        +setCodeCgnc(String): void
        +getArticle(): String
        +setArticle(String): void
        +getPar(): String
        +setPar(String): void
        +getLig(): String
        +setLig(String): void
        +getMotifDeplacement(): String
        +setMotifDeplacement(String): void
        +getLignesDeplacement(): List~LigneDeplacement~
        +setLignesDeplacement(List~LigneDeplacement~): void
    }
    
    class LigneDeplacement {
        -idLigneDeplacement: Long
        -paiement: Paiement
        -dateDepart: LocalDate
        -dateArrivee: LocalDate
        -parcours: String
        -heureDepart: LocalTime
        -heureRetour: LocalTime
        -nombreTauxBase: BigDecimal
        -tauxBaseApplique: BigDecimal
        -montant: BigDecimal
        +getIdLigneDeplacement(): Long
        +setIdLigneDeplacement(Long): void
        +getPaiement(): Paiement
        +setPaiement(Paiement): void
        +getDateDepart(): LocalDate
        +setDateDepart(LocalDate): void
        +getDateArrivee(): LocalDate
        +setDateArrivee(LocalDate): void
        +getParcours(): String
        +setParcours(String): void
        +getHeureDepart(): LocalTime
        +setHeureDepart(LocalTime): void
        +getHeureRetour(): LocalTime
        +setHeureRetour(LocalTime): void
        +getNombreTauxBase(): BigDecimal
        +setNombreTauxBase(BigDecimal): void
        +getTauxBaseApplique(): BigDecimal
        +setTauxBaseApplique(BigDecimal): void
        +getMontant(): BigDecimal
        +setMontant(BigDecimal): void
    }
    
    class Lot {
        -idLot: Long
        -nomLot: String
        -dateCreation: LocalDate
        -statut: StatutLot
        -utilisateur: Utilisateur
        +getIdLot(): Long
        +setIdLot(Long): void
        +getNomLot(): String
        +setNomLot(String): void
        +getDateCreation(): LocalDate
        +setDateCreation(LocalDate): void
        +getStatut(): StatutLot
        +setStatut(StatutLot): void
        +getUtilisateur(): Utilisateur
        +setUtilisateur(Utilisateur): void
    }
    
    class Utilisateur {
        -idUtilisateur: Long
        -nomUtilisateur: String
        -motDePasse: String
        +getIdUtilisateur(): Long
        +setIdUtilisateur(Long): void
        +getNomUtilisateur(): String
        +setNomUtilisateur(String): void
        +getMotDePasse(): String
        +setMotDePasse(String): void
    }
    
    class ConfigurationApp {
        <<entity>>
        -id: Long
        -cle: String
        -valeur: String
        +getId(): Long
        +setId(Long): void
        +getCle(): String
        +setCle(String): void
        +getValeur(): String
        +setValeur(String): void
    }
    
    %% Relationships
    Professeur "1" -- "*" Paiement : has >
    Paiement "1" -- "1" TypePaiement : has >
    Paiement "1" -- "1" Lot : belongs to >
    Paiement "1" -- "1" Utilisateur : created by >
    Paiement "1" -- "*" LigneDeplacement : contains >
    Lot "1" -- "1" StatutLot : has >
    Utilisateur "1" -- "*" Lot : has >
    Utilisateur "1" -- "*" Paiement : creates >
    
    %% Notes
    note for Professeur "getRibComplet() returns concatenation of ribBanque + ribVille + ribNumeroCompte + ribCle" end
    note for Paiement "For DEPLACEMENT type: retenuIr is always 0, montantNet = montantBrut" end
    note for Paiement "lignesDeplacement is only used for DEPLACEMENT type" end
    note for LigneDeplacement "Represents a travel segment for déplacement payments" end
    note for Lot "Represents a batch/group of payments" end
</class-diagram>
```