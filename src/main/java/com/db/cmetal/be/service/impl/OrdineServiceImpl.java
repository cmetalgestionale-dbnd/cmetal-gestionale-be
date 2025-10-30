package com.db.cmetal.be.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.db.cmetal.be.entity.Ordine;
import com.db.cmetal.be.entity.Sessione;
import com.db.cmetal.be.repository.OrdineRepository;
import com.db.cmetal.be.service.OrdineService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdineServiceImpl implements OrdineService {

    private final OrdineRepository ordineRepository;

    @Override
    public List<Ordine> findAll() {
        return ordineRepository.findAll();
    }

    @Override
    public Ordine findById(Long id) {
        return ordineRepository.findById(id).orElse(null);
    }

    @Override
    public Ordine save(Ordine ordine) {
        return ordineRepository.save(ordine);
    }

    @Override
    public Ordine update(Long id, Ordine ordine) {
        ordine.setId(id);
        return ordineRepository.save(ordine);
    }

    @Override
    public void delete(Long id) {
        ordineRepository.deleteById(id);
    }

	@Override
	public List<Ordine> findBySessione(Sessione sessione) {
		return ordineRepository.findBySessione(sessione);
	}
}
