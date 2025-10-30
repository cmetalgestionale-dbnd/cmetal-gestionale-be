package com.db.cmetal.be.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.cmetal.be.entity.CostoProdotto;
import com.db.cmetal.be.entity.Prodotto;
import com.db.cmetal.be.repository.CostoProdottoRepository;
import com.db.cmetal.be.repository.ProdottoRepository;
import com.db.cmetal.be.service.CostoProdottoService;

@Service
@Transactional
public class CostoProdottoServiceImpl implements CostoProdottoService {

    @Autowired
    private CostoProdottoRepository costoProdottoRepository;

    @Autowired
    private ProdottoRepository prodottoRepository;

    @Override
    public List<CostoProdotto> getAll() {
        return costoProdottoRepository.findAll()
                .stream()
                .filter(cp -> cp.getProdotto() != null && !cp.getProdotto().getIsDeleted())
                .toList();
    }


    @Override
    public CostoProdotto saveOrUpdate(Long prodottoId, Double costo) {
        Optional<CostoProdotto> existing = costoProdottoRepository.findByProdottoId(prodottoId);

        Prodotto prodotto = prodottoRepository.findById(prodottoId)
                .orElseThrow(() -> new RuntimeException("Prodotto non trovato"));

        if (prodotto.getIsDeleted()) {
            throw new RuntimeException("Impossibile modificare un prodotto cancellato");
        }

        CostoProdotto cp;
        if (existing.isPresent()) {
            cp = existing.get();
            cp.setCosto(costo);
        } else {
            cp = new CostoProdotto();
            cp.setProdotto(prodotto);
            cp.setCosto(costo);
        }

        return costoProdottoRepository.save(cp);
    }
}
