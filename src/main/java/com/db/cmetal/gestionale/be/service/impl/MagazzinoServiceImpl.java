package com.db.cmetal.gestionale.be.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.db.cmetal.gestionale.be.entity.Magazzino;
import com.db.cmetal.gestionale.be.repository.MagazzinoRepository;
import com.db.cmetal.gestionale.be.service.MagazzinoService;
import com.db.cmetal.gestionale.be.service.WebSocketService;
import com.db.cmetal.gestionale.be.utils.Constants;

@Service
public class MagazzinoServiceImpl implements MagazzinoService {

    private final MagazzinoRepository repository;
    private final WebSocketService wsService;

    public MagazzinoServiceImpl(MagazzinoRepository repository,
    		WebSocketService wsService) {
        this.repository = repository;
        this.wsService = wsService;
    }

    @Override
    public List<Magazzino> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Magazzino> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Magazzino save(Magazzino magazzino) {
    	wsService.broadcast(Constants.MSG_REFRESH, null);
        return repository.save(magazzino);
    }

    @Override
    public void deleteById(Long id) {
    	wsService.broadcast(Constants.MSG_REFRESH, null);
        repository.deleteById(id);
    }
}
