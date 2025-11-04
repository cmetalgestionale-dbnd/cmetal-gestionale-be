package com.db.cmetal.gestionale.be.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.db.cmetal.gestionale.be.entity.Assegnazione;

public interface AssegnazioneRepository extends JpaRepository<Assegnazione, Long> {
    List<Assegnazione> findAllByIsDeletedFalse();
    List<Assegnazione> findByUtenteIdAndAssegnazioneAtBetweenAndIsDeletedFalse(
        Long utenteId,
        OffsetDateTime start,
        OffsetDateTime end
    );
}
