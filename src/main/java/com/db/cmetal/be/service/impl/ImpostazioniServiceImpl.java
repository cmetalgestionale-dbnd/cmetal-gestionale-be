package com.db.cmetal.be.service.impl;

import org.springframework.stereotype.Service;

import com.db.cmetal.be.entity.Impostazioni;
import com.db.cmetal.be.repository.ImpostazioniRepository;
import com.db.cmetal.be.service.ImpostazioniService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ImpostazioniServiceImpl implements ImpostazioniService {

    private final ImpostazioniRepository repo;
    private final Map<String, Impostazioni> cache = new ConcurrentHashMap<>();

    public ImpostazioniServiceImpl(ImpostazioniRepository repo) {
        this.repo = repo;
        refreshCache();
    }

    /** Ricarica tutte le impostazioni dal DB nella cache */
    public void refreshCache() {
        repo.findAll().forEach(i -> cache.put(i.getChiave(), i));
    }

    @Override
    public List<Impostazioni> findAll() {
        return repo.findAll();
    }

    @Override
    public Optional<Impostazioni> findByChiave(String chiave) {
        return Optional.ofNullable(cache.get(chiave));
    }

    @Override
    public Impostazioni save(Impostazioni i) {
        Impostazioni saved = repo.save(i);
        cache.put(i.getChiave(), saved);
        return saved;
    }

    @Override
    public Impostazioni update(String chiave, String valore) {
        return repo.findById(chiave)
            .map(i -> {
                i.setValore(valore);
                Impostazioni saved = repo.save(i);
                cache.put(chiave, saved);
                return saved;
            })
            .orElse(null);
    }

    @Override
    public Integer getIntValue(String chiave, int defaultValue) {
        try {
            return Integer.parseInt(cache.get(chiave).getValore());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBooleanValue(String chiave, boolean defaultValue) {
        try {
            return !"0".equals(cache.get(chiave).getValore());
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    @Override
    public Double getDoubleValue(String chiave, double defaultValue) {
        try {
            return Double.parseDouble(cache.get(chiave).getValore());
        } catch (Exception e) {
            return defaultValue;
        }
    }

}
