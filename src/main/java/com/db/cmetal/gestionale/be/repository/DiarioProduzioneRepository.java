package com.db.cmetal.gestionale.be.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.db.cmetal.gestionale.be.entity.DiarioProduzione;

public interface DiarioProduzioneRepository extends JpaRepository<DiarioProduzione, Long> {
    @Query("""
            SELECT d FROM DiarioProduzione d
            WHERE d.data = :data
            ORDER BY d.utente.nome ASC, d.utente.cognome ASC, d.oraInizio ASC
            """)
    List<DiarioProduzione> findByDataOrdered(@Param("data") LocalDate data);

    @Query("""
            SELECT d FROM DiarioProduzione d
            WHERE d.utente.id = :utenteId AND d.data = :data
            ORDER BY d.oraInizio ASC
            """)
    List<DiarioProduzione> findByUtenteIdAndDataOrdered(@Param("utenteId") Long utenteId, @Param("data") LocalDate data);

    @Query("""
            SELECT d FROM DiarioProduzione d
            WHERE d.utente.id = :utenteId AND d.data = :data
            ORDER BY d.oraInizio ASC
            """)
    List<DiarioProduzione> findReportRows(@Param("utenteId") Long utenteId, @Param("data") LocalDate data);
}
