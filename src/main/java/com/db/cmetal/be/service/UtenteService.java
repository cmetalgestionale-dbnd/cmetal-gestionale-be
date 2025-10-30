package com.db.cmetal.be.service;

import java.util.List;
import java.util.Optional;

import com.db.cmetal.be.entity.Utente;

public interface UtenteService {
    Optional<Utente> findById(Long id);
    Optional<Utente> findByUsername(String username);
    List<Utente> findAll();
    Utente save(Utente utente);
    void deleteById(Long id);
}
