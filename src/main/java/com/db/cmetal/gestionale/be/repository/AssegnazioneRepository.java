package com.db.cmetal.gestionale.be.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.db.cmetal.gestionale.be.entity.Assegnazione;

public interface AssegnazioneRepository extends JpaRepository<Assegnazione, Long> {
    List<Assegnazione> findAllByIsDeletedFalse();
    List<Assegnazione> findByUtenteIdAndAssegnazioneAtBetweenAndIsDeletedFalse(
        Long utenteId,
        OffsetDateTime start,
        OffsetDateTime end
    );
    List<Assegnazione> findByAssegnazioneAtBetweenAndIsDeletedFalse(
    	    OffsetDateTime start,
    	    OffsetDateTime end
    	);
	List<Assegnazione> findByUtenteIdAndIsDeletedFalse(Long utenteId);
	List<Assegnazione> findByIsDeletedTrueOrAssegnazioneAtBefore(OffsetDateTime date);
	List<Assegnazione> findByUtenteId(Long id);
	List<Assegnazione> findByCommessaId(Long id);
	List<Assegnazione> findByClienteId(Long id);
	
	
	@Query("""
			    SELECT a FROM Assegnazione a
			    WHERE a.isDeleted = false
			      AND a.utente.id = :utenteId
			      AND a.assegnazioneAt BETWEEN :start AND :end
			      AND (a.commessa IS NULL OR a.commessa.isDeleted = false)
			      AND (a.cliente IS NULL OR a.cliente.isDeleted = false)
			      AND (a.utente IS NULL OR a.utente.isDeleted = false)
			    ORDER BY a.assegnazioneAt DESC
			""")
	List<Assegnazione> findVisibleByUtenteIdAndAssegnazioneAtBetween(@Param("utenteId") Long utenteId,
			@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);
}
