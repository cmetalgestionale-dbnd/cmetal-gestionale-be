package com.db.cmetal.gestionale.be.service.impl;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.cmetal.gestionale.be.dto.AssegnazioneDto;
import com.db.cmetal.gestionale.be.entity.Assegnazione;
import com.db.cmetal.gestionale.be.entity.Cliente;
import com.db.cmetal.gestionale.be.entity.Commessa;
import com.db.cmetal.gestionale.be.entity.Utente;
import com.db.cmetal.gestionale.be.repository.AssegnazioneRepository;
import com.db.cmetal.gestionale.be.repository.ClienteRepository;
import com.db.cmetal.gestionale.be.repository.CommessaRepository;
import com.db.cmetal.gestionale.be.repository.UtenteRepository;
import com.db.cmetal.gestionale.be.service.AssegnazioneService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AssegnazioneServiceImpl implements AssegnazioneService {

    private final AssegnazioneRepository assegnazioneRepository;
    private final UtenteRepository utenteRepository;
    private final CommessaRepository commessaRepository;
    private final ClienteRepository clienteRepository;

    @Override
    public List<Assegnazione> getAll() {
        return assegnazioneRepository.findAllByIsDeletedFalse();
    }

    @Override
    public List<Assegnazione> getByUtenteAndData(Long utenteId, LocalDate data) {
        OffsetDateTime startOfDay = data.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime endOfDay = data.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        return assegnazioneRepository.findByUtenteIdAndAssegnazioneAtBetweenAndIsDeletedFalse(utenteId, startOfDay, endOfDay);
    }

    @Override
    public Assegnazione getById(Long id) {
        return assegnazioneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assegnazione non trovata"));
    }

    @Override
    public Assegnazione createFromDto(AssegnazioneDto dto, Long assegnatoDaId, OffsetDateTime assegnazioneAt) {
        Utente utente = utenteRepository.findById(dto.getUtenteId())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        Commessa commessa = commessaRepository.findById(dto.getCommessaId())
                .orElseThrow(() -> new RuntimeException("Commessa non trovata"));
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente non trovato"));
        Utente assegnatoDa = utenteRepository.findById(assegnatoDaId)
                .orElseThrow(() -> new RuntimeException("Utente assegnatore non trovato"));

        Assegnazione a = new Assegnazione();
        a.setUtente(utente);
        a.setCommessa(commessa);
        a.setCliente(cliente);
        a.setAssegnatoDa(assegnatoDa);
        a.setNote(dto.getNote());
        a.setAssegnazioneAt(assegnazioneAt);
        a.setCreatedAt(OffsetDateTime.now());
        a.setUpdatedAt(OffsetDateTime.now());
        a.setIsDeleted(false);
        return assegnazioneRepository.save(a);
    }

    @Override
    public Assegnazione updateFromDto(Long id, AssegnazioneDto dto) {
        Assegnazione existing = getById(id);

        existing.setUtente(utenteRepository.findById(dto.getUtenteId())
                .orElseThrow(() -> new RuntimeException("Utente non trovato")));
        existing.setCommessa(commessaRepository.findById(dto.getCommessaId())
                .orElseThrow(() -> new RuntimeException("Commessa non trovata")));
        existing.setCliente(clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente non trovato")));
        existing.setNote(dto.getNote());
        existing.setUpdatedAt(OffsetDateTime.now());

        return assegnazioneRepository.save(existing);
    }

    @Override
    public void softDelete(Long id) {
        Assegnazione a = getById(id);
        a.setIsDeleted(true);
        a.setUpdatedAt(OffsetDateTime.now());
        assegnazioneRepository.save(a);
    }
}
