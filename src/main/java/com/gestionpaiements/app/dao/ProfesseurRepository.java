package com.gestionpaiements.app.dao;

import com.gestionpaiements.app.model.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfesseurRepository extends JpaRepository<Professeur, Long> {
    Optional<Professeur> findByCinAndPpr(String cin, String ppr);
}