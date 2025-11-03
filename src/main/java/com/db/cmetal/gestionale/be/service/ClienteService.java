package com.db.cmetal.gestionale.be.service;

import java.util.List;
import java.util.Optional;

import com.db.cmetal.gestionale.be.entity.Cliente;

public interface ClienteService {
    Optional<Cliente> findById(Long id);
    List<Cliente> findAll();
    Cliente save(Cliente cliente);
    void deleteById(Long id);
}
