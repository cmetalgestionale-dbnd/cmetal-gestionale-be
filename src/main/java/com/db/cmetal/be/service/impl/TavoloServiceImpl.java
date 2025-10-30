package com.db.cmetal.be.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.db.cmetal.be.entity.Ordine;
import com.db.cmetal.be.entity.Tavolo;
import com.db.cmetal.be.repository.OrdineRepository;
import com.db.cmetal.be.repository.TavoloRepository;
import com.db.cmetal.be.service.TavoloService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TavoloServiceImpl implements TavoloService {

    private final TavoloRepository tavoloRepository;
    private final OrdineRepository ordineRepository;

    @Override
    public List<Tavolo> findAll() {
        return tavoloRepository.findByIsDeletedFalse();
    }

    @Override
    public Tavolo findById(Integer id) {
        return tavoloRepository.findById(id)
            .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
            .orElse(null);
    }

    @Override
    public Tavolo save(Tavolo tavolo) {
        if (tavolo.getId() == null) { // nuovo tavolo
            Tavolo existing = tavoloRepository.findByNumeroAndIsDeletedTrue(tavolo.getNumero());
            if (existing != null) {
                if (Boolean.TRUE.equals(existing.getIsDeleted())) {
                    // Riattiva il tavolo eliminato
                    existing.setIsDeleted(false);
                    return tavoloRepository.save(existing);
                } else {
                    throw new IllegalArgumentException("Esiste già un tavolo con questo numero");
                }
            }
        }
        return tavoloRepository.save(tavolo);
    }


    @Override
    public Tavolo update(Integer id, Tavolo tavolo) {
        tavolo.setId(id);
        return tavoloRepository.save(tavolo);
    }

    @Override
    public void delete(Integer id) {
        tavoloRepository.findById(id).ifPresent(t -> {
            t.setIsDeleted(true);
            tavoloRepository.save(t);
        });
    }

    @Override
    public Tavolo findByNumero(Integer numero) {
        return tavoloRepository.findByNumeroAndIsDeletedFalse(numero);
    }

    @Override
    public List<Ordine> findBySessione(Long sessioneId) {
        return ordineRepository.findBySessioneId(sessioneId); // già filtra sessioni isDeleted=false
    }


}
