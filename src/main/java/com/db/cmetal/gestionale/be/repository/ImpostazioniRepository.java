package com.db.cmetal.gestionale.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.db.cmetal.gestionale.be.entity.Impostazioni;

@Repository
public interface ImpostazioniRepository extends JpaRepository<Impostazioni, String> {
	
}
