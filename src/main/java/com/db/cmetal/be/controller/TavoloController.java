package com.db.cmetal.be.controller;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.cmetal.be.entity.Ordine;
import com.db.cmetal.be.entity.Sessione;
import com.db.cmetal.be.entity.Tavolo;
import com.db.cmetal.be.service.SessioneService;
import com.db.cmetal.be.service.TavoloService;
import com.db.cmetal.be.utils.Constants;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tavoli")
@RequiredArgsConstructor
public class TavoloController {

    private final TavoloService tavoloService;
    private final SessioneService sessioneService;

    @GetMapping
    public List<Tavolo> getAllTavoli() {
        return tavoloService.findAll();
    }

    @GetMapping("/{id}")
    public Tavolo getTavoloById(@PathVariable Integer id) {
        return tavoloService.findById(id);
    }

    @PostMapping
    public Tavolo createTavolo(@RequestBody Tavolo tavolo) {
        return tavoloService.save(tavolo);
    }

    @PutMapping("/{id}")
    public Tavolo updateTavolo(@PathVariable Integer id, @RequestBody Tavolo tavolo) {
        return tavoloService.update(id, tavolo);
    }

    @DeleteMapping("/{id}")
    public void deleteTavolo(@PathVariable Integer id) {
        tavoloService.delete(id);
    }
    
    @GetMapping("/{id}/ordini")
    public List<Ordine> getOrdiniByTavolo(@PathVariable Integer tavoloId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof Claims claims && Constants.ROLE_CLIENT.equals(claims.get("role", String.class))) {
            Long sessioneId = claims.get("sessioneId", Long.class);
            Sessione sessione = sessioneService.findById(sessioneId);

            if (sessione == null || !"ATTIVA".equals(sessione.getStato())) {
                return null;
            } else {
            	return tavoloService.findBySessione(sessione.getId());
            }
        }
		return null;
    }

}
