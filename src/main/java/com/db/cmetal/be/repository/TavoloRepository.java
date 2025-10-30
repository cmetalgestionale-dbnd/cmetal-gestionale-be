package com.db.cmetal.be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.db.cmetal.be.entity.Tavolo;

public interface TavoloRepository extends JpaRepository<Tavolo, Integer> {
    Tavolo findByNumeroAndIsDeletedFalse(Integer numero);
    Tavolo findByNumeroAndIsDeletedTrue(Integer numero);
    List<Tavolo> findByIsDeletedFalse();
}

