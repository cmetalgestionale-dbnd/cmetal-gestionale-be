package com.db.cmetal.be.controller;

import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.db.cmetal.be.entity.UtenteProdotto;
import com.db.cmetal.be.service.UtenteProdottoService;

import java.util.List;

@RestController
@RequestMapping("/api/utente-prodotti")
public class UtenteProdottoController {

    private final UtenteProdottoService service;

    public UtenteProdottoController(UtenteProdottoService service) {
        this.service = service;
    }

    @GetMapping("/{utenteId}")
    public List<UtenteProdotto> getByUtente(@PathVariable Long utenteId) {
        return service.getByUtente(utenteId);
    }

    @GetMapping("/{utenteId}/{prodottoId}")
    public ResponseEntity<UtenteProdotto> getOne(@PathVariable Long utenteId, @PathVariable Long prodottoId) {
        return service.getOne(utenteId, prodottoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{utenteId}/{prodottoId}")
    public UtenteProdotto setRiceveComanda(@PathVariable Long utenteId,
                                           @PathVariable Long prodottoId,
                                           @RequestBody RiceveComandaRequest body) {
        return service.setRiceveComanda(utenteId, prodottoId, body.isRiceveComanda());
    }

    @DeleteMapping("/{utenteId}/{prodottoId}")
    public ResponseEntity<Void> delete(@PathVariable Long utenteId, @PathVariable Long prodottoId) {
        service.deleteByUtenteAndProdotto(utenteId, prodottoId);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class RiceveComandaRequest {
        private boolean riceveComanda;
    }
}
