package com.db.cmetal.be.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.db.cmetal.be.entity.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Recupera tutte le categorie attive
    List<Categoria> findAllByIsDeletedFalse();

    // Recupera una categoria attiva per id
    Optional<Categoria> findByIdAndIsDeletedFalse(Long id);
}

