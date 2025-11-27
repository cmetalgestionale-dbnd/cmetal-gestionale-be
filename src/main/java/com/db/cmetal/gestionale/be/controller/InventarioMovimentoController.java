package com.db.cmetal.gestionale.be.controller;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.db.cmetal.gestionale.be.entity.InventarioMovimento;
import com.db.cmetal.gestionale.be.service.InventarioMovimentoService;

@RestController
@RequestMapping("/api/inventario/movimenti")
public class InventarioMovimentoController {

    private final InventarioMovimentoService service;

    public InventarioMovimentoController(InventarioMovimentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<InventarioMovimento> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioMovimento> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

//    @GetMapping("/articolo/{articoloId}")
//    public List<InventarioMovimento> getByArticolo(@PathVariable Long articoloId) {
//        return service.findByArticolo(articoloId);
//    }

    @PostMapping
    public InventarioMovimento create(@RequestBody InventarioMovimento movimento) {
        return service.save(movimento);
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
    
    //articolo + range opzionale
    @GetMapping("/articolo/{articoloId}")
    public List<InventarioMovimento> getByArticolo(
            @PathVariable Long articoloId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        // se non ci sono filtri → comportamento vecchio
        if (from == null && to == null) {
            return service.findByArticolo(articoloId);
        }

        var range = buildRange(from, to);
        return service.findByArticoloAndRange(articoloId, range.from, range.to);
    }

    //nuovo: magazzino + range opzionale
    @GetMapping("/magazzino/{magazzinoId}")
    public List<InventarioMovimento> getByMagazzino(
            @PathVariable Long magazzinoId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        var range = buildRange(from, to);
        return service.findByMagazzinoAndRange(magazzinoId, range.from, range.to);
    }
    
    private Range buildRange(String from, String to) {
        ZoneId zone = ZoneId.systemDefault();

        LocalDate fromDate = (from != null)
                ? LocalDate.parse(from)
                : LocalDate.of(1970, 1, 1);
        LocalDate toDate = (to != null)
                ? LocalDate.parse(to)
                : LocalDate.of(2100, 1, 1);

        Range r = new Range();
        r.from = fromDate.atStartOfDay(zone).toOffsetDateTime();
        r.to   = toDate.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

        return r;
    }

    private static class Range {
        OffsetDateTime from;
        OffsetDateTime to;
    }

    @GetMapping("/magazzino/{magazzinoId}/report/pdf")
    public ResponseEntity<byte[]> generaReportPdfMagazzino(
            @PathVariable Long magazzinoId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) throws Exception {

        var range = buildRange(from, to);

        byte[] pdfBytes = service.generaReportPdfMagazzino(
                magazzinoId,
                range.from,
                range.to
        );

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=report-magazzino-" + magazzinoId + ".pdf")
                .header("Content-Type", "application/pdf")
                .body(pdfBytes);
    }



}
