package com.gestionpaiements.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class LigneImportProfesseur {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long importId;
    private int numeroLigne;
    @Lob private String donneesOriginales;
    @Lob private String donnees;
    @Column(length = 2000) private String erreur;
    private Long professeurId;
    private LocalDateTime dateCorrection;
    @Version private Long version;
    public Long getId() { return id; }
    public Long getImportId() { return importId; }
    public void setImportId(Long value) { importId = value; }
    public int getNumeroLigne() { return numeroLigne; }
    public void setNumeroLigne(int value) { numeroLigne = value; }
    public String getDonneesOriginales() { return donneesOriginales; }
    public void setDonneesOriginales(String value) { donneesOriginales = value; }
    public String getDonnees() { return donnees; }
    public void setDonnees(String value) { donnees = value; }
    public String getErreur() { return erreur; }
    public void setErreur(String value) { erreur = value; }
    public Long getProfesseurId() { return professeurId; }
    public void setProfesseurId(Long value) { professeurId = value; }
    public LocalDateTime getDateCorrection() { return dateCorrection; }
    public void setDateCorrection(LocalDateTime value) { dateCorrection = value; }
    public String getStatut() {
        if (erreur != null && !erreur.isBlank()) return "Erreur";
        if (professeurId == null) return "À traiter";
        return dateCorrection == null ? "Importé" : "Corrigé";
    }
}
