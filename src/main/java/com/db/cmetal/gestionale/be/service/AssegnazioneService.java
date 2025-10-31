package com.db.cmetal.gestionale.be.service;

import com.db.cmetal.gestionale.be.entity.Assegnazione;
import java.util.List;
import java.util.Optional;

public interface AssegnazioneService {
    List<Assegnazione> findAll();
    Optional<Assegnazione> findById(Long id);
    Assegnazione save(Assegnazione assegnazione);
    Assegnazione update(Long id, Assegnazione assegnazione);
    void delete(Long id);
}
