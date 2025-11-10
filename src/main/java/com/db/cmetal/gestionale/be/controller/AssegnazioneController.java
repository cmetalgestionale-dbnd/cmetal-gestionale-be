package com.db.cmetal.gestionale.be.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.db.cmetal.gestionale.be.dto.AssegnazioneDto;
import com.db.cmetal.gestionale.be.entity.Allegato;
import com.db.cmetal.gestionale.be.entity.Assegnazione;
import com.db.cmetal.gestionale.be.entity.Utente;
import com.db.cmetal.gestionale.be.service.AssegnazioneService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assegnazioni")
@RequiredArgsConstructor
public class AssegnazioneController {

    private final AssegnazioneService assegnazioneService;

	@GetMapping
	public List<Assegnazione> getFiltered(@RequestParam(required = true) Long utenteId, @RequestParam(required = true) String date) {
		LocalDate localDate = LocalDate.parse(date);
		if (utenteId != null) {
			return assegnazioneService.getByUtenteAndData(utenteId, localDate);
		}
		return null;
	}


    @PostMapping
    public Assegnazione create(@RequestBody AssegnazioneDto dto) {
        Utente assegnatoDa = (Utente) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LocalDateTime assegnazioneAt = LocalDateTime.parse(dto.getAssegnazioneAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return assegnazioneService.createFromDto(dto, assegnatoDa.getId(), assegnazioneAt);
    }

    @PutMapping("/{id}")
    public Assegnazione update(@PathVariable Long id, @RequestBody AssegnazioneDto dto) {
        return assegnazioneService.updateFromDto(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        assegnazioneService.softDelete(id);
    }
    
    @PutMapping("/{id}/start")
    public Assegnazione startAssegnazione(@PathVariable Long id) {
        Utente utenteCorrente = (Utente) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return assegnazioneService.startAssegnazione(id, utenteCorrente.getId());
    }

    @PutMapping("/{id}/end")
    public Assegnazione endAssegnazione(@PathVariable Long id) {
        Utente utenteCorrente = (Utente) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return assegnazioneService.endAssegnazione(id, utenteCorrente.getId());
    }
    
    @PostMapping("/{id}/upload-foto")
    public Allegato uploadFoto(@PathVariable Long id, @RequestPart("file") MultipartFile file) throws Exception {
        Utente utenteCorrente = (Utente) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return assegnazioneService.uploadFoto(id, file, utenteCorrente.getId());
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> getFoto(@PathVariable Long id) throws Exception {
        return assegnazioneService.getFotoFile(id)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> generaReportPdf(@RequestParam(required = false) String data) throws Exception {
        LocalDate localDate;

        if (data != null && !data.isEmpty()) {
            localDate = LocalDate.parse(data);
        } else {
            localDate = LocalDate.now();
        }

        byte[] pdfBytes = assegnazioneService.generaReportPdf(localDate);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=report-assegnazioni-" + localDate + ".pdf")
                .header("Content-Type", "application/pdf")
                .body(pdfBytes);
    }

}
