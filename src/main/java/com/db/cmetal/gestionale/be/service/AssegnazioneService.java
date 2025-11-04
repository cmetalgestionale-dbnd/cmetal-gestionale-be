package com.db.cmetal.gestionale.be.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import com.db.cmetal.gestionale.be.dto.AssegnazioneDto;
import com.db.cmetal.gestionale.be.entity.Assegnazione;

public interface AssegnazioneService {
    List<Assegnazione> getAll();
    List<Assegnazione> getByUtenteAndData(Long utenteId, LocalDate data);
    Assegnazione getById(Long id);
    Assegnazione createFromDto(AssegnazioneDto dto, Long assegnatoDaId, OffsetDateTime assegnazioneAt);
    Assegnazione updateFromDto(Long id, AssegnazioneDto dto);
    void softDelete(Long id);
}
