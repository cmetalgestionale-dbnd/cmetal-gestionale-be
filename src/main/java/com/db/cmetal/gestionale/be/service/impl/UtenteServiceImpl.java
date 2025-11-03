package com.db.cmetal.gestionale.be.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.db.cmetal.gestionale.be.entity.Utente;
import com.db.cmetal.gestionale.be.repository.UtenteRepository;
import com.db.cmetal.gestionale.be.service.UtenteService;

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
        // Gestione password
        if (utente.getPassword() != null && !utente.getPassword().isEmpty() && !isPasswordEncoded(utente.getPassword())) {
            utente.setPassword(passwordEncoder.encode(utente.getPassword()));
        } else if (utente.getId() != null && (utente.getPassword() == null || utente.getPassword().isEmpty())) {
            Utente existing = repository.findById(utente.getId()).orElseThrow();
            utente.setPassword(existing.getPassword());
        }

        if (utente.getIsDeleted() == null) {
            utente.setIsDeleted(false);
        }
        if (utente.getAttivo() == null) {
            utente.setAttivo(true);
        }

        LocalDateTime now = LocalDateTime.now();

        if (utente.getId() == null) {
            // Nuovo utente → setta createdAt
            utente.setCreatedAt(now);
        } else {
            // Modifica → conserva createdAt esistente
            Utente existing = repository.findById(utente.getId()).orElseThrow();
            utente.setCreatedAt(existing.getCreatedAt());
        }

        // Aggiorna sempre updatedAt
        utente.setUpdatedAt(now);

        return repository.save(utente);
    }

    @Override
    public void deleteById(Long id) {
        repository.findById(id).ifPresent(u -> {
            u.setIsDeleted(true);
            u.setAttivo(false); // opzionale, per sicurezza
            repository.save(u);
        });
    }


    private boolean isPasswordEncoded(String password) {
        return password != null && password.startsWith("$2");
    }
}
