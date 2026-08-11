package com.db.cmetal.gestionale.be.controller;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

import com.db.cmetal.gestionale.be.dto.TabellaMarciaDto;
import com.db.cmetal.gestionale.be.entity.TabellaMarcia;
import com.db.cmetal.gestionale.be.entity.Utente;
import com.db.cmetal.gestionale.be.repository.TabellaMarciaRepository;
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
@RequestMapping("/api/tabelle-marcia")
@RequiredArgsConstructor
public class TabellaMarciaController {

    private final TabellaMarciaRepository repository;
    private final UtenteRepository utenteRepository;
    private final WebSocketService wsService;

    @GetMapping
    public List<TabellaMarcia> getByDate(
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

    @GetMapping("/{id}/report/pdf")
    public ResponseEntity<byte[]> generaReportPdf(@PathVariable Long id) {
        TabellaMarcia tabella = repository.findById(id)
                .filter(this::canView)
                .orElseThrow(() -> new IllegalArgumentException("Tabella di marcia non trovata"));
        byte[] pdfBytes = generaPdfMarcia(tabella);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tabella-marcia-" + tabella.getId() + ".pdf")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdfBytes);
    }

    @PostMapping
    public TabellaMarcia create(@RequestBody TabellaMarciaDto dto) {
        TabellaMarcia tabella = new TabellaMarcia();
        applyDto(tabella, dto);
        tabella.setUtente(utenteRepository.findById(currentUser().getId()).orElseThrow());
        tabella.setCreatedAt(LocalDateTime.now());
        tabella.setUpdatedAt(LocalDateTime.now());
        TabellaMarcia saved = repository.save(tabella);
        wsService.broadcast(Constants.MSG_REFRESH, null);
        return saved;
    }

    @PutMapping("/{id}")
    public ResponseEntity<TabellaMarcia> update(@PathVariable Long id, @RequestBody TabellaMarciaDto dto) {
        return repository.findById(id)
                .filter(this::canEdit)
                .map(existing -> {
                    applyDto(existing, dto);
                    existing.setUpdatedAt(LocalDateTime.now());
                    TabellaMarcia saved = repository.save(existing);
                    wsService.broadcast(Constants.MSG_REFRESH, null);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/invia")
    public ResponseEntity<TabellaMarcia> invia(@PathVariable Long id) {
        return repository.findById(id)
                .filter(this::canEdit)
                .map(existing -> {
                    existing.setInviatoAt(LocalDateTime.now());
                    existing.setUpdatedAt(LocalDateTime.now());
                    TabellaMarcia saved = repository.save(existing);
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

    private void applyDto(TabellaMarcia tabella, TabellaMarciaDto dto) {
        LocalDate data = dto.getData() == null ? LocalDate.now() : dto.getData();
        tabella.setData(data);
        tabella.setDataRientro(dto.getDataRientro() == null ? data : dto.getDataRientro());
        tabella.setTarga(dto.getTarga());
        tabella.setOrarioUscita(dto.getOrarioUscita());
        tabella.setOrarioRientro(dto.getOrarioRientro());
        tabella.setKmPartenza(dto.getKmPartenza());
        tabella.setKmRientro(dto.getKmRientro());
        tabella.setGuasti(Boolean.TRUE.equals(dto.getGuasti()));
        tabella.setRifornimento(Boolean.TRUE.equals(dto.getRifornimento()));
        tabella.setImportoRifornimento(dto.getImportoRifornimento());
        tabella.setMetodoPagamento(dto.getMetodoPagamento());
        tabella.setControlliPrePartenza(dto.getControlliPrePartenza());
        tabella.setGuastiRotture(dto.getGuastiRotture());
        tabella.setNote(dto.getNote());
        if (Boolean.TRUE.equals(dto.getInvia())) {
            tabella.setInviatoAt(LocalDateTime.now());
        }
    }

    private boolean canEdit(TabellaMarcia tabella) {
        Utente current = currentUser();
        return current.getLivello() == 0 || tabella.getUtente().getId().equals(current.getId());
    }

    private boolean canView(TabellaMarcia tabella) {
        Utente current = currentUser();
        return current.getLivello() == 0 || tabella.getUtente().getId().equals(current.getId());
    }

    private Utente currentUser() {
        return (Utente) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private byte[] generaPdfMarcia(TabellaMarcia row) {
        try {
            Document document = new Document(PageSize.A4, 18, 18, 18, 18);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(0, 92, 170));
            Font greenFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(42, 128, 32));
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(42, 128, 32));

            PdfPTable header = new PdfPTable(4);
            header.setWidthPercentage(100);
            header.setWidths(new float[] { 0.8f, 0.9f, 1.7f, 1.8f });
            addBox(header, "PRG_M_1", valueFont, Element.ALIGN_LEFT, 58);
            addBox(header, "TABELLA DI\nMARCIA\nCANTIERE", titleFont, Element.ALIGN_CENTER, 58);
            addBox(header, "", valueFont, Element.ALIGN_CENTER, 58);
            addLogoBox(header, 58);
            document.add(header);

            PdfPTable main = new PdfPTable(3);
            main.setWidthPercentage(100);
            main.setWidths(new float[] { 1f, 1f, 1f });
            addBox(main, "CONDUCENTE", labelFont, Element.ALIGN_CENTER, 52);
            addBox(main, "GIORNO E\nORARIO USCITA", labelFont, Element.ALIGN_CENTER, 52);
            addBox(main, "GIORNO E\nORARIO RIENTRO", labelFont, Element.ALIGN_CENTER, 52);
            addBox(main, userName(row.getUtente()), valueFont, Element.ALIGN_CENTER, 56);
            addBox(main, dateTime(row.getData(), row.getOrarioUscita()), valueFont, Element.ALIGN_CENTER, 56);
            addBox(main, dateTime(row.getDataRientro() != null ? row.getDataRientro() : row.getData(), row.getOrarioRientro()), valueFont, Element.ALIGN_CENTER, 56);
            addBox(main, "KM IN USCITA", labelFont, Element.ALIGN_CENTER, 52);
            addBox(main, "KM IN ENTRATA", labelFont, Element.ALIGN_CENTER, 52);
            addBox(main, "TOTALE PERCORSO", greenFont, Element.ALIGN_CENTER, 52);
            addBox(main, value(row.getKmPartenza()), valueFont, Element.ALIGN_CENTER, 56);
            addBox(main, value(row.getKmRientro()), valueFont, Element.ALIGN_CENTER, 56);
            addBox(main, totalKm(row), valueFont, Element.ALIGN_CENTER, 56);
            document.add(main);

            PdfPTable fuel = new PdfPTable(2);
            fuel.setWidthPercentage(100);
            fuel.setWidths(new float[] { 1f, 1f });
            addBox(fuel, "RIFORNIMENTO\nEFFETTUATO " + fuelAmount(row), labelFont, Element.ALIGN_CENTER, 48);
            addBox(fuel, "METODO\nPAGAMENTO", labelFont, Element.ALIGN_CENTER, 48);
            addBox(fuel, Boolean.TRUE.equals(row.getRifornimento()) ? "SI" : "NO", valueFont, Element.ALIGN_CENTER, 52);
            addBox(fuel, row.getMetodoPagamento(), valueFont, Element.ALIGN_CENTER, 52);
            document.add(fuel);

            PdfPTable notes = new PdfPTable(1);
            notes.setWidthPercentage(100);
            addBox(notes, "CONTROLLI FATTI PRIMA DI PARTIRE", labelFont, Element.ALIGN_CENTER, 34);
            addBox(notes, row.getControlliPrePartenza(), valueFont, Element.ALIGN_LEFT, 88);
            addBox(notes, "EVENTUALI GUASTI E ROTTURE", labelFont, Element.ALIGN_CENTER, 34);
            addBox(notes, blankToDefault(row.getGuastiRotture(), row.getNote()), valueFont, Element.ALIGN_LEFT, 98);
            document.add(notes);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la generazione del PDF tabella di marcia", e);
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

    private String userName(Utente utente) {
        return ((utente.getNome() != null ? utente.getNome() : utente.getUsername())
                + " " + (utente.getCognome() != null ? utente.getCognome() : "")).trim();
    }

    private String dateTime(LocalDate date, LocalTime time) {
        String day = date.format(DateTimeFormatter.ofPattern("dd/MM/yy"));
        return time == null ? day : day + " " + time.format(DateTimeFormatter.ofPattern("H:mm"));
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String totalKm(TabellaMarcia row) {
        if (row.getKmPartenza() == null || row.getKmRientro() == null) {
            return "";
        }
        return String.valueOf(row.getKmRientro() - row.getKmPartenza());
    }

    private String fuelAmount(TabellaMarcia row) {
        BigDecimal amount = row != null ? row.getImportoRifornimento() : null;
        return amount == null ? "EURO" : amount.toPlainString() + " EURO";
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
