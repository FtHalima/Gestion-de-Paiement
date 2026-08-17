package com.gestionpaiements.app.service;

import com.gestionpaiements.app.model.Utilisateur;
import org.springframework.stereotype.Component;

/**
 * Bean de session contenant l'utilisateur actuellement connecté.
 */
@Component
public class SessionUtilisateur {

    private Utilisateur utilisateurConnecte;

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public void setUtilisateurConnecte(Utilisateur utilisateurConnecte) {
        this.utilisateurConnecte = utilisateurConnecte;
    }
}