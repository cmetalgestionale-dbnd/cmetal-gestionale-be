package com.db.cmetal.gestionale.be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "commessa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commessa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codice;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "rif_disegno", length = 100)
    private String rifDisegno;

    private String tipologia;

    @ManyToOne
    @JoinColumn(name = "pdf_allegato_id")
    private Allegato pdfAllegato;

    @Column(nullable = false)
    private String stato = "aperta";

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Utente createdBy;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @Column(name = "data_creazione", nullable = false)
    private LocalDate dataCreazione;

    @Column(name = "data_consegna")
    private LocalDate dataConsegna;
}
