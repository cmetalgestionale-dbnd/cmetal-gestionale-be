package com.db.cmetal.gestionale.be.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.db.cmetal.gestionale.be.entity.Cliente;
import com.db.cmetal.gestionale.be.repository.ClienteRepository;
import com.db.cmetal.gestionale.be.service.ClienteService;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;

    public ClienteServiceImpl(ClienteRepository repository) {
        this.repository = repository;
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
        if (cliente.getCreatedAt() == null) {
            cliente.setCreatedAt(OffsetDateTime.now());
        }
        return repository.save(cliente);
    }

    @Override
    public void deleteById(Long id) {
        repository.findById(id).ifPresent(c -> {
            c.setIsDeleted(true);
            repository.save(c);
        });
    }
}
