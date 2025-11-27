package com.db.cmetal.gestionale.be.service;

import java.util.List;
import java.util.Optional;

import com.db.cmetal.gestionale.be.entity.InventarioArticolo;

public interface InventarioArticoloService {
    List<InventarioArticolo> findAll();
    Optional<InventarioArticolo> findById(Long id);
    InventarioArticolo save(InventarioArticolo articolo);
    void deleteById(Long id);
}
