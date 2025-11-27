package com.db.cmetal.gestionale.be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "inventario_articolo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioArticolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "magazzino_id", nullable = false)
    private Magazzino magazzino;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    @Column(nullable = false)
    private BigDecimal prezzoUnitario = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal quantitaMagazzino = BigDecimal.ZERO;

    @Column(nullable = false, insertable = false, updatable = false)
    private BigDecimal valoreInventario;

    @Column(nullable = false)
    private BigDecimal livelloRiordino = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal quantitaInRiordino = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean fuoriProduzione = false;
}
