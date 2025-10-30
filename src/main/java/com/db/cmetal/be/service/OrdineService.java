package com.db.cmetal.be.service;

import java.util.List;

import com.db.cmetal.be.entity.Ordine;
import com.db.cmetal.be.entity.Sessione;

public interface OrdineService {
    List<Ordine> findAll();
    Ordine findById(Long id);
    Ordine save(Ordine ordine);
    Ordine update(Long id, Ordine ordine);
    void delete(Long id);
	List<Ordine> findBySessione(Sessione sessione);
}
