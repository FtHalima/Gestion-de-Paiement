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
     * Calcule les montants d'un paiement, puis sauvegarde le paiement.
     * La date de paiement et le mode sont ceux fournis dans l'objet paiement
     * (pas de valeur par défaut appliquée ici).
     *
     * @param paiement le paiement à calculer et enregistrer (doit avoir nombreHeures, taux, tauxIr, etc.)
     * @return le paiement sauvegardé avec les montants calculés
     */
    @Transactional
    public Paiement calculerEtEnregistrer(Paiement paiement) {
        BigDecimal nombreHeures = paiement.getNombreHeures();
        BigDecimal taux = paiement.getTaux();
        BigDecimal tauxIr = paiement.getTauxIr();
        TypePaiement type = paiement.getTypePaiement();

        BigDecimal montantBrut = BigDecimal.ZERO;
        BigDecimal retenuIr = BigDecimal.ZERO;

        if (nombreHeures != null && taux != null && tauxIr != null) {
            // Calcul du montant brut : nombreHeures × taux
            montantBrut = nombreHeures.multiply(taux);
            montantBrut = montantBrut.setScale(2, RoundingMode.HALF_UP);

            // Calcul de la retenue IR : montantBrut × (tauxIr / 100)
            retenuIr = montantBrut.multiply(tauxIr.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
            retenuIr = retenuIr.setScale(2, RoundingMode.HALF_UP);
        }

        // Calcul du montant net : montantBrut - retenueIr
        BigDecimal montantNet = montantBrut.subtract(retenuIr);
        montantNet = montantNet.setScale(2, RoundingMode.HALF_UP);

        // Fixation des champs calculés
        paiement.setMontantBrut(montantBrut);
        paiement.setRetenueIr(retenuIr);
        paiement.setMontantNet(montantNet);

        // Sauvegarde
        return paiementRepository.save(paiement);
    }

    /**
     * Calcule les montants brut, retenue IR et net en fonction du type, nombre d'heures, taux et taux d'IR.
     * Utilisé pour l'affichage préalable à l'enregistrement.
     *
     * @param type      le type de paiement
     * @param nombreHeures nombre d'heures
     * @param taux      taux horaire
     * @param tauxIr    taux d'IR (%)
     * @return résultat contenant les trois montants
     */
    public TauxIRResult calculerMontants(TypePaiement type, BigDecimal nombreHeures, BigDecimal taux, BigDecimal tauxIr) {
        if (nombreHeures == null || taux == null || tauxIr == null) {
            return new TauxIRResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        // Calcul du montant brut : nombreHeures × taux
        BigDecimal montantBrut = nombreHeures.multiply(taux);
        montantBrut = montantBrut.setScale(2, RoundingMode.HALF_UP);

        // Calcul de la retenue IR : montantBrut × (tauxIr / 100)
        BigDecimal retenuIr = montantBrut.multiply(tauxIr.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
        retenuIr = retenuIr.setScale(2, RoundingMode.HALF_UP);

        // Calcul du montant net : montantBrut - retenueIr
        BigDecimal montantNet = montantBrut.subtract(retenuIr);
        montantNet = montantNet.setScale(2, RoundingMode.HALF_UP);

        return new TauxIRResult(montantBrut, retenuIr, montantNet);
    }

    // --- Méthodes de calcul préparation (stubs) ---

    /**
     * Calcul spécifique pour le type VACATAIRE.
     * À implémenter avec la formule officielle lorsque disponible.
     *
     * @param nombreHeures nombre d'heures
     * @param taux         taux horaire
     * @param tauxIr       taux d'IR (%)
     * @return montant brut, retenue IR, montant net
     */
    public TauxIRResult calculerVacataire(BigDecimal nombreHeures, BigDecimal taux, BigDecimal tauxIr) {
        // Stub : retourne zéro
        return new TauxIRResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Calcul spécifique pour le type HEURE_SUP.
     * À implémenter avec la formule officielle lorsque disponible.
     *
     * @param nombreHeures nombre d'heures
     * @param taux         taux horaire
     * @param tauxIr       taux d'IR (%)
     * @return montant brut, retenue IR, montant net
     */
    public TauxIRResult calculerHeureSupplementaire(BigDecimal nombreHeures, BigDecimal taux, BigDecimal tauxIr) {
        // Stub : retourne zéro
        return new TauxIRResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Calcul spécifique pour le type DEPLACEMENT.
     * À implémenter avec la formule officielle lorsque disponible.
     *
     * @param nombreHeures nombre d'heures (peut être non utilisé selon la formule)
     * @param taux         taux horaire (peut être non utilisé)
     * @param tauxIr       taux d'IR (%) (peut être non utilisé)
     * @return montant brut, retenue IR, montant net
     */
    public TauxIRResult calculerDeplacement(BigDecimal nombreHeures, BigDecimal taux, BigDecimal tauxIr) {
        // Stub : retourne zéro
        return new TauxIRResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /** Helper class to hold calculation results. */
    public static class TauxIRResult {
        private final BigDecimal montantBrut;
        private final BigDecimal retenueIr;
        private final BigDecimal montantNet;

        public TauxIRResult(BigDecimal montantBrut, BigDecimal retenueIr, BigDecimal montantNet) {
            this.montantBrut = montantBrut;
            this.retenueIr = retenueIr;
            this.montantNet = montantNet;
        }

        public BigDecimal getMontantBrut() {
            return montantBrut;
        }

        public BigDecimal getRetenueIr() {
            return retenueIr;
        }

        public BigDecimal getMontantNet() {
            return montantNet;
        }
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