package com.db.cmetal.gestionale.be.service.impl;

import com.db.cmetal.gestionale.be.entity.Assegnazione;
import com.db.cmetal.gestionale.be.repository.AssegnazioneRepository;
import com.db.cmetal.gestionale.be.service.AssegnazioneService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AssegnazioneServiceImpl implements AssegnazioneService {

    private final AssegnazioneRepository assegnazioneRepository;

    public AssegnazioneServiceImpl(AssegnazioneRepository assegnazioneRepository) {
        this.assegnazioneRepository = assegnazioneRepository;
    }

    @Override
    public List<Assegnazione> findAll() {
        return assegnazioneRepository.findAll();
    }

    @Override
    public Optional<Assegnazione> findById(Long id) {
        return assegnazioneRepository.findById(id);
    }

    @Override
    public Assegnazione save(Assegnazione assegnazione) {
        return assegnazioneRepository.save(assegnazione);
    }

    @Override
    public Assegnazione update(Long id, Assegnazione assegnazione) {
        Assegnazione existing = assegnazioneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assegnazione non trovata"));
        assegnazione.setId(existing.getId());
        return assegnazioneRepository.save(assegnazione);
    }

    @Override
    public void delete(Long id) {
        Assegnazione a = assegnazioneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assegnazione non trovata"));
        a.setIsDeleted(true);
        assegnazioneRepository.save(a);
    }
}
