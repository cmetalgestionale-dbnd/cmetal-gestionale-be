package com.db.cmetal.gestionale.be.dto;

import lombok.Data;

@Data
public class AssegnazioneDto {
    private Long commessaId;
    private Long clienteId;
    private Long utenteId;
    private String note;
    private String assegnazioneAt; // ISO string dal frontend
}
