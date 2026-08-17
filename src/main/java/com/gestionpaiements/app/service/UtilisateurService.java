package com.gestionpaiements.app.service;

import com.gestionpaiements.app.dao.UtilisateurRepository;
import com.gestionpaiements.app.model.Utilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Authentifie un utilisateur anhand de son login et de son mot de passe en clair.
     *
     * @param login          le login de l'utilisateur
     * @param motDePasseClair le mot de passe en clair
     * @return Optional contenant l'utilisateur si l'authentification réussit, vide sinon
     */
    public Optional<Utilisateur> authentifier(String login, String motDePasseClair) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByNomUtilisateur(login);
        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            if (BCrypt.checkpw(motDePasseClair, utilisateur.getMotDePasse())) {
                return Optional.of(utilisateur);
            }
        }
        return Optional.empty();
    }

    /**
     * Crée un compte si le login n'est pas déjà utilisé.
     *
     * @param login          le login souhaité
     * @param motDePasseClair le mot de passe en clair
     * @return Optional contenant l'utilisateur créé si le login est disponible, vide sinon
     */
    public Optional<Utilisateur> creerCompte(String login, String motDePasseClair) {
        // Vérifier l'unicité du login
        if (utilisateurRepository.findByNomUtilisateur(login).isPresent()) {
            return Optional.empty(); // login déjà pris
        }
        // Hash du mot de passe
        String motDePasseHash = BCrypt.hashpw(motDePasseClair, BCrypt.gensalt());
        Utilisateur nouvelUtilisateur = new Utilisateur(login, motDePasseHash);
        Utilisateur sauvegarde = utilisateurRepository.save(nouvelUtilisateur);
        return Optional.of(sauvegarde);
    }
}