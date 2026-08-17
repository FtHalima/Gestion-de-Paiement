package com.gestionpaiements.app.dao;

import com.gestionpaiements.app.model.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfesseurRepository extends JpaRepository<Professeur, Long> {
    Optional<Professeur> findByCinAndPpr(String cin, String ppr);

    @Query("SELECT p FROM Professeur p WHERE ((:cin IS NOT NULL AND p.cin = :cin) OR (:ppr IS NOT NULL AND p.ppr = :ppr))")
    Optional<Professeur> findByCinOrPpr(@Param("cin") String cin, @Param("ppr") String ppr);
}