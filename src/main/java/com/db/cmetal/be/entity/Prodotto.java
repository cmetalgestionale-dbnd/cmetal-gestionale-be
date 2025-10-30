package com.db.cmetal.be.entity;

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
@Table(name = "prodotto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prodotto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    private String immagine;

    private Double prezzo;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(name = "is_pranzo", nullable = false)
    private Boolean isPranzo = true;

    @Column(name = "is_cena", nullable = false)
    private Boolean isCena = true;

    @Column(name = "is_ayce", nullable = false)
    private Boolean isAyce = true;

    @Column(name = "is_carta", nullable = false)
    private Boolean isCarta = true;

    @Column(name = "islimitedpartecipanti", nullable = false)
    private Boolean isLimitedPartecipanti = false;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
