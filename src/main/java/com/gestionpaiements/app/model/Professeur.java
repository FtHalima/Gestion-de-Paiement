package com.gestionpaiements.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "professeur", uniqueConstraints = {
        @jakarta.persistence.UniqueConstraint(columnNames = "cin"),
        @jakarta.persistence.UniqueConstraint(columnNames = "ppr")
})
public class Professeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProfesseur;

    @Column(nullable = false, unique = true)
    private String cin;

    @Column(nullable = false, unique = true)
    private String ppr;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(name = "ddr")
    private LocalDate ddr;

    private String grade;
    private Integer echelle;
    private String affectation;
    @Column(name = "rib_banque")
    private String ribBanque;
    @Column(name = "rib_ville")
    private String ribVille;
    @Column(name = "rib_numero_compte")
    private String ribNumeroCompte;
    @Column(name = "rib_cle")
    private String ribCle;

    // Default constructor
    public Professeur() {
    }

    // Constructor
    public Professeur(String cin, String ppr, String nom, String prenom, LocalDate ddr,
                      String grade, Integer echelle, String affectation,
                      String ribBanque, String ribVille, String ribNumeroCompte, String ribCle) {
        this.cin = cin;
        this.ppr = ppr;
        this.nom = nom;
        this.prenom = prenom;
        this.ddr = ddr;
        this.grade = grade;
        this.echelle = echelle;
        this.affectation = affectation;
        this.ribBanque = ribBanque;
        this.ribVille = ribVille;
        this.ribNumeroCompte = ribNumeroCompte;
        this.ribCle = ribCle;
    }

    // Getters and Setters
    public Long getIdProfesseur() {
        return idProfesseur;
    }

    public void setIdProfesseur(Long idProfesseur) {
        this.idProfesseur = idProfesseur;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getPpr() {
        return ppr;
    }

    public void setPpr(String ppr) {
        this.ppr = ppr;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public LocalDate getDdr() {
        return ddr;
    }

    public void setDdr(LocalDate ddr) {
        this.ddr = ddr;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Integer getEchelle() {
        return echelle;
    }

    public void setEchelle(Integer echelle) {
        this.echelle = echelle;
    }

    public String getAffectation() {
        return affectation;
    }

    public void setAffectation(String affectation) {
        this.affectation = affectation;
    }

    public String getRibBanque() {
        return ribBanque;
    }

    public void setRibBanque(String ribBanque) {
        this.ribBanque = ribBanque;
    }

    public String getRibVille() {
        return ribVille;
    }

    public void setRibVille(String ribVille) {
        this.ribVille = ribVille;
    }

    public String getRibNumeroCompte() {
        return ribNumeroCompte;
    }

    public void setRibNumeroCompte(String ribNumeroCompte) {
        this.ribNumeroCompte = ribNumeroCompte;
    }

    public String getRibCle() {
        return ribCle;
    }

    public void setRibCle(String ribCle) {
        this.ribCle = ribCle;
    }

    /**
     * Returns the full RIB as concatenation of its parts:
     * ribBanque + ribVille + ribNumeroCompte + ribCle
     * Null parts are treated as empty strings.
     */
    public String getRibComplet() {
        String ribBanque = (this.ribBanque != null) ? this.ribBanque : "";
        String ribVille = (this.ribVille != null) ? this.ribVille : "";
        String ribNumeroCompte = (this.ribNumeroCompte != null) ? this.ribNumeroCompte : "";
        String ribCle = (this.ribCle != null) ? this.ribCle : "";
        return ribBanque + ribVille + ribNumeroCompte + ribCle;
    }
}