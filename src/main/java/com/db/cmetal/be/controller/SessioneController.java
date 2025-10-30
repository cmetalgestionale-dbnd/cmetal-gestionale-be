package com.db.cmetal.be.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.db.cmetal.be.dto.ResocontoDto;
import com.db.cmetal.be.entity.Sessione;
import com.db.cmetal.be.entity.Tavolo;
import com.db.cmetal.be.service.CucinaWebSocketService;
import com.db.cmetal.be.service.SessioneService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sessioni")
@RequiredArgsConstructor
public class SessioneController {

    private final SessioneService sessioneService;
    private final TavoloWebSocketController tavoloWebSocketController;
    private final CucinaWebSocketService cucinaWebSocketService;

    @GetMapping
    public List<Sessione> getAllSessioni() {
        return sessioneService.findAll();
    }

    @GetMapping("/{id}")
    public Sessione getSessioneById(@PathVariable Long id) {
        return sessioneService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Sessione> createSessione(@RequestBody Sessione sessione) {
        Tavolo tavolo = sessione.getTavolo();
        Sessione attiva = sessioneService.findAttivaByTavolo(tavolo);
        if (attiva != null) {
            return ResponseEntity.status(409).build();
        }

        // FORZA la data server-side: non fidarti di quella del client
        if (sessione.getOrarioInizio() == null) {
            sessione.setOrarioInizio(LocalDateTime.now());
        } else {
            // opzionale: IGNORA sempre il valore client per sicurezza
            sessione.setOrarioInizio(LocalDateTime.now());
        }

        Sessione nuova = sessioneService.save(sessione);
        return ResponseEntity.ok(nuova);
    }

    @PutMapping("/{id}")
    public Sessione updateSessione(@PathVariable Long id, @RequestBody Sessione sessione) {
        return sessioneService.update(id, sessione);
    }

    @DeleteMapping("/{id}")
    public void deleteSessione(@PathVariable Long id) {
        sessioneService.delete(id);
    }
    
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getSessioneResocontoPdfById(@PathVariable Long id) {
        byte[] pdfBytes = sessioneService.generatePdfResoconto(id);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=sessione_" + id + ".pdf")
                .body(pdfBytes);
    }
    
    
    @GetMapping("/{id}/resoconto")
    public List<ResocontoDto> getSessioneResocontoById(@PathVariable Long id) {
        return sessioneService.getResoconto(id);
    }
    
    @PutMapping("/{id}/disattiva")
    public Sessione disattivaSessione(@PathVariable Long id, @RequestBody Sessione sessione) {
        Sessione s = sessioneService.update(id, sessione);
        tavoloWebSocketController.clearTavoloQueue(sessione.getTavolo().getId());
        cucinaWebSocketService.notifyNewOrder(null);
        return s;
    }
    
    @PutMapping("/{id}/reset-timer")
    public Sessione resetTimer(@PathVariable Long id, @RequestBody Sessione sessione) {
        Sessione s = sessioneService.update(id, sessione);
        tavoloWebSocketController.sendRefresh(sessione.getTavolo().getId());
        return s;
    }
    
    @GetMapping("/by-day")
    public List<Sessione> getSessioniByDay(@RequestParam String data) {
        // data in formato YYYY-MM-DD
        LocalDate giorno = LocalDate.parse(data);
        LocalDateTime inizio = giorno.atStartOfDay();
        LocalDateTime fine = giorno.plusDays(1).atStartOfDay();

        return sessioneService.findByPeriodo(inizio, fine);
    }
    
    @PutMapping("/{id}/ripristina")
    public ResponseEntity<Sessione> ripristina(@PathVariable Long id) {
        Sessione s = sessioneService.findByIdDeleted(id);
        if (s == null) return ResponseEntity.notFound().build();
        s.setIsDeleted(false);
        sessioneService.save(s);
        return ResponseEntity.ok(s);
    }
    
    @GetMapping("/eliminate/by-day")
    public List<Sessione> getSessioniEliminateByDay(@RequestParam String data) {
        LocalDate giorno = LocalDate.parse(data);
        LocalDateTime inizio = giorno.atStartOfDay();
        LocalDateTime fine = giorno.plusDays(1).atStartOfDay();
        return sessioneService.findEliminateByPeriodo(inizio, fine);
    }


}
