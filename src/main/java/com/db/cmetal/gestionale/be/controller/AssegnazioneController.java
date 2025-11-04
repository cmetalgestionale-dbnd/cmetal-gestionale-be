package com.db.cmetal.gestionale.be.controller;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.db.cmetal.gestionale.be.dto.AssegnazioneDto;
import com.db.cmetal.gestionale.be.entity.Assegnazione;
import com.db.cmetal.gestionale.be.entity.Utente;
import com.db.cmetal.gestionale.be.service.AssegnazioneService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assegnazioni")
@RequiredArgsConstructor
public class AssegnazioneController {

    private final AssegnazioneService assegnazioneService;

    @GetMapping
    public List<Assegnazione> getAll() {
        return assegnazioneService.getAll();
    }

    @GetMapping("/utente/{utenteId}/data/{data}")
    public List<Assegnazione> getByUtenteAndData(@PathVariable Long utenteId, @PathVariable String data) {
        return assegnazioneService.getByUtenteAndData(utenteId, java.time.LocalDate.parse(data));
    }

    @PostMapping
    public Assegnazione create(@RequestBody AssegnazioneDto dto) {
        Utente assegnatoDa = (Utente) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        OffsetDateTime assegnazioneAt = OffsetDateTime.parse(dto.getAssegnazioneAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return assegnazioneService.createFromDto(dto, assegnatoDa.getId(), assegnazioneAt);
    }

    @PutMapping("/{id}")
    public Assegnazione update(@PathVariable Long id, @RequestBody AssegnazioneDto dto) {
        return assegnazioneService.updateFromDto(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        assegnazioneService.softDelete(id);
    }
}
