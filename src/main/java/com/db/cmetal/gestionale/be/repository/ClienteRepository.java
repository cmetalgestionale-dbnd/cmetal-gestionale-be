package com.db.cmetal.gestionale.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.db.cmetal.gestionale.be.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

}
