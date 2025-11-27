package com.db.cmetal.gestionale.be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "inventario_movimento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioMovimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inventario_id", nullable = false)
    private InventarioArticolo inventario;

    @Column(nullable = false)
    private BigDecimal quantita;

    @Column(name = "movimento_at", nullable = false)
    private OffsetDateTime movimentoAt;

    @Column(columnDefinition = "TEXT")
    private String descrizione;
}
