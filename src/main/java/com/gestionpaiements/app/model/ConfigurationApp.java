package com.gestionpaiements.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuration_app")
public class ConfigurationApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exercice")
    private String exercice;

    @Column(name = "code_cgnc")
    private String codeCgnc;

    @Column(name = "article")
    private String article;

    @Column(name = "par")
    private String par;

    @Column(name = "lig")
    private String lig;

    @Column(name = "ville_signature")
    private String villeSignature;

    @Column(name = "texte_decret", length = 2000)
    private String texteDecret;

    @Column(name = "nom_direction")
    private String nomDirection;

    @Lob
    @Column(name = "logo")
    private byte[] logo;

    // Default constructor
    public ConfigurationApp() {
    }

    // Constructor
    public ConfigurationApp(String exercice, String codeCgnc, String article, String par, String lig,
                            String villeSignature, String texteDecret, String nomDirection, byte[] logo) {
        this.exercice = exercice;
        this.codeCgnc = codeCgnc;
        this.article = article;
        this.par = par;
        this.lig = lig;
        this.villeSignature = villeSignature;
        this.texteDecret = texteDecret;
        this.nomDirection = nomDirection;
        this.logo = logo;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExercice() {
        return exercice;
    }

    public void setExercice(String exercice) {
        this.exercice = exercice;
    }

    public String getCodeCgnc() {
        return codeCgnc;
    }

    public void setCodeCgnc(String codeCgnc) {
        this.codeCgnc = codeCgnc;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getPar() {
        return par;
    }

    public void setPar(String par) {
        this.par = par;
    }

    public String getLig() {
        return lig;
    }

    public void setLig(String lig) {
        this.lig = lig;
    }

    public String getVilleSignature() {
        return villeSignature;
    }

    public void setVilleSignature(String villeSignature) {
        this.villeSignature = villeSignature;
    }

    public String getTexteDecret() {
        return texteDecret;
    }

    public void setTexteDecret(String texteDecret) {
        this.texteDecret = texteDecret;
    }

    public String getNomDirection() {
        return nomDirection;
    }

    public void setNomDirection(String nomDirection) {
        this.nomDirection = nomDirection;
    }

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }
}