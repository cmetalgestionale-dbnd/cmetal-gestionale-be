package com.db.cmetal.be.service;

import java.util.List;

import com.db.cmetal.be.entity.CostoProdotto;

public interface CostoProdottoService {
    List<CostoProdotto> getAll();
    CostoProdotto saveOrUpdate(Long prodottoId, Double costo);
}

