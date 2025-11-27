package com.db.cmetal.gestionale.be.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.db.cmetal.gestionale.be.entity.InventarioArticolo;
import com.db.cmetal.gestionale.be.service.InventarioArticoloService;

@RestController
@RequestMapping("/api/inventario/articoli")
public class InventarioArticoloController {

    private final InventarioArticoloService service;

    public InventarioArticoloController(InventarioArticoloService service) {
        this.service = service;
    }

    @GetMapping
    public List<InventarioArticolo> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioArticolo> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public InventarioArticolo create(@RequestBody InventarioArticolo articolo) {
        return service.save(articolo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventarioArticolo> update(@PathVariable Long id, @RequestBody InventarioArticolo articolo) {
        return service.findById(id)
                .map(existing -> {
                    articolo.setId(id);
                    return ResponseEntity.ok(service.save(articolo));
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
