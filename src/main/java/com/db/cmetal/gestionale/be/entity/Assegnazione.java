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
@Table(name = "assegnazione")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assegnazione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "commessa_id")
    private Commessa commessa;

    @ManyToOne
    @JoinColumn(name = "utente_id")
    private Utente utente;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "assegnato_da")
    private Utente assegnatoDa;

    @Column(name = "assegnazione_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime assegnazioneAt;

    @Column(name = "start_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime startAt;

    @Column(name = "end_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime endAt;

    @ManyToOne
    @JoinColumn(name = "foto_allegato_id")
    private Allegato fotoAllegato;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime updatedAt;
}
