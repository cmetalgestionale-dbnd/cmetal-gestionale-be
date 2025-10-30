package com.db.cmetal.be.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.db.cmetal.be.dto.UltimoOrdineDto;
import com.db.cmetal.be.entity.Prodotto;
import com.db.cmetal.be.repository.ProdottoRepository;
import com.db.cmetal.be.service.ProdottoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdottoServiceImpl implements ProdottoService {

    private final ProdottoRepository prodottoRepository;

    @Override
    public List<Prodotto> findAll() {
        return prodottoRepository.findByIsDeletedFalse();
    }

    @Override
    public Prodotto findById(Long id) {
        return prodottoRepository.findByIdAndIsDeletedFalse(id)
                .orElse(null);
    }

    @Override
    public Prodotto save(Prodotto prodotto) {
        return prodottoRepository.save(prodotto);
    }

    @Override
    public Prodotto update(Long id, Prodotto prodotto) {
        prodotto.setId(id);
        return prodottoRepository.save(prodotto);
    }

    @Override
    public void delete(Long id) {
        Prodotto p = findById(id);
        if (p != null) {
            p.setIsDeleted(true);
            prodottoRepository.save(p);
        }
    }

	@Override
	public List<UltimoOrdineDto> getProdottiUtilizzatiUltimoServizio(LocalDateTime inizio, LocalDateTime fine) {
	    return prodottoRepository.getQuantitaOrdinataGiornata(inizio, fine);
	}

	@Override
	public List<Prodotto> findDeleted() {
	    return prodottoRepository.findByIsDeletedTrue();
	}

	@Override
	public Prodotto restore(Long id) {
	    Prodotto p = prodottoRepository.findById(id).orElseThrow(() -> new RuntimeException("Prodotto non trovato"));
	    p.setIsDeleted(false);
	    return prodottoRepository.save(p);
	}


}
