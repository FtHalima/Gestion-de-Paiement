package com.gestionpaiements.app.service;

import com.gestionpaiements.app.dao.LotRepository;
import com.gestionpaiements.app.model.Lot;
import com.gestionpaiements.app.model.StatutLot;
import com.gestionpaiements.app.model.Utilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class LotService {

    private final LotRepository lotRepository;

    @Autowired
    public LotService(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    /**
     * Retourne le lot actif le plus récent de l'utilisateur, ou en crée un nouveau si aucunlot actif n'existe.
     *
     * @param utilisateur l'utilisateur concerné
     * @return le lot actif (existant ou nouvellement créé)
     */
    public Lot getOuCreerLotActif(Utilisateur utilisateur) {
        Optional<Lot> lotActifOpt = lotRepository.findFirstByUtilisateurAndStatutOrderByIdLotDesc(utilisateur, StatutLot.ACTIF);
        if (lotActifOpt.isPresent()) {
            return lotActifOpt.get();
        }
        // Aucun lot actif : création d'un nouveau lot
        String nomLot = "Lot_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Lot nouveauLot = new Lot(nomLot, LocalDate.now(), StatutLot.ACTIF, utilisateur);
        return lotRepository.save(nouveauLot);
    }

    /**
     * Clôture le lot actif de l'utilisateur en passant son statut à CLOTURE.
     * Ne fait rien si l'utilisateur n'a aucun lot actif.
     *
     * @param utilisateur l'utilisateur concerné
     */
    public void cloturerLotActif(Utilisateur utilisateur) {
        Optional<Lot> lotActifOpt = lotRepository.findFirstByUtilisateurAndStatutOrderByIdLotDesc(utilisateur, StatutLot.ACTIF);
        lotActifOpt.ifPresent(lot -> {
            lot.setStatut(StatutLot.CLOTURE);
            lotRepository.save(lot);
        });
    }

    /**
     * Liste tous les lots clôturés, triés par date de création décroissante.
     *
     * @return liste des lots clôturés
     */
    public List<Lot> listerLotsClotures() {
        List<Lot> lots = lotRepository.findByStatut(StatutLot.CLOTURE);
        lots.sort(Comparator.comparing(Lot::getDateCreation).reversed());
        return lots;
    }
}