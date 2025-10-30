package com.db.cmetal.be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.db.cmetal.be.entity.UtenteProdotto;
import com.db.cmetal.be.entity.UtenteProdottoId;

public interface UtenteProdottoRepository extends JpaRepository<UtenteProdotto, UtenteProdottoId> {
    List<UtenteProdotto> findById_UtenteId(Long utenteId);
    
    @Query("SELECT up.id.prodottoId FROM UtenteProdotto up " +
    	       "WHERE up.id.utenteId = :utenteId AND up.riceveComanda = true " +
    	       "AND up.prodotto.isDeleted = false")
    	List<Long> findProdottoIdsByUtenteIdAndRiceveComandaTrue(Long utenteId);

    	@Query("SELECT up.id.prodottoId FROM UtenteProdotto up " +
    	       "WHERE up.id.utenteId = :utenteId AND up.prodotto.isDeleted = false")
    	List<Long> findProdottoIdsByUtenteId(Long utenteId);


}
