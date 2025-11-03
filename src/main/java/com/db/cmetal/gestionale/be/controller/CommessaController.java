package com.db.cmetal.gestionale.be.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.db.cmetal.gestionale.be.dto.CommessaDto;
import com.db.cmetal.gestionale.be.entity.Allegato;
import com.db.cmetal.gestionale.be.entity.Commessa;
import com.db.cmetal.gestionale.be.entity.Utente;
import com.db.cmetal.gestionale.be.repository.AllegatoRepository;
import com.db.cmetal.gestionale.be.service.CommessaService;
import com.db.cmetal.gestionale.be.service.SupabaseS3Service;

@RestController
@RequestMapping("/api/commesse")
public class CommessaController {

    private final CommessaService commessaService;
    private final SupabaseS3Service s3Service;
    private final AllegatoRepository allegatoRepository;

    public CommessaController(CommessaService commessaService,
                              SupabaseS3Service s3Service,
                              AllegatoRepository allegatoRepository) {
        this.commessaService = commessaService;
        this.s3Service = s3Service;
        this.allegatoRepository = allegatoRepository;
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
    	
        Commessa commessa = new Commessa();
        commessa.setCodice(dto.codice);
        commessa.setDescrizione(dto.descrizione);

        if (file != null && !file.isEmpty()) {
            String path = "commesse/" + java.util.UUID.randomUUID() + "_" + file.getOriginalFilename();
            s3Service.uploadFile(file, path);

            Allegato allegato = new Allegato();
            allegato.setNomeFile(file.getOriginalFilename());
            allegato.setTipoFile(file.getContentType());
            allegato.setStoragePath(path);
            allegato.setCreatedAt(LocalDateTime.now());
            allegato.setCreatedBy(user);
            allegato.setIsDeleted(false);
            allegatoRepository.save(allegato);

            commessa.setPdfAllegato(allegato);
        }

        return commessaService.saveCommessa(commessa, user);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Commessa updateCommessa(
            @PathVariable Long id,
            @RequestPart("commessa") CommessaDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "removeFile", required = false) Boolean removeFile) throws Exception {

    	Utente user = (Utente) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	
        Commessa existing = commessaService.getCommessaById(id)
                .orElseThrow(() -> new RuntimeException("Commessa non trovata"));

        existing.setCodice(dto.codice);
        existing.setDescrizione(dto.descrizione);

        if (Boolean.TRUE.equals(removeFile) && existing.getPdfAllegato() != null) {
            Allegato a = existing.getPdfAllegato();
            a.setIsDeleted(true);
            allegatoRepository.save(a);
            existing.setPdfAllegato(null);
        }

        if (file != null && !file.isEmpty()) {
            if (existing.getPdfAllegato() != null) {
                Allegato old = existing.getPdfAllegato();
                old.setIsDeleted(true);
                allegatoRepository.save(old);
            }
            String path = "commesse/" + java.util.UUID.randomUUID() + "_" + file.getOriginalFilename();
            s3Service.uploadFile(file, path);

            Allegato newA = new Allegato();
            newA.setNomeFile(file.getOriginalFilename());
            newA.setTipoFile(file.getContentType());
            newA.setStoragePath(path);
            newA.setCreatedAt(LocalDateTime.now());
            newA.setCreatedBy(user);
            newA.setIsDeleted(false);
            allegatoRepository.save(newA);

            existing.setPdfAllegato(newA);
        }

        return commessaService.updateCommessa(id, existing);
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
        Commessa c = commessaService.getCommessaById(id)
                .orElseThrow(() -> new RuntimeException("Commessa non trovata"));
        if (c.getPdfAllegato() == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(s3Service.getPublicUrl(c.getPdfAllegato().getStoragePath()));
    }
    
    @GetMapping("/{id}/allegato")
    public ResponseEntity<byte[]> getAllegato(@PathVariable Long id) throws Exception {
        Commessa c = commessaService.getCommessaById(id)
                .orElseThrow(() -> new RuntimeException("Commessa non trovata"));

        Allegato allegato = c.getPdfAllegato();
        if (allegato == null) return ResponseEntity.notFound().build();

        // Recupera il file dal bucket
        byte[] fileBytes = s3Service.downloadFile(allegato.getStoragePath());

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + allegato.getNomeFile() + "\"")
                .contentType(MediaType.parseMediaType(allegato.getTipoFile()))
                .body(fileBytes);
    }

}
