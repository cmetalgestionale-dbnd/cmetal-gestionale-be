package com.db.cmetal.be.service;

import java.util.List;
import java.util.Optional;

import com.db.cmetal.be.entity.Impostazioni;

public interface ImpostazioniService {

    List<Impostazioni> findAll();

    Optional<Impostazioni> findByChiave(String chiave);

    Impostazioni save(Impostazioni Impostazioni);

    Impostazioni update(String chiave, String valore);

	Integer getIntValue(String string, int i);
	
	boolean getBooleanValue(String chiave, boolean defaultValue);

	Double getDoubleValue(String chiave, double defaultValue);
}
