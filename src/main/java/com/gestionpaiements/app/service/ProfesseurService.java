package com.gestionpaiements.app.service;

import com.gestionpaiements.app.dao.ProfesseurRepository;
import com.gestionpaiements.app.model.Professeur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    /**
     * Retourne le taux et le taux d'IR associés au grade et à l'échelle du professeur.
     * À implémenter selon la configuration officielle.
     *
     * @param grade  le grade du professeur (ex: PRIMAIRE, SUPERIEUR)
     * @param echelle l'échelle du professeur (ex: 5)
     * @return Optional contenant le taux et le taux d'IR, vide si non configuré
     */
    public Optional<TauxIR> trouverTauxEtIRParGradeEtEchelle(String grade, String echelle) {
        // Stub : aucune configuration officielle fournie pour le moment
        return Optional.empty();
    }
}