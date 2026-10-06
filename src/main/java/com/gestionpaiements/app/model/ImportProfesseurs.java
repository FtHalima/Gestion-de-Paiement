package com.gestionpaiements.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ImportProfesseurs {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomFichier;
    private LocalDateTime dateImport;
    private String etat;
    @Column(length = 2000)
    private String erreur;
    public Long getId() { return id; }
    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String value) { nomFichier = value; }
    public LocalDateTime getDateImport() { return dateImport; }
    public void setDateImport(LocalDateTime value) { dateImport = value; }
    public String getEtat() { return etat; }
    public void setEtat(String value) { etat = value; }
    public String getErreur() { return erreur; }
    public void setErreur(String value) { erreur = value; }
}
