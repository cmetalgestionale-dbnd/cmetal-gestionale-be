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
@Table(name = "allegato")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Allegato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_file", nullable = false)
    private String nomeFile;

    @Column(name = "tipo_file", nullable = false)
    private String tipoFile;

    @Column(name = "storage_path", nullable = false, columnDefinition = "TEXT")
    private String storagePath;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Utente createdBy;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
