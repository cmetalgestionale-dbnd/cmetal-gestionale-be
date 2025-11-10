package com.db.cmetal.gestionale.be.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.db.cmetal.gestionale.be.entity.Cliente;
import com.db.cmetal.gestionale.be.repository.ClienteRepository;
import com.db.cmetal.gestionale.be.service.ClienteService;
import com.db.cmetal.gestionale.be.service.WebSocketService;
import com.db.cmetal.gestionale.be.utils.Constants;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;
    private final WebSocketService wsService;

    public ClienteServiceImpl(ClienteRepository repository, WebSocketService wsService) {
        this.repository = repository;
		this.wsService = wsService;
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Cliente> findAll() {
        return repository.findAll();
    }

    @Override
    public Cliente save(Cliente cliente) {
        if (cliente.getIsDeleted() == null) {
            cliente.setIsDeleted(false);
        }
        Cliente saved = repository.save(cliente);
        wsService.broadcast(Constants.MSG_REFRESH, null);
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        repository.findById(id).ifPresent(c -> {
            c.setIsDeleted(true);
            repository.save(c);
        });
        wsService.broadcast(Constants.MSG_REFRESH, null);
    }
}
