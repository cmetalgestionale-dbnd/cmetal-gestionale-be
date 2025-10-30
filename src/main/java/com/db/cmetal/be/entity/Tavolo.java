package com.db.cmetal.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tavolo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tavolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private Integer numero;

    private Boolean attivo;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;


}
