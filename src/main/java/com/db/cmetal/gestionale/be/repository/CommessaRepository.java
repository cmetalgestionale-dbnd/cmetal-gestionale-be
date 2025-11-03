package com.db.cmetal.gestionale.be.repository;

import com.db.cmetal.gestionale.be.entity.Commessa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommessaRepository extends JpaRepository<Commessa, Long> {
    Optional<Commessa> findByCodice(String codice);
}
