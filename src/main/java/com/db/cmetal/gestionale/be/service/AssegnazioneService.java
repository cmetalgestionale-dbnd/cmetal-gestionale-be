package com.db.cmetal.gestionale.be.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.db.cmetal.gestionale.be.dto.AssegnazioneDto;
import com.db.cmetal.gestionale.be.entity.Allegato;
import com.db.cmetal.gestionale.be.entity.Assegnazione;

public interface AssegnazioneService {
    List<Assegnazione> getAll();
    List<Assegnazione> getByUtenteAndData(Long utenteId, LocalDate data);
    Assegnazione getById(Long id);
    Assegnazione createFromDto(AssegnazioneDto dto, Long assegnatoDaId, OffsetDateTime assegnazioneAt);
    Assegnazione updateFromDto(Long id, AssegnazioneDto dto);
    void softDelete(Long id);
	Assegnazione startAssegnazione(Long id, Long id2);
	Assegnazione endAssegnazione(Long id, Long id2);
	Allegato uploadFoto(Long assegnazioneId, MultipartFile file, Long utenteId) throws Exception;
    Optional<ResponseEntity<byte[]>> getFotoFile(Long assegnazioneId) throws Exception;
}
