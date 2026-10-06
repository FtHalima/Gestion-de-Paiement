package com.gestionpaiements.app.dao;
import com.gestionpaiements.app.model.ImportProfesseurs;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ImportProfesseursRepository extends JpaRepository<ImportProfesseurs, Long> {
    List<ImportProfesseurs> findAllByOrderByDateImportDescIdDesc();
    Optional<ImportProfesseurs> findFirstByOrderByDateImportDescIdDesc();
}
