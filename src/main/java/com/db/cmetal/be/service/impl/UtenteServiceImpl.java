package com.db.cmetal.be.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.db.cmetal.be.entity.Utente;
import com.db.cmetal.be.repository.UtenteRepository;
import com.db.cmetal.be.service.UtenteService;

@Service
public class UtenteServiceImpl implements UtenteService {

    private final UtenteRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UtenteServiceImpl(UtenteRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<Utente> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Utente> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    public List<Utente> findAll() {
        return repository.findAll();
    }

    @Override
    public Utente save(Utente utente) {
        if (utente.getId() == null || !isPasswordEncoded(utente.getPassword())) {
            utente.setPassword(passwordEncoder.encode(utente.getPassword()));
        }
        return repository.save(utente);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private boolean isPasswordEncoded(String password) {
        return password != null && password.startsWith("$2");
    }
}
