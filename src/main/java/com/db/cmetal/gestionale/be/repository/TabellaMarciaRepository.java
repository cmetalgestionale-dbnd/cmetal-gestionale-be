package com.db.cmetal.gestionale.be.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.db.cmetal.gestionale.be.entity.TabellaMarcia;

public interface TabellaMarciaRepository extends JpaRepository<TabellaMarcia, Long> {
    @Query("""
            SELECT t FROM TabellaMarcia t
            WHERE t.data = :data
            ORDER BY t.utente.nome ASC, t.utente.cognome ASC, t.targa ASC
            """)
    List<TabellaMarcia> findByDataOrdered(@Param("data") LocalDate data);

    @Query("""
            SELECT t FROM TabellaMarcia t
            WHERE t.utente.id = :utenteId AND t.data = :data
            ORDER BY t.targa ASC
            """)
    List<TabellaMarcia> findByUtenteIdAndDataOrdered(@Param("utenteId") Long utenteId, @Param("data") LocalDate data);

    @Query("""
            SELECT t FROM TabellaMarcia t
            WHERE t.utente.id = :utenteId AND t.data = :data
            ORDER BY t.targa ASC
            """)
    List<TabellaMarcia> findReportRows(@Param("utenteId") Long utenteId, @Param("data") LocalDate data);
}
