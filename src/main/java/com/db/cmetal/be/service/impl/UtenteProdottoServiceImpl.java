package com.db.cmetal.be.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.cmetal.be.entity.UtenteProdotto;
import com.db.cmetal.be.entity.UtenteProdottoId;
import com.db.cmetal.be.repository.UtenteProdottoRepository;
import com.db.cmetal.be.service.ProdottoService;
import com.db.cmetal.be.service.UtenteProdottoService;
import com.db.cmetal.be.service.UtenteService;

@Service
public class UtenteProdottoServiceImpl implements UtenteProdottoService {

    private final UtenteProdottoRepository repository;
    private final UtenteService utenteService;
    private final ProdottoService prodottoService;

    public UtenteProdottoServiceImpl(UtenteProdottoRepository repository, UtenteService utenteService, ProdottoService prodottoService) {
        this.repository = repository;
		this.utenteService = utenteService;
		this.prodottoService = prodottoService;
    }

    @Override
    public List<UtenteProdotto> getByUtente(Long utenteId) {
        return repository.findById_UtenteId(utenteId);
    }

    @Override
    public Optional<UtenteProdotto> getOne(Long utenteId, Long prodottoId) {
        return repository.findById(new UtenteProdottoId(utenteId, prodottoId));
    }

    @Transactional
    @Override
    public UtenteProdotto setRiceveComanda(Long utenteId, Long prodottoId, boolean riceveComanda) {

        UtenteProdottoId id = new UtenteProdottoId(utenteId, prodottoId);

        UtenteProdotto up = repository.findById(id).orElseGet(() -> {
            UtenteProdotto nuovo = new UtenteProdotto();
            nuovo.setId(id);
            nuovo.setRiceveComanda(riceveComanda);
            // assegna le entità correlate
            nuovo.setUtente(utenteService.findById(utenteId)
                    .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + utenteId)));
            nuovo.setProdotto(prodottoService.findById(prodottoId));
            return nuovo;
        });

        up.setRiceveComanda(riceveComanda);
        return repository.save(up);
    }

    @Transactional
    @Override
    public void deleteByUtenteAndProdotto(Long utenteId, Long prodottoId) {
        repository.deleteById(new UtenteProdottoId(utenteId, prodottoId));
    }
}
