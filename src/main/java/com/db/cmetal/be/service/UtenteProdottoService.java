package com.db.cmetal.be.service;

import java.util.List;
import java.util.Optional;

import com.db.cmetal.be.entity.UtenteProdotto;

public interface UtenteProdottoService {
    List<UtenteProdotto> getByUtente(Long utenteId);
    UtenteProdotto setRiceveComanda(Long utenteId, Long prodottoId, boolean riceveComanda);
    Optional<UtenteProdotto> getOne(Long utenteId, Long prodottoId);
    void deleteByUtenteAndProdotto(Long utenteId, Long prodottoId);
}
