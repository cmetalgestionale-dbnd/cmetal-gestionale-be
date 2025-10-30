package com.db.cmetal.be.service;

import java.time.LocalDateTime;
import java.util.List;

import com.db.cmetal.be.dto.UltimoOrdineDto;
import com.db.cmetal.be.entity.Prodotto;

public interface ProdottoService {
    List<Prodotto> findAll();
    Prodotto findById(Long id);
    Prodotto save(Prodotto prodotto);
    Prodotto update(Long id, Prodotto prodotto);
    void delete(Long id);
    List<UltimoOrdineDto> getProdottiUtilizzatiUltimoServizio(LocalDateTime inizio, LocalDateTime fine);
	List<Prodotto> findDeleted();
	Prodotto restore(Long id);
    	
}
