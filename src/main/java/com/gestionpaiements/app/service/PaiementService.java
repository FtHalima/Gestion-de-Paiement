package com.gestionpaiements.app.service;

import com.gestionpaiements.app.dao.PaiementRepository;
import com.gestionpaiements.app.model.Lot;
import com.gestionpaiements.app.model.Paiement;
import com.gestionpaiements.app.model.TypePaiement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PaiementService {

    private final PaiementRepository paiementRepository;

    @Autowired
    public PaiementService(PaiementRepository paiementRepository) {
        this.paiementRepository = paiementRepository;
    }

    /**
     * Calcule les montants d'un paiement, fixe le mode et la date de paiement par défaut,
     * puis sauvegarde le paiement.
     *
     * @param paiement le paiement à calculer et enregistrer (doit avoir nombreHeures, taux, tauxIr, etc.)
     * @return le paiement sauvegardé avec les montants calculés
     */
    @Transactional
    public Paiement calculerEtEnregistrer(Paiement paiement) {
        BigDecimal nombreHeures = paiement.getNombreHeures();
        BigDecimal taux = paiement.getTaux();
        BigDecimal tauxIr = paiement.getTauxIr();

        // Calcul du montant brut : nombreHeures × taux
        BigDecimal montantBrut = nombreHeures.multiply(taux);
        // Arrondi à 2 décimales
        montantBrut = montantBrut.setScale(2, RoundingMode.HALF_UP);

        // Calcul de la retenue IR : montantBrut × (tauxIr / 100)
        BigDecimal retenueIr = montantBrut.multiply(tauxIr.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
        retenueIr = retenuIr.setScale(2, RoundingMode.HALF_UP);

        // Calcul du montant net : montantBrut - retenueIr
        BigDecimal montantNet = montantBrut.subtract(retenuIr);
        montantNet = montantNet.setScale(2, RoundingMode.HALF_UP);

        // Fixation des champs calculés
        paiement.setMontantBrut(montantBrut);
        paiement.setRetenueIr(retenuIr);
        paiement.setMontantNet(montantNet);

        // Mode de paiement par défaut
        paiement.setModePaiement("VIREMENT");

        // Date de paiement par défaut si non renseignée
        if (paiement.getDatePaiement() == null) {
            paiement.setDatePaiement(LocalDate.now());
        }

        // Sauvegarde
        return paiementRepository.save(paiement);
    }

    /**
     * Liste tous les paiements associés à un lot donné.
     *
     * @param lot le lot
     * @return liste des paiements du lot
     */
    public List<Paiement> listerParLot(Lot lot) {
        return paiementRepository.findByLot(lot);
    }

    /**
     * Liste tous les paiements d'un type donné.
     *
     * @param type le type de paiement
     * @return liste des paiements de ce type
     */
    public List<Paiement> listerParType(TypePaiement type) {
        return paiementRepository.findByTypePaiement(type);
    }

    /**
     * Compte le nombre de paiements d'un type donné (utile pour les statistiques du dashboard).
     *
     * @param type le type de paiement
     * @return le nombre de paiements de ce type
     */
    public long compterParType(TypePaiement type) {
        return paiementRepository.findByTypePaiement(type).size();
    }
}