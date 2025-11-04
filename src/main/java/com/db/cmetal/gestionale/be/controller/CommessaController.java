package com.db.cmetal.gestionale.be.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.db.cmetal.gestionale.be.dto.CommessaDto;
import com.db.cmetal.gestionale.be.entity.Commessa;
import com.db.cmetal.gestionale.be.entity.Utente;
import com.db.cmetal.gestionale.be.service.CommessaService;

@RestController
@RequestMapping("/api/commesse")
public class CommessaController {

    private final CommessaService commessaService;

    public CommessaController(CommessaService commessaService) {
        this.commessaService = commessaService;
    }

    @GetMapping
    public List<Commessa> getAllCommesse() {
        return commessaService.getAllCommesse();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Commessa createCommessa(
            @RequestPart("commessa") CommessaDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {

        Utente user = (Utente) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return commessaService.createCommessa(dto, file, user);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Commessa updateCommessa(
            @PathVariable Long id,
            @RequestPart("commessa") CommessaDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "removeFile", required = false) Boolean removeFile) throws Exception {

        Utente user = (Utente) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return commessaService.updateCommessaWithFile(id, dto, file, removeFile, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        commessaService.deleteCommessa(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreCommessa(@PathVariable Long id) {
        commessaService.restoreCommessa(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/allegato/url")
    public ResponseEntity<String> getAllegatoUrl(@PathVariable Long id) {
        return commessaService.getAllegatoUrl(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/allegato")
    public ResponseEntity<byte[]> getAllegato(@PathVariable Long id) throws Exception {
        return commessaService.getAllegatoFile(id)
                .orElse(ResponseEntity.notFound().build());
    }

}
