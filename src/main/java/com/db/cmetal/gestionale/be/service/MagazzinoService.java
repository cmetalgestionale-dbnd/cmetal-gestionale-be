package com.db.cmetal.gestionale.be.service;

import java.util.List;
import java.util.Optional;
import com.db.cmetal.gestionale.be.entity.Magazzino;

public interface MagazzinoService {
    List<Magazzino> findAll();
    Optional<Magazzino> findById(Long id);
    Magazzino save(Magazzino magazzino);
    void deleteById(Long id);
}
