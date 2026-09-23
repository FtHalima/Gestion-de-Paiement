package com.gestionpaiements.app.service;

import com.gestionpaiements.app.dao.PaiementRepository;
import com.gestionpaiements.app.model.*;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.List;

public class DeplacementDisponibiliteRegression {
    private static LigneDeplacement trajet(int debut, int fin) {
        LigneDeplacement ligne = new LigneDeplacement();
        ligne.setDateDepart(LocalDate.of(2026, 9, debut));
        ligne.setDateArrivee(LocalDate.of(2026, 9, fin));
        return ligne;
    }

    private static void refuse(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException e) {
            if (!e.getMessage().contains("Veuillez modifier") || !e.getMessage().contains("20/09/2026"))
                throw new AssertionError("Message incomplet", e);
            return;
        }
        throw new AssertionError("Chevauchement accepté");
    }

    public static void main(String[] args) {
        LigneDeplacement occupe = trajet(20, 28);
        for (LigneDeplacement conflit : List.of(trajet(26,30), trajet(20,28), trajet(21,22), trajet(19,30), trajet(28,30))) {
            refuse(() -> DeplacementDisponibilite.verifier(List.of(occupe, conflit), List.of()));
            refuse(() -> DeplacementDisponibilite.verifier(List.of(conflit), List.of(occupe)));
        }
        DeplacementDisponibilite.verifier(List.of(trajet(19,19), trajet(29,30)), List.of(occupe));
        DeplacementDisponibilite.verifier(List.of(new LigneDeplacement()), List.of(occupe));

        Professeur professeur = new Professeur(); professeur.setIdProfesseur(1L);
        Paiement existant = new Paiement(); existant.setIdPaiement(10L);
        existant.getLignesDeplacement().add(occupe);
        PaiementRepository repository = (PaiementRepository) Proxy.newProxyInstance(
                PaiementRepository.class.getClassLoader(), new Class[]{PaiementRepository.class},
                (proxy, method, arguments) -> {
                    if (!method.getName().equals("findDeplacementsAvecTrajets")) throw new AssertionError(method.getName());
                    return arguments[0] == professeur ? List.of(existant) : List.of();
                });
        PaiementService service = new PaiementService(repository, null, null, null);
        Paiement nouveau = new Paiement(); nouveau.setTypePaiement(TypePaiement.DEPLACEMENT);
        nouveau.setProfesseur(professeur); nouveau.getLignesDeplacement().add(trajet(26,30));
        refuse(() -> service.verifierDisponibiliteDeplacement(nouveau));
        nouveau.setIdPaiement(10L);
        service.verifierDisponibiliteDeplacement(nouveau); // Modification du même paiement autorisée.
        nouveau.getLignesDeplacement().add(0, occupe);
        refuse(() -> service.verifierDisponibiliteDeplacement(nouveau)); // Mais pas de conflit interne.
        nouveau.getLignesDeplacement().clear(); nouveau.getLignesDeplacement().add(trajet(26,30));
        nouveau.setIdPaiement(null);
        Professeur autre = new Professeur(); autre.setIdProfesseur(2L); nouveau.setProfesseur(autre);
        service.verifierDisponibiliteDeplacement(nouveau);
        nouveau.setProfesseur(professeur); nouveau.setTypePaiement(TypePaiement.VACATAIRE);
        service.verifierDisponibiliteDeplacement(nouveau);
        System.out.println("OK : chevauchements internes/enregistrés, dates inclusives, modification et autres professeurs.");
    }
}
