package com.gestionpaiements.app.service;

import com.gestionpaiements.app.dao.PaiementRepository;
import com.gestionpaiements.app.model.Lot;
import com.gestionpaiements.app.model.Paiement;
import com.gestionpaiements.app.model.Professeur;
import com.gestionpaiements.app.model.TypePaiement;
import com.gestionpaiements.app.model.Utilisateur;
import com.gestionpaiements.app.service.ProfesseurService;
import com.gestionpaiements.app.service.LotService;
import com.gestionpaiements.app.service.SessionUtilisateur;
import org.hibernate.Hibernate;
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
    private final ProfesseurService professeurService;
    private final SessionUtilisateur sessionUtilisateur;
    private final LotService lotService;

    @Autowired
    public PaiementService(PaiementRepository paiementRepository, ProfesseurService professeurService, SessionUtilisateur sessionUtilisateur, LotService lotService) {
        this.paiementRepository = paiementRepository;
        this.professeurService = professeurService;
        this.sessionUtilisateur = sessionUtilisateur;
        this.lotService = lotService;
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
        // Deleguer le calcul à la méthode unique
        TauxIRResult result = calculerMontants(paiement.getTypePaiement(), paiement.getNombreHeures(), paiement.getTaux(), paiement.getTauxIr());
        paiement.setMontantBrut(result.getMontantBrut());
        paiement.setRetenueIr(result.getRetenueIr());
        paiement.setMontantNet(result.getMontantNet());

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
        if (nombreHeures == null || taux == null) {
            return new TauxIRResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal montantBrut = nombreHeures.multiply(taux).setScale(2, RoundingMode.HALF_UP);

        // Déplacement : pas de retenue IR, le montant brut est directement le montant final
        if (type == TypePaiement.DEPLACEMENT) {
            return new TauxIRResult(montantBrut, BigDecimal.ZERO, montantBrut);
        }

        if (tauxIr == null) {
            return new TauxIRResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal retenuIr = montantBrut.multiply(tauxIr.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal montantNet = montantBrut.subtract(retenuIr).setScale(2, RoundingMode.HALF_UP);

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
    @Transactional(readOnly = true)
    public List<Paiement> listerParType(TypePaiement type) {
        return paiementRepository.findByTypePaiementWithProfesseur(type);
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

    /**
     * Enregistre un paiement associé à un professeur (existants ou nouveau) et à l'utilisateur connecté.
     * Le mode de paiement est forcé à "VIREMENT" et le type de référence à "RIB".
     * Le calcul des montants est effectué si les champs nécessaires sont fournis.
     *
     * @param paiement le paiement à enregistrer (doit avoir professeur, typePaiement, nombreHeures, taux, tauxIr, etc.)
     * @return le paiement sauvegardé avec son identifiant généré
     */
    @Transactional
    public Paiement enregistrerPaiement(Paiement paiement) {
        // Log AVANT SERVICE
        System.out.println("AVANT SERVICE : nombreHeures=" + paiement.getNombreHeures()
                + ", taux=" + paiement.getTaux()
                + ", tauxIr=" + paiement.getTauxIr());

        // Sauvegarde ou récupération du professeur
        Professeur savedProf = professeurService.creerOuRecuperer(paiement.getProfesseur());
        paiement.setProfesseur(savedProf);

        // Utilisateur connecté
        Utilisateur utilisateur = sessionUtilisateur.getUtilisateurConnecte();
        if (utilisateur == null) {
            throw new IllegalStateException("Aucun utilisateur connecté");
        }
        paiement.setUtilisateur(utilisateur);

        // Lot actif (existant ou créé)
        Lot actifLot = lotService.getOuCreerLotActif(utilisateur);
        paiement.setLot(actifLot);

        // Mode paiement et type de référence fixes
        paiement.setModePaiement("VIREMENT");
        paiement.setTypeReferenceReglement("RIB");

        // Calcul systématique des montants (utilise la méthode unique)
        // Calcul systématique des montants, SAUF pour Déplacement où le contrôleur
        // a déjà calculé le total à partir des lignes de trajets
        if (paiement.getTypePaiement() != TypePaiement.DEPLACEMENT) {
            TauxIRResult result = calculerMontants(paiement.getTypePaiement(), paiement.getNombreHeures(), paiement.getTaux(), paiement.getTauxIr());
            paiement.setMontantBrut(result.getMontantBrut());
            paiement.setRetenueIr(result.getRetenueIr());
            paiement.setMontantNet(result.getMontantNet());
}

        // Log APRÈS CALCUL
        System.out.println("APRÈS CALCUL : montantBrut=" + paiement.getMontantBrut()
                + ", retenueIr=" + paiement.getRetenueIr()
                + ", montantNet=" + paiement.getMontantNet());

        // Log AVANT REPOSITORY
        System.out.println("AVANT REPOSITORY : taux=" + paiement.getTaux() + ", tauxIr=" + paiement.getTauxIr()
                + ", montantBrut=" + paiement.getMontantBrut()
                + ", retenueIr=" + paiement.getRetenueIr()
                + ", montantNet=" + paiement.getMontantNet());

        // Sauvegarde du paiement
        Paiement saved = paiementRepository.save(paiement);

        // Force initialization of lazy-loaded lignesDeplacement collection
        Hibernate.initialize(saved.getLignesDeplacement());

        // Log APRÈS REPOSITORY
        System.out.println("APRÈS REPOSITORY : taux=" + saved.getTaux() + ", tauxIr=" + saved.getTauxIr()
                + ", montantBrut=" + saved.getMontantBrut()
                + ", retenueIr=" + saved.getRetenueIr()
                + ", montantNet=" + saved.getMontantNet());

        return saved;
    }

      /**
     * Retourne le paiement le plus récent associé à un professeur donné.
     *
     * @param professeur le professeur
     * @return le paiement le plus récent, ou vide si aucun paiement n'existe
     */
    
    @Transactional(readOnly = true)
    public Optional<Paiement> trouverDernierPaiementParProfesseur(Professeur professeur) {
        Optional<Paiement> opt = paiementRepository.findFirstByProfesseurOrderByIdPaiementDesc(professeur);
        // Force le chargement de la collection lazy pendant que la session est encore ouverte
        opt.ifPresent(p -> p.getLignesDeplacement().size());
        return opt;
    }

    /**
     * Retourne les N paiements les plus récents (utile pour le tableau de bord).
     *
     * @param limit nombre maximum de paiements à retourner
     * @return liste des paiements les plus récents, triés du plus récent au plus ancien
     */
    public List<Paiement> trouverDerniers(int limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                0, limit, org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "idPaiement"));
        return paiementRepository.findAll(pageable).getContent();
    }

        /**
     * Supprime une liste de paiements.
     *
     * @param paiements les paiements à supprimer
     */
    @Transactional
    public void supprimerPaiements(List<Paiement> paiements) {
        paiementRepository.deleteAll(paiements);
    }

    @Transactional
    public void supprimerParId(Long id) {
        paiementRepository.deleteById(id);
    }
}
