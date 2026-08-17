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
import java.time.LocalDate;

@Entity
@Table(name = "lot")
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLot;

    @Column(nullable = false)
    private String nomLot;

    @Column(name = "date_creation")
    private LocalDate dateCreation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutLot statut;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    // Default constructor
    public Lot() {
    }

    // Constructor
    public Lot(String nomLot, LocalDate dateCreation, StatutLot statut, Utilisateur utilisateur) {
        this.nomLot = nomLot;
        this.dateCreation = dateCreation;
        this.statut = statut;
        this.utilisateur = utilisateur;
    }

    // Getters and Setters
    public Long getIdLot() {
        return idLot;
    }

    public void setIdLot(Long idLot) {
        this.idLot = idLot;
    }

    public String getNomLot() {
        return nomLot;
    }

    public void setNomLot(String nomLot) {
        this.nomLot = nomLot;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public StatutLot getStatut() {
        return statut;
    }

    public void setStatut(StatutLot statut) {
        this.statut = statut;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}