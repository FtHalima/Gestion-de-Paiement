package com.gestionpaiements.app.dao;
import com.gestionpaiements.app.model.LigneImportProfesseur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface LigneImportProfesseurRepository extends JpaRepository<LigneImportProfesseur, Long> {
    List<LigneImportProfesseur> findByImportIdOrderByNumeroLigneAsc(Long importId);
}
