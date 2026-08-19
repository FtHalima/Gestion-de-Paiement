package com.gestionpaiements.app.dao;

import com.gestionpaiements.app.model.Lot;
import com.gestionpaiements.app.model.Paiement;
import com.gestionpaiements.app.model.Professeur;
import com.gestionpaiements.app.model.TypePaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    List<Paiement> findByLot(Lot lot);
    List<Paiement> findByTypePaiement(TypePaiement type);
    List<Paiement> findByProfesseur(Professeur professeur);

    @Query("select p from Paiement p join fetch p.professeur where p.typePaiement = :type")
    List<Paiement> findByTypePaiementWithProfesseur(TypePaiement type);

    Optional<Paiement> findFirstByProfesseurOrderByIdPaiementDesc(Professeur professeur);
}