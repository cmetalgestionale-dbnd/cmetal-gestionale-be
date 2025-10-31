package com.db.cmetal.gestionale.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.db.cmetal.gestionale.be.entity.Cliente;

public interface CommessaRepository extends JpaRepository<Cliente, Long> {

}
