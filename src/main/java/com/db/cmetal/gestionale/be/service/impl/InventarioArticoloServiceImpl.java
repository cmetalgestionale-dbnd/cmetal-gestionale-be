package com.db.cmetal.gestionale.be.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.db.cmetal.gestionale.be.entity.InventarioArticolo;
import com.db.cmetal.gestionale.be.repository.InventarioArticoloRepository;
import com.db.cmetal.gestionale.be.service.InventarioArticoloService;
import com.db.cmetal.gestionale.be.service.WebSocketService;
import com.db.cmetal.gestionale.be.utils.Constants;

@Service
public class InventarioArticoloServiceImpl implements InventarioArticoloService {

    private final InventarioArticoloRepository repository;
    private final WebSocketService wsService;

    public InventarioArticoloServiceImpl(InventarioArticoloRepository repository,
    		WebSocketService wsService) {
        this.repository = repository;
        this.wsService = wsService;
    }

    @Override
    public List<InventarioArticolo> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<InventarioArticolo> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public InventarioArticolo save(InventarioArticolo articolo) {
    	wsService.broadcast(Constants.MSG_REFRESH, null);
        return repository.save(articolo);
    }

    @Override
    public void deleteById(Long id) {
    	wsService.broadcast(Constants.MSG_REFRESH, null);
        repository.deleteById(id);
    }
}
