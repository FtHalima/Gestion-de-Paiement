package com.gestionpaiements.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "paiement")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paiement")
    private Long idPaiement;

    @ManyToOne
    @JoinColumn(name = "id_professeur", nullable = false)
    private Professeur professeur;

    @ManyToOne
    @JoinColumn(name = "id_lot", nullable = false)
    private Lot lot;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypePaiement typePaiement;

    @Column(name = "objet_reglement")
    private String objetReglement;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "nombre_heures", precision = 10, scale = 2)
    private BigDecimal nombreHeures;

    @Column(name = "taux", precision = 10, scale = 2)
    private BigDecimal taux;

    @Column(name = "taux_ir", precision = 10, scale = 2)
    private BigDecimal tauxIr;

    @Column(name = "montant_brut", precision = 10, scale = 2)
    private BigDecimal montantBrut;

    @Column(name = "retenue_ir", precision = 10, scale = 2)
    private BigDecimal retenueIr;

    @Column(name = "montant_net", precision = 10, scale = 2)
    private BigDecimal montantNet;

    @Column(name = "mode_paiement")
    private String modePaiement;

    @Column(name = "type_reference_reglement")
    private String typeReferenceReglement;

    @Column(name = "reference_reglement")
    private String referenceReglement;

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    // Default constructor
    public Paiement() {
    }

    // Constructor
    public Paiement(Professeur professeur, Lot lot, Utilisateur utilisateur, TypePaiement typePaiement,
                    String objetReglement, LocalDate dateDebut, LocalDate dateFin,
                    BigDecimal nombreHeures, BigDecimal taux, BigDecimal tauxIr,
                    BigDecimal montantBrut, BigDecimal retenueIr, BigDecimal montantNet,
                    String modePaiement, String typeReferenceReglement, String referenceReglement,
                    LocalDate datePaiement) {
        this.professeur = professeur;
        this.lot = lot;
        this.utilisateur = utilisateur;
        this.typePaiement = typePaiement;
        this.objetReglement = objetReglement;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.nombreHeures = nombreHeures;
        this.taux = taux;
        this.tauxIr = tauxIr;
        this.montantBrut = montantBrut;
        this.retenueIr = retenueIr;
        this.montantNet = montantNet;
        this.modePaiement = modePaiement;
        this.typeReferenceReglement = typeReferenceReglement;
        this.referenceReglement = referenceReglement;
        this.datePaiement = datePaiement;
    }

    // Getters and Setters
    public Long getIdPaiement() {
        return idPaiement;
    }

    public void setIdPaiement(Long idPaiement) {
        this.idPaiement = idPaiement;
    }

    public Professeur getProfesseur() {
        return professeur;
    }

    public void setProfesseur(Professeur professeur) {
        this.professeur = professeur;
    }

    public Lot getLot() {
        return lot;
    }

    public void setLot(Lot lot) {
        this.lot = lot;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public TypePaiement getTypePaiement() {
        return typePaiement;
    }

    public void setTypePaiement(TypePaiement typePaiement) {
        this.typePaiement = typePaiement;
    }

    public String getObjetReglement() {
        return objetReglement;
    }

    public void setObjetReglement(String objetReglement) {
        this.objetReglement = objetReglement;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public BigDecimal getNombreHeures() {
        return nombreHeures;
    }

    public void setNombreHeures(BigDecimal nombreHeures) {
        this.nombreHeures = nombreHeures;
    }

    public BigDecimal getTaux() {
        return taux;
    }

    public void setTaux(BigDecimal taux) {
        this.taux = taux;
    }

    public BigDecimal getTauxIr() {
        return tauxIr;
    }

    public void setTauxIr(BigDecimal tauxIr) {
        this.tauxIr = tauxIr;
    }

    public BigDecimal getMontantBrut() {
        return montantBrut;
    }

    public void setMontantBrut(BigDecimal montantBrut) {
        this.montantBrut = montantBrut;
    }

    public BigDecimal getRetenueIr() {
        return retenueIr;
    }

    public void setRetenueIr(BigDecimal retenueIr) {
        this.retenueIr = retenueIr;
    }

    public BigDecimal getMontantNet() {
        return montantNet;
    }

    public void setMontantNet(BigDecimal montantNet) {
        this.montantNet = montantNet;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public String getTypeReferenceReglement() {
        return typeReferenceReglement;
    }

    public void setTypeReferenceReglement(String typeReferenceReglement) {
        this.typeReferenceReglement = typeReferenceReglement;
    }

    public String getReferenceReglement() {
        return referenceReglement;
    }

    public void setReferenceReglement(String referenceReglement) {
        this.referenceReglement = referenceReglement;
    }

    public LocalDate getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDate datePaiement) {
        this.datePaiement = datePaiement;
    }
}