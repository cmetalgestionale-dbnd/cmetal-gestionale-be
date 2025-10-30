package com.db.cmetal.be.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.cmetal.be.entity.Categoria;
import com.db.cmetal.be.repository.CategoriaRepository;
import com.db.cmetal.be.service.CategoriaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public List<Categoria> findAll() {
        return categoriaRepository.findAllByIsDeletedFalse(); // <-- filtra isDeleted
    }

    @Override
    public Categoria findById(Long id) {
        return categoriaRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Categoria non trovata con id: " + id));
    }

    @Override
    public Categoria save(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Override
    public void softDelete(Long id) {
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria non trovata con id: " + id));
        c.setIsDeleted(true);   // <-- cancellazione logica
        categoriaRepository.save(c);
    }
}
