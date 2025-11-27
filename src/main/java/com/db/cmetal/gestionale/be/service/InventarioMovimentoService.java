package com.db.cmetal.gestionale.be.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.db.cmetal.gestionale.be.entity.InventarioMovimento;

public interface InventarioMovimentoService {
    List<InventarioMovimento> findAll();
    Optional<InventarioMovimento> findById(Long id);
    List<InventarioMovimento> findByArticolo(Long articoloId);
    InventarioMovimento save(InventarioMovimento movimento);
    void deleteById(Long id);
    List<InventarioMovimento> findByArticoloAndRange(Long articoloId, OffsetDateTime from, OffsetDateTime to);
    List<InventarioMovimento> findByMagazzinoAndRange(Long magazzinoId, OffsetDateTime from, OffsetDateTime to);
    byte[] generaReportPdfMagazzino(Long magazzinoId, OffsetDateTime from, OffsetDateTime to) throws Exception;

}
