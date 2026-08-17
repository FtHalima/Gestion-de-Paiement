package com.gestionpaiements.app.service;

import com.gestionpaiements.app.dao.ProfesseurRepository;
import com.gestionpaiements.app.model.Professeur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ProfesseurService {

    private final ProfesseurRepository professeurRepository;

    @Autowired
    public ProfesseurService(ProfesseurRepository professeurRepository) {
        this.professeurRepository = professeurRepository;
    }

    /**
     * Recherche un professeur par son CIN et son PPR.
     *
     * @param cin le CIN du professeur
     * @param ppr le PPR du professeur
     * @return Optional contenant le professeur trouvé, vide sinon
     */
    public Optional<Professeur> rechercherParCinPpr(String cin, String ppr) {
        return professeurRepository.findByCinAndPpr(cin, ppr);
    }

    /**
     * Recherche un professeur par son CIN ou son PPR (au moins un des deux doit être fourni).
     *
     * @param cin le CIN du professeur (peut être null ou vide)
     * @param ppr le PPR du professeur (peut être null ou vide)
     * @return Optional contenant le professeur trouvé, vide sinon
     */
    public Optional<Professeur> rechercherParCinOuPpr(String cin, String ppr) {
        if ((cin == null || cin.isEmpty()) && (ppr == null || ppr.isEmpty())) {
            return Optional.empty();
        }
        // Trim and treat empty as null for query
        String cinParam = cin != null && !cin.isEmpty() ? cin : null;
        String pprParam = ppr != null && !ppr.isEmpty() ? ppr : null;
        return professeurRepository.findByCinOrPpr(cinParam, pprParam);
    }

    /**
     * Crée un nouveau professeur si son id est null, sinon retourne l'existant.
     *
     * @param professeur le professeur à créer ou récupérer
     * @return le professeur sauvegardé ou déjà existant
     */
    public Professeur creerOuRecuperer(Professeur professeur) {
        if (professeur.getIdProfesseur() == null) {
            // Nouveau professeur : on le sauvegarde
            return professeurRepository.save(professeur);
        } else {
            // Professeur existant : on le retourne tel quel (on pourrait aussi le recharger)
            return professeurRepository.findById(professeur.getIdProfesseur())
                    .orElseGet(() -> professeurRepository.save(professeur));
        }
    }

    /**
     * Résultat contenant le taux et le taux d'IR associés à un grade et une échelle.
     */
    public static class TauxIR {
        private final BigDecimal taux;
        private final BigDecimal tauxIr;

        public TauxIR(BigDecimal taux, BigDecimal tauxIr) {
            this.taux = taux;
            this.tauxIr = tauxIr;
        }

        public BigDecimal getTaux() {
            return taux;
        }

        public BigDecimal getTauxIr() {
            return tauxIr;
        }
    }

    // Map for taux and IR by grade|echelle
    private static final Map<String, TauxIR> TAUX_IR_MAP = new HashMap<>();
    static {
        TAUX_IR_MAP.put("Professeur d'Enseignement Superieur|12", new TauxIR(new BigDecimal("222"), new BigDecimal("37")));
        TAUX_IR_MAP.put("Professeur Encadrant|12", new TauxIR(new BigDecimal("222"), new BigDecimal("37")));
        TAUX_IR_MAP.put("Professeur Qualifié|12", new TauxIR(new BigDecimal("195"), new BigDecimal("37")));
        TAUX_IR_MAP.put("Professeur Adjoint|12", new TauxIR(new BigDecimal("156"), new BigDecimal("37")));
        TAUX_IR_MAP.put("Inspecteur|12", new TauxIR(new BigDecimal("156"), new BigDecimal("37")));
        TAUX_IR_MAP.put("Inspecteur|11", new TauxIR(new BigDecimal("144"), new BigDecimal("34")));
        TAUX_IR_MAP.put("Personnel d'Enseignement|12", new TauxIR(new BigDecimal("156"), new BigDecimal("37")));
        TAUX_IR_MAP.put("Personnel d'Enseignement|11", new TauxIR(new BigDecimal("144"), new BigDecimal("34")));
        TAUX_IR_MAP.put("Personnel d'Enseignement|10", new TauxIR(new BigDecimal("117"), new BigDecimal("30")));
        TAUX_IR_MAP.put("Professeur Agrégé|12", new TauxIR(new BigDecimal("327"), new BigDecimal("37")));
        // Add more if needed
    }

    /**
     * Retourne le taux et le taux d'IR associés au grade et à l'échelle du professeur.
     *
     * @param grade  le grade du professeur (ex: PRIMAIRE, SUPERIEUR)
     * @param echelle l'échelle du professeur (ex: 5)
     * @return Optional contenant le taux et le taux d'IR, vide si non configuré
     */
    public Optional<TauxIR> trouverTauxEtIRParGradeEtEchelle(String grade, String echelle) {
        if (grade == null || echelle == null) {
            return Optional.empty();
        }
        String key = grade.trim() + "|" + echelle.trim();
        return Optional.ofNullable(TAUX_IR_MAP.get(key));
    }
}