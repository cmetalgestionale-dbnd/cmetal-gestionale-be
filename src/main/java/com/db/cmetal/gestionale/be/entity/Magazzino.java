package com.db.cmetal.gestionale.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "magazzino")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Magazzino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;
}
