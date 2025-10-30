package com.db.cmetal.be.entity;

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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "ordine")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ordine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sessione_id", nullable = false)
    private Sessione sessione;

    @ManyToOne
    @JoinColumn(name = "tavolo_id", nullable = false)
    private Tavolo tavolo;

    @ManyToOne
    @JoinColumn(name = "prodotto_id", nullable = false)
    private Prodotto prodotto;

    @Column(nullable = false)
    private Integer quantita;

    @Column(name = "prezzo_unitario", nullable = false)
    private Double prezzoUnitario;

    @Column(nullable = false)
    private LocalDateTime orario;

    private Boolean flagConsegnato;

    private String stato;
}
