package com.db.cmetal.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @Column(length = 50)
    private String chiave;

    @Column(length = 255, nullable = false)
    private String valore;

    @Column(length = 20, nullable = false)
    private String tipo; // 'int' o 'boolean'

    @Column(name = "min_value")
    private Integer minValue;

    @Column(name = "max_value")
    private Integer maxValue;

    @Column(columnDefinition = "TEXT")
    private String descrizione;
}
