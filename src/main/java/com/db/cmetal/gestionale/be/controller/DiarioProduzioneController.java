package com.db.cmetal.gestionale.be.controller;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
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
import org.springframework.web.bind.annotation.RestController;

import com.db.cmetal.gestionale.be.dto.DiarioProduzioneDto;
import com.db.cmetal.gestionale.be.entity.Commessa;
import com.db.cmetal.gestionale.be.entity.DiarioProduzione;
import com.db.cmetal.gestionale.be.entity.Utente;
import com.db.cmetal.gestionale.be.repository.CommessaRepository;
import com.db.cmetal.gestionale.be.repository.DiarioProduzioneRepository;
import com.db.cmetal.gestionale.be.repository.ImpostazioniRepository;
import com.db.cmetal.gestionale.be.repository.UtenteRepository;
import com.db.cmetal.gestionale.be.service.WebSocketService;
import com.db.cmetal.gestionale.be.utils.Constants;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/diario-produzione")
@RequiredArgsConstructor
public class DiarioProduzioneController {

    private final DiarioProduzioneRepository repository;
    private final CommessaRepository commessaRepository;
    private final UtenteRepository utenteRepository;
    private final ImpostazioniRepository impostazioniRepository;
    private final WebSocketService wsService;

    @GetMapping
    public List<DiarioProduzione> getByDate(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long utenteId) {
        Utente current = currentUser();
        LocalDate data = date == null || date.isBlank() ? LocalDate.now() : LocalDate.parse(date);
        if (current.getLivello() == 0 && utenteId != null) {
            return repository.findByUtenteIdAndDataOrdered(utenteId, data);
        }
        if (current.getLivello() == 0) {
            return repository.findByDataOrdered(data);
        }
        return repository.findByUtenteIdAndDataOrdered(current.getId(), data);
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> generaReportPdf(
            @RequestParam(required = false) String data,
            @RequestParam(required = false) Long utenteId) {
        LocalDate localDate = data == null || data.isBlank() ? LocalDate.now() : LocalDate.parse(data);
        Utente utente = resolveReportUser(utenteId);
        byte[] pdfBytes = generaPdfDiario(localDate, utente);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=diario-produzione-" + utente.getId() + "-" + localDate + ".pdf")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdfBytes);
    }

    @PostMapping
    public DiarioProduzione create(@RequestBody DiarioProduzioneDto dto) {
        DiarioProduzione diario = new DiarioProduzione();
        applyDto(diario, dto);
        diario.setUtente(utenteRepository.findById(currentUser().getId()).orElseThrow());
        diario.setCreatedAt(LocalDateTime.now());
        diario.setUpdatedAt(LocalDateTime.now());
        DiarioProduzione saved = repository.save(diario);
        wsService.broadcast(Constants.MSG_REFRESH, null);
        return saved;
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiarioProduzione> update(@PathVariable Long id, @RequestBody DiarioProduzioneDto dto) {
        return repository.findById(id)
                .filter(this::canEdit)
                .map(existing -> {
                    applyDto(existing, dto);
                    existing.setUpdatedAt(LocalDateTime.now());
                    DiarioProduzione saved = repository.save(existing);
                    wsService.broadcast(Constants.MSG_REFRESH, null);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/invia")
    public ResponseEntity<DiarioProduzione> invia(@PathVariable Long id) {
        return repository.findById(id)
                .filter(this::canEdit)
                .map(existing -> {
                    existing.setInviatoAt(LocalDateTime.now());
                    existing.setUpdatedAt(LocalDateTime.now());
                    DiarioProduzione saved = repository.save(existing);
                    wsService.broadcast(Constants.MSG_REFRESH, null);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return repository.findById(id)
                .filter(this::canEdit)
                .map(existing -> {
                    repository.delete(existing);
                    wsService.broadcast(Constants.MSG_REFRESH, null);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void applyDto(DiarioProduzione diario, DiarioProduzioneDto dto) {
        if (dto.getCommessaId() == null) {
            throw new IllegalArgumentException("Commessa obbligatoria");
        }
        Commessa commessa = commessaRepository.findById(dto.getCommessaId())
                .orElseThrow(() -> new IllegalArgumentException("Commessa non trovata"));
        diario.setCommessa(commessa);
        diario.setClienteCommessa(blankToDefault(dto.getClienteCommessa(), commessa.getCodice()));
        diario.setData(dto.getData() == null ? LocalDate.now() : dto.getData());
        diario.setOraInizio(dto.getOraInizio());
        diario.setOraFine(dto.getOraFine());
        diario.setDescrizione(dto.getDescrizione());
        diario.setTipoLavorazione(dto.getTipoLavorazione());
        diario.setAttrezzaturaDanneggiata(dto.getAttrezzaturaDanneggiata());
        diario.setMaterialeUtilizzatoExtra(dto.getMaterialeUtilizzatoExtra());
        diario.setConsumabiliPrelevati(dto.getConsumabiliPrelevati());
        if (Boolean.TRUE.equals(dto.getInvia())) {
            diario.setInviatoAt(LocalDateTime.now());
        }
    }

    private boolean canEdit(DiarioProduzione diario) {
        Utente current = currentUser();
        return current.getLivello() == 0 || diario.getUtente().getId().equals(current.getId());
    }

    private Utente currentUser() {
        return (Utente) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Utente resolveReportUser(Long utenteId) {
        Utente current = currentUser();
        if (current.getLivello() == 0 && utenteId != null) {
            return utenteRepository.findById(utenteId)
                    .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));
        }
        return current;
    }

    private byte[] generaPdfDiario(LocalDate data, Utente utente) {
        List<DiarioProduzione> righe = repository.findReportRows(utente.getId(), data);

        try {
            Document document = new Document(PageSize.A4, 24, 24, 24, 24);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(0, 92, 170));
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
            Font italicFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, Color.BLACK);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(0, 92, 170));

            PdfPTable header = new PdfPTable(3);
            header.setWidthPercentage(100);
            header.setWidths(new float[] { 1.2f, 1.2f, 1.2f });
            addBox(header, "OPERATORE", labelFont, Element.ALIGN_CENTER, 36);
            addBox(header, userName(utente), valueFont, Element.ALIGN_CENTER, 36);
            addLogoBox(header, 72);
            addBox(header, "DATA", labelFont, Element.ALIGN_CENTER, 36);
            addBox(header, data.format(DateTimeFormatter.ofPattern("dd/MM/yy")), valueFont, Element.ALIGN_CENTER, 36);
            addBox(header, "", valueFont, Element.ALIGN_CENTER, 36);
            document.add(header);

            PdfPTable title = new PdfPTable(1);
            title.setWidthPercentage(100);
            addBox(title, "SVOLGIMENTO LAVORAZIONE", titleFont, Element.ALIGN_CENTER, 56);
            document.add(title);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 1f, 1f, 1f });
            addBox(table, "INIZIO - FINE", labelFont, Element.ALIGN_CENTER, 58);
            addBox(table, "CLIENTE\nCOMMESSA", labelFont, Element.ALIGN_CENTER, 58);
            addBox(table, "TIPO DI LAVORAZIONE", labelFont, Element.ALIGN_CENTER, 58);

            int rows = Math.max(6, righe.size());
            for (int i = 0; i < rows; i++) {
                if (i < righe.size()) {
                    DiarioProduzione riga = righe.get(i);
                    addBox(table, timeRange(riga.getOraInizio(), riga.getOraFine()), italicFont, Element.ALIGN_CENTER, 62);
                    addBox(table, blankToDefault(riga.getClienteCommessa(), riga.getCommessa().getCodice()), italicFont, Element.ALIGN_CENTER, 62);
                    addBox(table, blankToDefault(riga.getTipoLavorazione(), riga.getDescrizione()), italicFont, Element.ALIGN_CENTER, 62);
                } else {
                    addBox(table, "", valueFont, Element.ALIGN_CENTER, 62);
                    addBox(table, "", valueFont, Element.ALIGN_CENTER, 62);
                    addBox(table, "", valueFont, Element.ALIGN_CENTER, 62);
                }
            }
            document.add(table);

            PdfPTable footer = new PdfPTable(3);
            footer.setWidthPercentage(100);
            footer.setWidths(new float[] { 1f, 2f, 0.01f });
            addBox(footer, "ATTREZZATURA DANNEGGIATA", labelFont, Element.ALIGN_CENTER, 60);
            addBox(footer, fieldValues(righe, DiarioProduzione::getAttrezzaturaDanneggiata), italicFont, Element.ALIGN_CENTER, 60);
            addHiddenBox(footer);
            addBox(footer, "MATERIALE UTILIZZATO\nEXTRA", labelFont, Element.ALIGN_CENTER, 70);
            addBox(footer, fieldValues(righe, DiarioProduzione::getMaterialeUtilizzatoExtra), italicFont, Element.ALIGN_CENTER, 70);
            addHiddenBox(footer);
            addBox(footer, "CONSUMABILI PRELEVATI\nDAL MAGAZZINO", labelFont, Element.ALIGN_CENTER, 70);
            addBox(footer, fieldValues(righe, DiarioProduzione::getConsumabiliPrelevati), italicFont, Element.ALIGN_CENTER, 70);
            addHiddenBox(footer);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la generazione del PDF diario produzione", e);
        }
    }

    private void addBox(PdfPTable table, String value, Font font, int alignment, float height) {
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setFixedHeight(height);
        cell.setPadding(6);
        cell.setBorderColor(new Color(210, 210, 210));
        table.addCell(cell);
    }

    private void addLogoBox(PdfPTable table, float height) {
        PdfPCell cell = new PdfPCell();
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setFixedHeight(height);
        cell.setBorderColor(new Color(210, 210, 210));
        cell.setBackgroundColor(new Color(225, 225, 225));
        Image logo = loadLogo();
        if (logo != null) {
            logo.scaleToFit(130, 54);
            logo.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(logo);
        } else {
            cell.addElement(new Paragraph("CASTELLANO METAL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(220, 0, 35))));
        }
        table.addCell(cell);
    }

    private Image loadLogo() {
        try {
            File file = new File("frontend/public/images/logos/logo_transparent.png");
            if (file.exists()) {
                return Image.getInstance(file.getPath());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void addHiddenBox(PdfPTable table) {
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorderColor(Color.WHITE);
        table.addCell(cell);
    }

    private String userName(Utente utente) {
        return ((utente.getNome() != null ? utente.getNome() : utente.getUsername())
                + " " + (utente.getCognome() != null ? utente.getCognome() : "")).trim();
    }

    private String timeRange(LocalTime start, LocalTime end) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("H:mm");
        if (start == null && end == null) return "";
        if (start == null) return end.format(fmt);
        if (end == null) return start.format(fmt);
        return start.format(fmt) + " - " + end.format(fmt);
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String fieldValues(List<DiarioProduzione> righe, java.util.function.Function<DiarioProduzione, String> getter) {
        return righe.stream()
                .map(getter)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }
}
