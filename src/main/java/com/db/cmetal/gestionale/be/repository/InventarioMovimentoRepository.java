package com.db.cmetal.gestionale.be.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.db.cmetal.gestionale.be.entity.InventarioMovimento;

public interface InventarioMovimentoRepository extends JpaRepository<InventarioMovimento, Long> {

    List<InventarioMovimento> findByInventarioIdOrderByMovimentoAtDesc(Long inventarioId);

    //per singolo articolo + range date
    List<InventarioMovimento> findByInventarioIdAndMovimentoAtBetweenOrderByMovimentoAtDesc(
            Long inventarioId,
            OffsetDateTime from,
            OffsetDateTime to
    );

    //per intero magazzino + range date
    List<InventarioMovimento> findByInventarioMagazzinoIdAndMovimentoAtBetweenOrderByMovimentoAtDesc(
            Long magazzinoId,
            OffsetDateTime from,
            OffsetDateTime to
    );

    @Query("""
    	    SELECT m FROM InventarioMovimento m
    	    WHERE m.inventario.magazzino.id = :magazzinoId
    	      AND m.movimentoAt >= :from
    	      AND m.movimentoAt < :to
    	    ORDER BY m.movimentoAt DESC
    	""")
    	List<InventarioMovimento> findByMagazzinoAndRange(
    	        @Param("magazzinoId") Long magazzinoId,
    	        @Param("from") OffsetDateTime from,
    	        @Param("to") OffsetDateTime to
    	);
    
    int deleteByMovimentoAtBefore(OffsetDateTime cutoff);

}
