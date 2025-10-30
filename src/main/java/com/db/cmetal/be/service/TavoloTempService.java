package com.db.cmetal.be.service;

import java.util.Map;

public interface TavoloTempService {

    void addItem(Integer tavoloId, Long prodottoId, int quantita);

    void removeItem(Integer tavoloId, Long prodottoId, int quantita);

    Map<Long, Integer> getOrdineTemp(Integer tavoloId);

    void clearOrdine(Integer tavoloId);

    int getTotalePortate(Integer tavoloId);
}
