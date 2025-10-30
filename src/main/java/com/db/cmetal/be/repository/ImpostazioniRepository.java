package com.db.cmetal.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.db.cmetal.be.entity.Impostazioni;

@Repository
public interface ImpostazioniRepository extends JpaRepository<Impostazioni, String> {
    // Possiamo aggiungere metodi custom se serve
}
