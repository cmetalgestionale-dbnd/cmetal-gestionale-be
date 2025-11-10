package com.db.cmetal.gestionale.be.service.impl;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.db.cmetal.gestionale.be.dto.AssegnazioneDto;
import com.db.cmetal.gestionale.be.entity.Allegato;
import com.db.cmetal.gestionale.be.entity.Assegnazione;
import com.db.cmetal.gestionale.be.entity.Cliente;
import com.db.cmetal.gestionale.be.entity.Commessa;
import com.db.cmetal.gestionale.be.entity.Impostazioni;
import com.db.cmetal.gestionale.be.entity.Utente;
import com.db.cmetal.gestionale.be.repository.AllegatoRepository;
import com.db.cmetal.gestionale.be.repository.AssegnazioneRepository;
import com.db.cmetal.gestionale.be.repository.ClienteRepository;
import com.db.cmetal.gestionale.be.repository.CommessaRepository;
import com.db.cmetal.gestionale.be.repository.ImpostazioniRepository;
import com.db.cmetal.gestionale.be.repository.UtenteRepository;
import com.db.cmetal.gestionale.be.service.AssegnazioneService;
import com.db.cmetal.gestionale.be.service.SupabaseS3Service;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AssegnazioneServiceImpl implements AssegnazioneService {

    private final AssegnazioneRepository assegnazioneRepository;
    private final UtenteRepository utenteRepository;
    private final CommessaRepository commessaRepository;
    private final ClienteRepository clienteRepository;
    private final AllegatoRepository allegatoRepository;
    private final ImpostazioniRepository impostazioniRepository;
    private final SupabaseS3Service s3Service;
    private final WebSocketService wsService;

    @Override
    public List<Assegnazione> getAll() {
        return assegnazioneRepository.findAllByIsDeletedFalse();
    }

    @Override
    public List<Assegnazione> getByUtenteAndData(Long utenteId, LocalDate data) {
    	LocalDateTime startOfDay = data.atStartOfDay();
    	LocalDateTime endOfDay = data.plusDays(1).atStartOfDay();
 return assegnazioneRepository.findVisibleByUtenteIdAndAssegnazioneAtBetween(utenteId, startOfDay, endOfDay);
    }


    @Override
    public Assegnazione getById(Long id) {
        return assegnazioneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assegnazione non trovata"));
    }

    @Override
    public Assegnazione createFromDto(AssegnazioneDto dto, Long assegnatoDaId, LocalDateTime assegnazioneAt) {
        Utente utente = utenteRepository.findById(dto.getUtenteId())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        Commessa commessa = commessaRepository.findById(dto.getCommessaId())
                .orElseThrow(() -> new RuntimeException("Commessa non trovata"));
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente non trovato"));
        Utente assegnatoDa = utenteRepository.findById(assegnatoDaId)
                .orElseThrow(() -> new RuntimeException("Utente assegnatore non trovato"));

        Assegnazione a = new Assegnazione();
        a.setUtente(utente);
        a.setCommessa(commessa);
        a.setCliente(cliente);
        a.setAssegnatoDa(assegnatoDa);
        a.setNote(dto.getNote());
        a.setAssegnazioneAt(assegnazioneAt);
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        a.setIsDeleted(false);
        
        Assegnazione saved = assegnazioneRepository.save(a);
        wsService.broadcast(Constants.MSG_REFRESH, null);
        return saved;
    }

    @Override
    public Assegnazione updateFromDto(Long id, AssegnazioneDto dto) {
        Assegnazione existing = getById(id);

        existing.setUtente(utenteRepository.findById(dto.getUtenteId())
                .orElseThrow(() -> new RuntimeException("Utente non trovato")));
        existing.setCommessa(commessaRepository.findById(dto.getCommessaId())
                .orElseThrow(() -> new RuntimeException("Commessa non trovata")));
        existing.setCliente(clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente non trovato")));
        existing.setNote(dto.getNote());
        existing.setUpdatedAt(LocalDateTime.now());

        assegnazioneRepository.save(existing);
        wsService.broadcast(Constants.MSG_REFRESH, null);
        return existing;
    }

    @Override
    public void softDelete(Long id) {
        Assegnazione a = getById(id);
        a.setIsDeleted(true);
        a.setUpdatedAt(LocalDateTime.now());
        assegnazioneRepository.save(a);
        wsService.broadcast(Constants.MSG_REFRESH, null);
    }
    
    @Override
    public Assegnazione startAssegnazione(Long id, Long utenteId) {
        Assegnazione a = assegnazioneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assegnazione non trovata"));
        // solo il dipendente assegnato può avviare
        if (!a.getUtente().getId().equals(utenteId)) {
            throw new RuntimeException("Non autorizzato");
        }
        a.setStartAt(LocalDateTime.now());
        assegnazioneRepository.save(a);
        wsService.broadcast(Constants.MSG_REFRESH, null);
        return a;
    }

    @Override
    public Assegnazione endAssegnazione(Long id, Long utenteId) {
        Assegnazione a = assegnazioneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assegnazione non trovata"));
        if (!a.getUtente().getId().equals(utenteId)) {
            throw new RuntimeException("Non autorizzato");
        }
        a.setEndAt(LocalDateTime.now());
        assegnazioneRepository.save(a);
        wsService.broadcast(Constants.MSG_REFRESH, null);
        return a;
    }
    
    @Override
    public Allegato uploadFoto(Long assegnazioneId, MultipartFile file, Long utenteId) throws Exception {
        Assegnazione a = getById(assegnazioneId);
        if (!a.getUtente().getId().equals(utenteId)) {
            throw new RuntimeException("Non autorizzato a caricare foto per questa assegnazione");
        }

        // VALIDAZIONE: file presente
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File mancante");
        }

        // VALIDAZIONE: tipo consentito
        String contentType = file.getContentType();
        if (contentType == null ||
            !(contentType.equalsIgnoreCase("image/jpeg")
              || contentType.equalsIgnoreCase("image/jpg")
              || contentType.equalsIgnoreCase("image/png")
              || contentType.equalsIgnoreCase("image/webp"))) {
            throw new IllegalArgumentException("Il file deve essere un'immagine (JPEG, PNG o WebP)");
        }

        // VALIDAZIONE: dimensione max 1 MB
        final long MAX_BYTES = 1_048_576L; // 1 MB
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("La foto non può superare 1 MB");
        }

        // se già presente un allegato, lo disabilitiamo
        if (a.getFotoAllegato() != null) {
            Allegato old = a.getFotoAllegato();
            old.setIsDeleted(true);
            allegatoRepository.save(old);
        }

        String path = "assegnazioni/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        s3Service.uploadFile(file, path);

        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        Allegato allegato = new Allegato();
        allegato.setNomeFile(file.getOriginalFilename());
        allegato.setTipoFile(file.getContentType());
        allegato.setStoragePath(path);
        allegato.setCreatedAt(LocalDateTime.now());
        allegato.setCreatedBy(utente);
        allegato.setIsDeleted(false);

        Allegato saved = allegatoRepository.save(allegato);
        a.setFotoAllegato(saved);
        assegnazioneRepository.save(a);

        wsService.broadcast(Constants.MSG_REFRESH, null);
        return saved;
    }

    @Override
    public Optional<ResponseEntity<byte[]>> getFotoFile(Long assegnazioneId) throws Exception {
        return Optional.ofNullable(getById(assegnazioneId).getFotoAllegato())
                .map(allegato -> {
                    try {
                        byte[] bytes = s3Service.downloadFile(allegato.getStoragePath());
                        return ResponseEntity.ok()
                                .header("Content-Disposition", "inline; filename=\"" + allegato.getNomeFile() + "\"")
                                .contentType(MediaType.parseMediaType(allegato.getTipoFile()))
                                .body(bytes);
                    } catch (Exception e) {
                        throw new RuntimeException("Errore download foto", e);
                    }
                });
    }

    @Override
    public byte[] generaReportPdf(LocalDate localDate) {
        try {
        	LocalDateTime startOfDay = localDate.atStartOfDay();
        	LocalDateTime endOfDay = localDate.plusDays(1).atStartOfDay();

            List<Utente> utenti = utenteRepository.findByLivello(2);

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

            // Aggiungo evento per footer dinamico
            writer.setPageEvent(new PdfPageEventHelper() {
                Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);

                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfContentByte cb = writer.getDirectContent();
                    Phrase footer = new Phrase(aziendaIndirizzo + "  |  P.IVA " + aziendaPiva, footerFont);
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

            // === FONT ===
            Font titoloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font sottoTitoloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font testoFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font intestazioneTabellaFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

            // === INTESTAZIONE ===
            Paragraph titolo = new Paragraph(aziendaNome.toUpperCase(), titoloFont);
            titolo.setAlignment(Element.ALIGN_CENTER);
            document.add(titolo);

            Paragraph sottoTitolo = new Paragraph("RAPPORTO DI LAVORO OFFICINA", sottoTitoloFont);
            sottoTitolo.setAlignment(Element.ALIGN_CENTER);
            document.add(sottoTitolo);

            document.add(Chunk.NEWLINE);

            Paragraph dataParagrafo = new Paragraph(
                    localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - Report giornaliero",
                    testoFont
            );
            dataParagrafo.setAlignment(Element.ALIGN_CENTER);
            document.add(dataParagrafo);

            document.add(Chunk.NEWLINE);

            // === TABELLA PRINCIPALE ===
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 2.5f, 4f, 2f, 2f, 2.5f});

            // Intestazioni tabella
            String[] headers = {
                    "Rif. Disegno", "Tipologia Lavoro Svolto", "Cod. Operatore",
                    "Iniziato", "Finito", "Rif. Cliente"
            };

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, intestazioneTabellaFont));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // === RIGHE DATI ===
            for (Utente utente : utenti) {
                int i = 1;
                List<Assegnazione> assegnazioni = assegnazioneRepository
                        .findVisibleByUtenteIdAndAssegnazioneAtBetween(utente.getId(), startOfDay, endOfDay);

                for (Assegnazione a : assegnazioni) {
                    String rifDisegno = (a.getCommessa() != null && a.getCommessa().getCodice() != null)
                            ? a.getCommessa().getCodice()
                            : "-";
                    String tipologiaLavoro = (a.getCommessa() != null ? a.getCommessa().getDescrizione() : "")
                            + (a.getNote() != null ? " - " + a.getNote() : "");
                    String codOperatore = utente.getNome() + " " + utente.getCognome() + " " + i;
                    String iniziato = (a.getStartAt() != null)
                            ? a.getStartAt().format(DateTimeFormatter.ofPattern("HH:mm"))
                            : "-";
                    String finito = (a.getEndAt() != null)
                            ? a.getEndAt().format(DateTimeFormatter.ofPattern("HH:mm"))
                            : "-";
                    String rifCliente = (a.getCliente() != null ? a.getCliente().getNome() : "-");

                    addCell(table, rifDisegno, testoFont);
                    addCell(table, tipologiaLavoro, testoFont);
                    addCell(table, codOperatore, testoFont);
                    addCell(table, iniziato, testoFont);
                    addCell(table, finito, testoFont);
                    addCell(table, rifCliente, testoFont);

                    i++;
                }
            }

            document.add(table);
            document.close();
            writer.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la generazione del PDF: " + e.getMessage(), e);
        }
    }

    // === Metodo di supporto per le celle ===
    private void addCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        table.addCell(cell);
    }



}
