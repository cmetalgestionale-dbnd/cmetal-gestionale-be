package com.db.cmetal.gestionale.be.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class TabellaMarciaDto {
    private LocalDate data;
    private LocalDate dataRientro;
    private String targa;
    private LocalTime orarioUscita;
    private LocalTime orarioRientro;
    private Integer kmPartenza;
    private Integer kmRientro;
    private Boolean guasti;
    private Boolean rifornimento;
    private BigDecimal importoRifornimento;
    private String metodoPagamento;
    private String controlliPrePartenza;
    private String guastiRotture;
    private String note;
    private Boolean invia;
}
