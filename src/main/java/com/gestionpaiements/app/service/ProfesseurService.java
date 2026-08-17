package com.gestionpaiements.app.service;

import com.gestionpaiements.app.dao.ProfesseurRepository;
import com.gestionpaiements.app.model.Professeur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}