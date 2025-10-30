package com.db.cmetal.be.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.db.cmetal.be.entity.CostoProdotto;

@Repository
public interface CostoProdottoRepository extends JpaRepository<CostoProdotto, Long> {

	@Query("SELECT cp FROM CostoProdotto cp WHERE cp.prodotto.id = :prodottoId AND cp.prodotto.isDeleted = false")
	Optional<CostoProdotto> findByProdottoId(@Param("prodottoId") Long prodottoId);
	
	@Query("SELECT cp FROM CostoProdotto cp WHERE cp.prodotto.id = :prodottoId")
	Optional<CostoProdotto> findByProdottoIdIgnoreDelete(@Param("prodottoId") Long prodottoId);

}

