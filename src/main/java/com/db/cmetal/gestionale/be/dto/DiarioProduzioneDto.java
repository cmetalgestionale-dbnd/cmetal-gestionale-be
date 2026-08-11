package com.db.cmetal.gestionale.be.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class DiarioProduzioneDto {
    private Long commessaId;
    private String clienteCommessa;
    private LocalDate data;
    private LocalTime oraInizio;
    private LocalTime oraFine;
    private String descrizione;
    private String tipoLavorazione;
    private String attrezzaturaDanneggiata;
    private String materialeUtilizzatoExtra;
    private String consumabiliPrelevati;
    private Boolean invia;
}
