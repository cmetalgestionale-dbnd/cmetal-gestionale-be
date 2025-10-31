package com.db.cmetal.gestionale.be.service;

import com.db.cmetal.gestionale.be.entity.Commessa;
import java.util.List;
import java.util.Optional;

public interface CommessaService {
    List<Commessa> findAll();
    Optional<Commessa> findById(Long id);
    Commessa save(Commessa commessa);
    Commessa update(Long id, Commessa commessa);
    void delete(Long id);
}
