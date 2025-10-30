package com.db.cmetal.be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TavoloMessagePayload {
    private Long prodottoId;
    private Integer quantita;
}