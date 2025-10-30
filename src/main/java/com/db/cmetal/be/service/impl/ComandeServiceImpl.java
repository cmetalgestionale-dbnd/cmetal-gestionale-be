package com.db.cmetal.be.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.cmetal.be.dto.OrdineDto;
import com.db.cmetal.be.entity.Ordine;
import com.db.cmetal.be.entity.Sessione;
import com.db.cmetal.be.mapper.ProdottoMapper;
import com.db.cmetal.be.repository.OrdineRepository;
import com.db.cmetal.be.repository.SessioneRepository;
import com.db.cmetal.be.repository.UtenteProdottoRepository;
import com.db.cmetal.be.service.ComandeService;
import com.db.cmetal.be.service.CucinaWebSocketService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComandeServiceImpl implements ComandeService {

	@Autowired
	UtenteProdottoRepository utenteProdottoRepository;
	
	@Autowired
	SessioneRepository sessioneRepository;
	
	@Autowired
	OrdineRepository ordineRepository;
	
	@Autowired
	CucinaWebSocketService cucinaWebSocketService;
	

	public List<OrdineDto> getComandeFiltrate(Long id, boolean soloAssegnati, boolean nascondiConsegnati) {
	    List<Long> productsList = soloAssegnati
	            ? utenteProdottoRepository.findProdottoIdsByUtenteIdAndRiceveComandaTrue(id)
	            : utenteProdottoRepository.findProdottoIdsByUtenteId(id);

	    // Lista sessioni attive non cancellate
	    List<Sessione> sessioneList = sessioneRepository.findByStatoIgnoreCaseAndIsDeletedFalse("ATTIVA");

	    // Poi tutti gli ordini filtrati restano uguali
	    List<Ordine> ordiniRaw = ordineRepository.findBySessioneIn(sessioneList);
	    

	    List<Ordine> ordiniFiltrati = ordiniRaw.stream()
	        .filter(o -> (!soloAssegnati || productsList.contains(o.getProdotto().getId())))
	        .filter(o -> !nascondiConsegnati || !o.getFlagConsegnato())
	        .toList();

	    return ordiniFiltrati.stream()
	        .map(o -> new OrdineDto(
	                o.getId(),
	                o.getSessione().getTavolo(),
	                ProdottoMapper.toDto(o.getProdotto()),
	                o.getQuantita(),
	                o.getOrario(),
	                o.getFlagConsegnato(),
	                o.getSessione() != null ? o.getSessione().getNumeroPartecipanti() : null
	        ))
	        .toList();
	}


	@Override
	public void toggleConsegnato(Long id, Map<String, Object> body) {
		ordineRepository.findById(id).ifPresent(ordine -> {
			boolean consegnato = Boolean.TRUE.equals(body.get("flagConsegnato"));

			cucinaWebSocketService.notifyConsegnaChanged(ordine.getId(), consegnato);

			ordine.setFlagConsegnato(consegnato);
			ordine.setStato(consegnato ? "CONSEGNATO" : "INVIATO");

			ordineRepository.save(ordine);
		});
	}
}
