package com.db.cmetal.gestionale.be.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;

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
@Table(name = "tabella_marcia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TabellaMarcia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false, length = 30)
    private String targa;

    @Column(name = "orario_uscita")
    private LocalTime orarioUscita;

    @Column(name = "orario_rientro")
    private LocalTime orarioRientro;

    @Column(name = "data_rientro")
    private LocalDate dataRientro;

    @Column(name = "km_partenza", nullable = false)
    private Integer kmPartenza;

    @Column(name = "km_rientro")
    private Integer kmRientro;

    @Column(nullable = false)
    private Boolean guasti = false;

    @Column(nullable = false)
    private Boolean rifornimento = false;

    @Column(name = "importo_rifornimento")
    private BigDecimal importoRifornimento;

    @Column(name = "metodo_pagamento", length = 100)
    private String metodoPagamento;

    @Column(name = "controlli_pre_partenza", columnDefinition = "TEXT")
    private String controlliPrePartenza;

    @Column(name = "guasti_rotture", columnDefinition = "TEXT")
    private String guastiRotture;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "inviato_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime inviatoAt;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime updatedAt;
}
