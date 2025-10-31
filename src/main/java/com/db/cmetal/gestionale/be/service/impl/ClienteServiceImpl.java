package com.db.cmetal.gestionale.be.service.impl;

import com.db.cmetal.gestionale.be.entity.Cliente;
import com.db.cmetal.gestionale.be.repository.ClienteRepository;
import com.db.cmetal.gestionale.be.service.ClienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        return clienteRepository.findById(id);
    }

    @Override
    public Cliente save(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente update(Long id, Cliente cliente) {
        Cliente existing = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente non trovato"));
        cliente.setId(existing.getId());
        return clienteRepository.save(cliente);
    }

    @Override
    public void delete(Long id) {
        Cliente c = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente non trovato"));
        c.setIsDeleted(true);
        clienteRepository.save(c);
    }
}
