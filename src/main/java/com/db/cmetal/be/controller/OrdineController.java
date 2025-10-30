package com.db.cmetal.be.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.db.cmetal.be.entity.Ordine;
import com.db.cmetal.be.entity.Prodotto;
import com.db.cmetal.be.entity.Sessione;
import com.db.cmetal.be.service.CucinaWebSocketService;
import com.db.cmetal.be.service.OrdineService;
import com.db.cmetal.be.service.ProdottoService;
import com.db.cmetal.be.service.SessioneService;
import com.db.cmetal.be.utils.AuthUtils;
import com.db.cmetal.be.utils.Constants;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ordini")
@RequiredArgsConstructor
public class OrdineController {

	@Autowired
	OrdineService ordineService;
	
	@Autowired
	SessioneService sessioneService;
	
	@Autowired
	ProdottoService prodottoService;
	
	@Autowired
	CucinaWebSocketService cucinaWebSocketService;
	
	@Autowired
	AuthUtils authUtils;

	@GetMapping
	public List<Ordine> getAllOrdini() {
		authUtils.getCurrentUserOrThrow(Constants.ROLE_DIPEN, Constants.ROLE_ADMIN);
		return ordineService.findAll();
	}

	@GetMapping("/{id}")
	public Ordine getOrdineById(@PathVariable Long id) {
		authUtils.getCurrentUserOrThrow(Constants.ROLE_CLIENT, Constants.ROLE_DIPEN, Constants.ROLE_ADMIN);
		return ordineService.findById(id);
	}

	@PostMapping
	public Ordine createOrdine(@RequestBody Ordine ordine) {
		authUtils.getCurrentUserOrThrow(Constants.ROLE_DIPEN, Constants.ROLE_ADMIN);
		return ordineService.save(ordine);
	}

	@PutMapping("/{id}")
	public Ordine updateOrdine(@PathVariable Long id, @RequestBody Ordine ordine) {
		authUtils.getCurrentUserOrThrow(Constants.ROLE_DIPEN, Constants.ROLE_ADMIN);
		return ordineService.update(id, ordine);
	}

	@DeleteMapping("/{id}")
	public void deleteOrdine(@PathVariable Long id) {
		authUtils.getCurrentUserOrThrow(Constants.ROLE_DIPEN, Constants.ROLE_ADMIN);
		ordineService.delete(id);
        cucinaWebSocketService.notifyNewOrder(null);
	}
	
    @GetMapping("/storico/{sessioneId}")
    public List<Ordine> getOrdiniBySessione(@PathVariable Long sessioneId) {
        Sessione sessione = sessioneService.findById(sessioneId);
        if (sessione == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sessione non trovata");
        return ordineService.findBySessione(sessione);
    }
    
	@PostMapping("/add-resoconto")
	public Ordine createOrdineForResoconto(@RequestBody Ordine ordine) {
		authUtils.getCurrentUserOrThrow(Constants.ROLE_DIPEN, Constants.ROLE_ADMIN);
		Sessione sessione = sessioneService.findById(ordine.getSessione().getId());

	    Prodotto prodotto = prodottoService.findById(ordine.getProdotto().getId());

	    if (sessione.getIsAyce() && prodotto.getCategoria().getId() < 100L) {
	        ordine.setPrezzoUnitario(0.0);
	    }

	    ordine.setSessione(sessione);
	    ordine.setProdotto(prodotto);
	    
	    // FORZA la data server-side: non fidarti di quella del client
        if (ordine.getOrario() == null) {
            ordine.setOrario(LocalDateTime.now());
        } else {
            // opzionale: IGNORA sempre il valore client per sicurezza
            ordine.setOrario(LocalDateTime.now());
        }

	    Ordine o = ordineService.save(ordine);
	    cucinaWebSocketService.notifyNewOrder(null);
	    return o;
	}
}
