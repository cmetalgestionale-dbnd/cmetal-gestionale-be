package com.db.cmetal.gestionale.be.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @ManyToOne
    @JoinColumn(name = "pdf_allegato_id")
    private Allegato pdfAllegato;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Utente createdBy;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime createdAt;
}
