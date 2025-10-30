package com.db.cmetal.be.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.db.cmetal.be.entity.Utente;

public interface UtenteRepository extends JpaRepository<Utente, Long> {
    Optional<Utente> findByUsername(String username);
}
