package com.gestionpaiements.app.dao;

import com.gestionpaiements.app.model.Lot;
import com.gestionpaiements.app.model.StatutLot;
import com.gestionpaiements.app.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {
    Optional<Lot> findFirstByUtilisateurAndStatutOrderByIdLotDesc(Utilisateur utilisateur, StatutLot statut);
    List<Lot> findByStatut(StatutLot statut);
}