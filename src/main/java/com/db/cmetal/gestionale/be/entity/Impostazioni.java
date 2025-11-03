package com.db.cmetal.gestionale.be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "impostazioni")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Impostazioni {

    @Id
    @Column(length = 100)
    private String chiave;

    @Column(nullable = false, length = 255)
    private String valore;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;
}
