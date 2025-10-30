package com.db.cmetal.be.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.db.cmetal.be.dto.UltimoOrdineDto;
import com.db.cmetal.be.entity.Prodotto;

public interface ProdottoRepository extends JpaRepository<Prodotto, Long> {
	
	@Query("""
		    SELECT new com.db.ayce.be.dto.UltimoOrdineDto(o.prodotto.nome, SUM(o.quantita))
		    FROM Ordine o
		    JOIN o.sessione s
		    WHERE o.orario >= :inizio 
		      AND o.orario < :fine
		      AND s.isDeleted = false
		    GROUP BY o.prodotto.nome
		    """)
		List<UltimoOrdineDto> getQuantitaOrdinataGiornata(
		        @Param("inizio") LocalDateTime inizio,
		        @Param("fine") LocalDateTime fine
		);


	List<Prodotto> findByIsDeletedFalse();

    // Optional: se vuoi findById filtrato
    Optional<Prodotto> findByIdAndIsDeletedFalse(Long id);
    
    // nuovo helper per verificare se esiste ed è attivo
    boolean existsByIdAndIsDeletedFalse(Long id);

	List<Prodotto> findByIsDeletedTrue();
}
