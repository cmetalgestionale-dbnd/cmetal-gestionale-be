package com.db.cmetal.be.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.db.cmetal.be.entity.Impostazioni;
import com.db.cmetal.be.service.ImpostazioniService;

import java.util.List;

@RestController
@RequestMapping("/api/impostazioni")
public class ImpostazioniController {

    private final ImpostazioniService service;

    public ImpostazioniController(ImpostazioniService service) {
        this.service = service;
    }

    @GetMapping
    public List<Impostazioni> getAll() {
        return service.findAll();
    }

    @GetMapping("/{chiave}")
    public ResponseEntity<Impostazioni> getByChiave(@PathVariable String chiave) {
        return service.findByChiave(chiave)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Impostazioni create(@RequestBody Impostazioni Impostazioni) {
        return service.save(Impostazioni);
    }

    @PutMapping("/{chiave}")
    public ResponseEntity<Impostazioni> update(@PathVariable String chiave, @RequestBody String valore) {
        Impostazioni updated = service.update(chiave, valore);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
}
