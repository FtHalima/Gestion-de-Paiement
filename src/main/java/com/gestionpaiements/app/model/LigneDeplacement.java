package com.gestionpaiements.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "ligne_deplacement")
public class LigneDeplacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ligne_deplacement")
    private Long idLigneDeplacement;

    @ManyToOne
    @JoinColumn(name = "id_paiement", nullable = false)
    private Paiement paiement;

    @Column(name = "date_depart")
    private LocalDate dateDepart;

    @Column(name = "date_arrivee")
    private LocalDate dateArrivee;

    @Column(name = "parcours")
    private String parcours;

    @Column(name = "heure_depart")
    private LocalTime heureDepart;

    @Column(name = "heure_retour")
    private LocalTime heureRetour;

    @Column(name = "nombre_taux_base", precision = 10, scale = 2)
    private BigDecimal nombreTauxBase;

    @Column(name = "taux_base_applique", precision = 10, scale = 2)
    private BigDecimal tauxBaseApplique;

    @Column(name = "montant", precision = 10, scale = 2)
    private BigDecimal montant;

    public LigneDeplacement() {}

    public Long getIdLigneDeplacement() { return idLigneDeplacement; }
    public void setIdLigneDeplacement(Long id) { this.idLigneDeplacement = id; }

    public Paiement getPaiement() { return paiement; }
    public void setPaiement(Paiement paiement) { this.paiement = paiement; }

    public LocalDate getDateDepart() { return dateDepart; }
    public void setDateDepart(LocalDate dateDepart) { this.dateDepart = dateDepart; }

    public LocalDate getDateArrivee() { return dateArrivee; }
    public void setDateArrivee(LocalDate dateArrivee) { this.dateArrivee = dateArrivee; }

    public String getParcours() { return parcours; }
    public void setParcours(String parcours) { this.parcours = parcours; }

    public LocalTime getHeureDepart() { return heureDepart; }
    public void setHeureDepart(LocalTime heureDepart) { this.heureDepart = heureDepart; }

    public LocalTime getHeureRetour() { return heureRetour; }
    public void setHeureRetour(LocalTime heureRetour) { this.heureRetour = heureRetour; }

    public BigDecimal getNombreTauxBase() { return nombreTauxBase; }
    public void setNombreTauxBase(BigDecimal nombreTauxBase) { this.nombreTauxBase = nombreTauxBase; }

    public BigDecimal getTauxBaseApplique() { return tauxBaseApplique; }
    public void setTauxBaseApplique(BigDecimal tauxBaseApplique) { this.tauxBaseApplique = tauxBaseApplique; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }
}