package com.db.cmetal.gestionale.be.service;

import com.db.cmetal.gestionale.be.entity.Commessa;
import com.db.cmetal.gestionale.be.entity.Utente;

import java.util.List;
import java.util.Optional;

public interface CommessaService {
    Commessa saveCommessa(Commessa commessa, Utente user);
    List<Commessa> getAllCommesse();
    Optional<Commessa> getCommessaById(Long id);
    Optional<Commessa> getCommessaByCodice(String codice);
    Commessa updateCommessa(Long id, Commessa commessa);
    void deleteCommessa(Long id);
	void hardDeleteCommessa(Long id);
	void restoreCommessa(Long id);
}
