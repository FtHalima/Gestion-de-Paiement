package com.gestionpaiements.app.dao;

import com.gestionpaiements.app.model.Lot;
import com.gestionpaiements.app.model.Paiement;
import com.gestionpaiements.app.model.TypePaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    List<Paiement> findByLot(Lot lot);
    List<Paiement> findByTypePaiement(TypePaiement type);
}