package com.db.cmetal.be.service;

import java.time.LocalDateTime;
import java.util.List;

import com.db.cmetal.be.dto.ResocontoDto;
import com.db.cmetal.be.entity.Sessione;
import com.db.cmetal.be.entity.Tavolo;

public interface SessioneService {
    List<Sessione> findAll();
    Sessione findById(Long id);
    Sessione save(Sessione sessione);
    Sessione update(Long id, Sessione sessione);
    void delete(Long id);
	byte[] generatePdfResoconto(Long id);
	List<ResocontoDto> getResoconto(Long id);
    Sessione findAttivaByTavolo(Tavolo tavolo);
	Sessione findAttivaById(Long sessioneId);
	List<Sessione> findByPeriodo(LocalDateTime inizio, LocalDateTime fine);
	List<Sessione> findEliminateByPeriodo(LocalDateTime inizio, LocalDateTime fine);
	Sessione findByIdDeleted(Long id);
}
