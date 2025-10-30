package com.db.cmetal.be.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.db.cmetal.be.service.TavoloTempService;

@Service
public class TavoloTempServiceImpl implements TavoloTempService {

    // TavoloId -> (ProdottoId -> Quantità)
    private final Map<Integer, Map<Long, Integer>> ordineTemp = new ConcurrentHashMap<>();

    @Override
    public void addItem(Integer tavoloId, Long prodottoId, int quantita) {
        ordineTemp.computeIfAbsent(tavoloId, k -> new ConcurrentHashMap<>())
                  .merge(prodottoId, quantita, Integer::sum);
    }

    @Override
    public void removeItem(Integer tavoloId, Long prodottoId, int quantita) {
        Map<Long, Integer> tavoloOrdine = ordineTemp.get(tavoloId);
        if (tavoloOrdine != null) {
            tavoloOrdine.merge(prodottoId, -quantita, Integer::sum);
            tavoloOrdine.entrySet().removeIf(e -> e.getValue() <= 0);
        }
    }

    @Override
    public Map<Long, Integer> getOrdineTemp(Integer tavoloId) {
        return ordineTemp.getOrDefault(tavoloId, Map.of());
    }

    @Override
    public void clearOrdine(Integer tavoloId) {
        ordineTemp.remove(tavoloId);
    }

    @Override
    public int getTotalePortate(Integer tavoloId) {
        return ordineTemp.getOrDefault(tavoloId, Map.of())
                         .values()
                         .stream()
                         .mapToInt(Integer::intValue)
                         .sum();
    }
}
