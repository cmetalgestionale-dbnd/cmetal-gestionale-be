package com.db.cmetal.gestionale.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.db.cmetal.gestionale.be.entity.Magazzino;

public interface MagazzinoRepository extends JpaRepository<Magazzino, Long> {
}
