package com.db.cmetal.gestionale.be.service.impl;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.db.cmetal.gestionale.be.entity.Impostazioni;
import com.db.cmetal.gestionale.be.entity.InventarioMovimento;
import com.db.cmetal.gestionale.be.repository.ImpostazioniRepository;
import com.db.cmetal.gestionale.be.repository.InventarioMovimentoRepository;
import com.db.cmetal.gestionale.be.service.InventarioMovimentoService;
import com.db.cmetal.gestionale.be.service.WebSocketService;
import com.db.cmetal.gestionale.be.utils.Constants;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class InventarioMovimentoServiceImpl implements InventarioMovimentoService {

    private final InventarioMovimentoRepository repository;
    private final ImpostazioniRepository impostazioniRepository;
    private final WebSocketService wsService;

    public InventarioMovimentoServiceImpl(InventarioMovimentoRepository repository, ImpostazioniRepository impostazioniRepository,
    		WebSocketService wsService) {
        this.repository = repository;
        this.impostazioniRepository = impostazioniRepository;
        this.wsService = wsService;
    }

    @Override
    public List<InventarioMovimento> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<InventarioMovimento> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<InventarioMovimento> findByArticolo(Long articoloId) {
        return repository.findByInventarioIdOrderByMovimentoAtDesc(articoloId);
    }

    @Override
    public InventarioMovimento save(InventarioMovimento movimento) {
    	wsService.broadcast(Constants.MSG_REFRESH, null);
        return repository.save(movimento);
    }

    @Override
    public void deleteById(Long id) {
    	wsService.broadcast(Constants.MSG_REFRESH, null);
        repository.deleteById(id);
    }
    
    @Override
    public List<InventarioMovimento> findByArticoloAndRange(Long articoloId, OffsetDateTime from, OffsetDateTime to) {
        return repository.findByInventarioIdAndMovimentoAtBetweenOrderByMovimentoAtDesc(
                articoloId, from, to
        );
    }

    @Override
    public List<InventarioMovimento> findByMagazzinoAndRange(Long magazzinoId, OffsetDateTime from, OffsetDateTime to) {
        return repository.findByInventarioMagazzinoIdAndMovimentoAtBetweenOrderByMovimentoAtDesc(
                magazzinoId, from, to
        );
    }

    @Override
    public byte[] generaReportPdfMagazzino(Long magazzinoId, OffsetDateTime from, OffsetDateTime to) {
        try {
            // Recupero movimenti filtrati
            List<InventarioMovimento> movimenti =
                    repository.findByMagazzinoAndRange(magazzinoId, from, to);

            // Recupero impostazioni aziendali
            String aziendaNome = impostazioniRepository.findById("azienda_nome")
                    .map(Impostazioni::getValore)
                    .orElse("CASTELLANO METAL");

            String aziendaPiva = impostazioniRepository.findById("azienda_piva")
                    .map(Impostazioni::getValore)
                    .orElse("");

            String aziendaIndirizzo = impostazioniRepository.findById("azienda_indirizzo")
                    .map(Impostazioni::getValore)
                    .orElse("");

            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // Footer come l'altro report
            writer.setPageEvent(new PdfPageEventHelper() {
                Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);

                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfContentByte cb = writer.getDirectContent();
                    Phrase footer = new Phrase(
                            aziendaIndirizzo + "  |  P.IVA " + aziendaPiva,
                            footerFont
                    );
                    ColumnText.showTextAligned(
                            cb,
                            Element.ALIGN_CENTER,
                            footer,
                            (document.right() - document.left()) / 2 + document.leftMargin(),
                            document.bottom() - 10,
                            0
                    );
                }
            });

            document.open();

            // FONT
            Font titoloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font sottoTitoloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font testoFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

            // HEADER
            Paragraph titolo = new Paragraph(aziendaNome.toUpperCase(), titoloFont);
            titolo.setAlignment(Element.ALIGN_CENTER);
            document.add(titolo);

            Paragraph sottoTitolo = new Paragraph(
                    "REPORT MOVIMENTI MAGAZZINO", sottoTitoloFont
            );
            sottoTitolo.setAlignment(Element.ALIGN_CENTER);
            document.add(sottoTitolo);

            document.add(Chunk.NEWLINE);

            Paragraph periodo = new Paragraph(
                    "Periodo: " +
                            from.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                            " → " +
                            to.minusDays(1).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    testoFont
            );
            periodo.setAlignment(Element.ALIGN_CENTER);
            document.add(periodo);

            document.add(Chunk.NEWLINE);


            // === TABELLA DATI ===
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1.5f, 2, 3.5f});

            addHeaderCell(table, "Articolo", headerFont);
            addHeaderCell(table, "Quantità", headerFont);
            addHeaderCell(table, "Data", headerFont);
            addHeaderCell(table, "Descrizione", headerFont);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (InventarioMovimento mov : movimenti) {
                addCell(table, mov.getInventario().getNome(), testoFont);
                addCell(table, String.valueOf(mov.getQuantita()), testoFont);
                addCell(table, mov.getMovimentoAt().format(fmt), testoFont);
                addCell(table,
                        mov.getDescrizione() != null ? mov.getDescrizione() : "-",
                        testoFont);
            }

            document.add(table);
            document.close();
            writer.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la generazione del PDF: " + e.getMessage(), e);
        }
    }

    // Helpers
    private void addHeaderCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        table.addCell(cell);
    }

}
