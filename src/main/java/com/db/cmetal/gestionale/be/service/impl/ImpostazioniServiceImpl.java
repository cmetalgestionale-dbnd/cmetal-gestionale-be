package com.db.cmetal.gestionale.be.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.cmetal.gestionale.be.entity.Impostazioni;
import com.db.cmetal.gestionale.be.repository.ImpostazioniRepository;
import com.db.cmetal.gestionale.be.service.ImpostazioniService;

@Service
@Transactional
public class ImpostazioniServiceImpl implements ImpostazioniService {

    private final ImpostazioniRepository repo;

    public ImpostazioniServiceImpl(ImpostazioniRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Impostazioni> findAll() {
        return repo.findAll();
    }

    @Override
    public Optional<Impostazioni> findByChiave(String chiave) {
        return repo.findById(chiave);
    }

    @Override
    public Impostazioni save(Impostazioni i) {
        return repo.save(i);
    }

    @Override
    public Impostazioni update(String chiave, String valore) {
        return repo.findById(chiave)
            .map(i -> {
                i.setValore(valore);
                return repo.save(i);
            })
            .orElse(null);
    }

    @Override
    public Integer getIntValue(String chiave, int defaultValue) {
        try {
            return Integer.parseInt(repo.findById(chiave)
                    .map(Impostazioni::getValore)
                    .orElseThrow());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBooleanValue(String chiave, boolean defaultValue) {
        try {
            return !"0".equals(repo.findById(chiave)
                    .map(Impostazioni::getValore)
                    .orElse("0"));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public Double getDoubleValue(String chiave, double defaultValue) {
        try {
            return Double.parseDouble(repo.findById(chiave)
                    .map(Impostazioni::getValore)
                    .orElseThrow());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
