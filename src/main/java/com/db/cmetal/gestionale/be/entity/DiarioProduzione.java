package com.db.cmetal.gestionale.be.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
@Table(name = "diario_produzione")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiarioProduzione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "commessa_id", nullable = false)
    private Commessa commessa;

    @Column(name = "cliente_commessa", length = 200)
    private String clienteCommessa;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "ora_inizio", nullable = false)
    private LocalTime oraInizio;

    @Column(name = "ora_fine", nullable = false)
    private LocalTime oraFine;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descrizione;

    @Column(name = "tipo_lavorazione", length = 200)
    private String tipoLavorazione;

    @Column(name = "attrezzatura_danneggiata", columnDefinition = "TEXT")
    private String attrezzaturaDanneggiata;

    @Column(name = "materiale_utilizzato_extra", columnDefinition = "TEXT")
    private String materialeUtilizzatoExtra;

    @Column(name = "consumabili_prelevati", columnDefinition = "TEXT")
    private String consumabiliPrelevati;

    @Column(name = "inviato_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime inviatoAt;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime updatedAt;
}
