package com.db.cmetal.gestionale.be.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.db.cmetal.gestionale.be.entity.Magazzino;
import com.db.cmetal.gestionale.be.service.MagazzinoService;

@RestController
@RequestMapping("/api/magazzini")
public class MagazzinoController {

    private final MagazzinoService service;

    public MagazzinoController(MagazzinoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Magazzino> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Magazzino> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Magazzino create(@RequestBody Magazzino magazzino) {
        return service.save(magazzino);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Magazzino> update(@PathVariable Long id, @RequestBody Magazzino magazzino) {
        return service.findById(id)
                .map(existing -> {
                    magazzino.setId(id);
                    return ResponseEntity.ok(service.save(magazzino));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        return service.findById(id)
                .map(x -> {
                    service.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
