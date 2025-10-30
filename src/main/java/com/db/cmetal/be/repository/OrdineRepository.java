package com.db.cmetal.be.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.db.cmetal.be.dto.ProductSalesDto;
import com.db.cmetal.be.entity.Ordine;
import com.db.cmetal.be.entity.Sessione;

public interface OrdineRepository extends JpaRepository<Ordine, Long> {
    // Ordini non consegnati e appartenenti a sessioni attive
    @Query("SELECT o FROM Ordine o WHERE o.flagConsegnato = false AND o.sessione.isDeleted = false")
    List<Ordine> findByFlagConsegnatoFalse();

    // Ordini appartenenti a una lista di sessioni attive
    @Query("SELECT o FROM Ordine o WHERE o.sessione IN :sessioni AND o.sessione.isDeleted = false")
    List<Ordine> findBySessioneIn(List<Sessione> sessioni);

    // Ordini per sessione attiva
    @Query("SELECT o FROM Ordine o WHERE o.sessione.id = :sessioneId AND o.sessione.isDeleted = false")
    List<Ordine> findBySessioneId(Long sessioneId);

    @Query("SELECT o FROM Ordine o WHERE o.sessione = :sessione AND o.sessione.isDeleted = false")
    List<Ordine> findBySessione(Sessione sessione);

    @Query("SELECT o FROM Ordine o WHERE o.orario BETWEEN :from AND :to AND o.sessione.isDeleted = false")
    List<Ordine> findByOrarioBetween(LocalDateTime from, LocalDateTime to);
    
    // nuovo: ordini nel range, solo sessioni attive
    @Query("SELECT o FROM Ordine o WHERE o.orario BETWEEN :from AND :to AND o.sessione.isDeleted = false")
    List<Ordine> findByOrarioBetweenAndSessioneIsDeletedFalse(LocalDateTime from, LocalDateTime to);

    @Query("SELECT new com.db.ayce.be.dto.ProductSalesDto(o.prodotto.id, o.prodotto.nome, SUM(o.quantita)) " +
	       "FROM Ordine o " +
	       "WHERE o.orario BETWEEN :start AND :end AND o.prodotto.isDeleted = false " +
	       "GROUP BY o.prodotto.id, o.prodotto.nome " +
	       "ORDER BY SUM(o.quantita) DESC")
	List<ProductSalesDto> findTopProductsByPeriod(LocalDateTime start, LocalDateTime end, PageRequest pageRequest);

	@Query("SELECT new com.db.ayce.be.dto.ProductSalesDto(o.prodotto.id, o.prodotto.nome, SUM(o.quantita)) " +
	       "FROM Ordine o " +
	       "WHERE o.orario BETWEEN :start AND :end AND o.prodotto.isDeleted = false " +
	       "GROUP BY o.prodotto.id, o.prodotto.nome " +
	       "ORDER BY SUM(o.quantita) ASC")
	List<ProductSalesDto> findBottomProductsByPeriod(LocalDateTime start, LocalDateTime end, PageRequest pageRequest);
}
