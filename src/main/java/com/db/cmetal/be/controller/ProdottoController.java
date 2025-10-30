package com.db.cmetal.be.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.db.cmetal.be.dto.UltimoOrdineDto;
import com.db.cmetal.be.entity.Categoria;
import com.db.cmetal.be.entity.Prodotto;
import com.db.cmetal.be.service.CategoriaService;
import com.db.cmetal.be.service.ImageService;
import com.db.cmetal.be.service.ProdottoService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/prodotti")
@RequiredArgsConstructor
public class ProdottoController {

    private final ProdottoService prodottoService;
    private final CategoriaService categoriaService;
    private final ImageService imageService;

    @GetMapping
    public List<Prodotto> getAllProdotti() {
        return prodottoService.findAll();
    }

    @GetMapping("/{id}")
    public Prodotto getProdottoById(@PathVariable Long id) {
        return prodottoService.findById(id);
    }

    @PostMapping(consumes = "multipart/form-data")
    public Prodotto createProdotto(
            @RequestParam("nome") String nome,
            @RequestParam("descrizione") String descrizione,
            @RequestParam("prezzo") Double prezzo,
            @RequestParam(value = "categoriaId", required = false) Long categoriaId,
            @RequestParam(value = "isPranzo", defaultValue = "true") Boolean isPranzo,
            @RequestParam(value = "isCena", defaultValue = "true") Boolean isCena,
            @RequestParam(value = "isAyce", defaultValue = "true") Boolean isAyce,
            @RequestParam(value = "isCarta", defaultValue = "true") Boolean isCarta,
            @RequestParam(value = "isLimitedPartecipanti", defaultValue = "false") Boolean isLimitedPartecipanti,
            @RequestParam(value = "immagine", required = false) MultipartFile immagineFile
    ) throws IOException {
        Prodotto p = new Prodotto();
        p.setNome(nome);
        p.setDescrizione(descrizione);
        p.setPrezzo(prezzo);

        if (categoriaId != null) {
            Categoria categoria = categoriaService.findById(categoriaId);
            p.setCategoria(categoria);
        }

        p.setIsPranzo(isPranzo);
        p.setIsCena(isCena);
        p.setIsAyce(isAyce);
        p.setIsCarta(isCarta);
        p.setIsLimitedPartecipanti(isLimitedPartecipanti);

        // salvo prodotto senza immagine per ottenere ID
        Prodotto saved = prodottoService.save(p);

        if (immagineFile != null && !immagineFile.isEmpty()) {
            String relativePath = imageService.saveProductImage(immagineFile, saved.getId());
            saved.setImmagine(relativePath);
            saved = prodottoService.save(saved);
        }

        return saved;
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public Prodotto updateProdotto(
            @PathVariable Long id,
            @RequestParam("nome") String nome,
            @RequestParam("descrizione") String descrizione,
            @RequestParam("prezzo") Double prezzo,
            @RequestParam(value = "categoriaId", required = false) Long categoriaId,
            @RequestParam(value = "isPranzo", defaultValue = "true") Boolean isPranzo,
            @RequestParam(value = "isCena", defaultValue = "true") Boolean isCena,
            @RequestParam(value = "isAyce", defaultValue = "true") Boolean isAyce,
            @RequestParam(value = "isCarta", defaultValue = "true") Boolean isCarta,
            @RequestParam(value = "isLimitedPartecipanti", defaultValue = "false") Boolean isLimitedPartecipanti,
            @RequestParam(value = "immagine", required = false) MultipartFile immagineFile
    ) throws IOException {
        Prodotto p = prodottoService.findById(id);
        p.setNome(nome);
        p.setDescrizione(descrizione);
        p.setPrezzo(prezzo);

        if (categoriaId != null) {
            Categoria categoria = categoriaService.findById(categoriaId);
            p.setCategoria(categoria);
        } else {
            p.setCategoria(null);
        }

        p.setIsPranzo(isPranzo);
        p.setIsCena(isCena);
        p.setIsAyce(isAyce);
        p.setIsCarta(isCarta);
        p.setIsLimitedPartecipanti(isLimitedPartecipanti);

        if (immagineFile != null && !immagineFile.isEmpty()) {
            String relativePath = imageService.saveProductImage(immagineFile, p.getId());
            p.setImmagine(relativePath);
        }

        return prodottoService.save(p);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProdotto(@PathVariable Long id) {
        prodottoService.delete(id); // adesso fa soft delete
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/utilizzati/pdf")
    public void getProdottiUtilizzatiPdf(
            @RequestParam(value = "data", required = false) String dataStr,
            HttpServletResponse response
    ) throws IOException, DocumentException {
        LocalDateTime now = LocalDateTime.now();
        LocalDate giornoTarget;

        if (dataStr != null) {
            // input fornito nel formato YYYY-MM-DD
            giornoTarget = LocalDate.parse(dataStr);
        } else {
            // comportamento default
            giornoTarget = now.toLocalTime().isBefore(LocalTime.of(7, 0))
                    ? now.toLocalDate().minusDays(1)
                    : now.toLocalDate();
        }

        LocalDateTime inizio = giornoTarget.atStartOfDay();
        LocalDateTime fine = giornoTarget.plusDays(1).atStartOfDay().plusHours(7);

        List<UltimoOrdineDto> prodottiUsati = prodottoService.getProdottiUtilizzatiUltimoServizio(inizio, fine);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=prodotti_utilizzati_" + giornoTarget + ".pdf");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font cellFont = new Font(Font.HELVETICA, 12);

        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter orarioFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        document.add(new Paragraph("Resoconto Prodotti Utilizzati", titleFont));
        document.add(new Paragraph("Giornata: " + giornoTarget.format(dataFormatter)));
        document.add(new Paragraph(
                "Ordini considerati dalle " + inizio.format(orarioFormatter) +
                " alle " + fine.format(orarioFormatter)
        ));
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{3, 1});

        PdfPCell h1 = new PdfPCell(new Phrase("Prodotto", headerFont));
        h1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(h1);

        PdfPCell h2 = new PdfPCell(new Phrase("Quantità", headerFont));
        h2.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(h2);

        for (UltimoOrdineDto p : prodottiUsati) {
            PdfPCell c1 = new PdfPCell(new Phrase(p.getNomeProdotto(), cellFont));
            table.addCell(c1);

            PdfPCell c2 = new PdfPCell(new Phrase(String.valueOf(p.getNumOrdinati()), cellFont));
            c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(c2);
        }

        document.add(table);
        document.close();
    }


    @GetMapping("/deleted")
    public List<Prodotto> getDeletedProdotti() {
        return prodottoService.findDeleted();
    }

    @PutMapping("/{id}/restore")
    public Prodotto restoreProdotto(@PathVariable Long id) {
        return prodottoService.restore(id);
    }

}
