package com.db.cmetal.be.service;

import java.util.List;

import com.db.cmetal.be.entity.Categoria;

public interface CategoriaService {
    List<Categoria> findAll();
    Categoria findById(Long id);
    Categoria save(Categoria categoria);
	void softDelete(Long id);
}
