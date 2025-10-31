package com.db.cmetal.gestionale.be.service.impl;

import com.db.cmetal.gestionale.be.entity.Allegato;
import com.db.cmetal.gestionale.be.repository.AllegatoRepository;
import com.db.cmetal.gestionale.be.service.AllegatoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AllegatoServiceImpl implements AllegatoService {

    private final AllegatoRepository allegatoRepository;

    public AllegatoServiceImpl(AllegatoRepository allegatoRepository) {
        this.allegatoRepository = allegatoRepository;
    }

    @Override
    public List<Allegato> findAll() {
        return allegatoRepository.findAll();
    }

    @Override
    public Optional<Allegato> findById(Long id) {
        return allegatoRepository.findById(id);
    }

    @Override
    public Allegato save(Allegato allegato) {
        return allegatoRepository.save(allegato);
    }

    @Override
    public Allegato update(Long id, Allegato allegato) {
        Allegato existing = allegatoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allegato non trovato"));
        allegato.setId(existing.getId());
        return allegatoRepository.save(allegato);
    }

    @Override
    public void delete(Long id) {
        Allegato allegato = allegatoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allegato non trovato"));
        allegato.setIsDeleted(true);
        allegatoRepository.save(allegato);
    }
}
