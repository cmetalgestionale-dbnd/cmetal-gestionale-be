package com.db.cmetal.gestionale.be.service;

import com.db.cmetal.gestionale.be.entity.Allegato;
import java.util.List;
import java.util.Optional;

public interface AllegatoService {
    List<Allegato> findAll();
    Optional<Allegato> findById(Long id);
    Allegato save(Allegato allegato);
    Allegato update(Long id, Allegato allegato);
    void delete(Long id);
}
