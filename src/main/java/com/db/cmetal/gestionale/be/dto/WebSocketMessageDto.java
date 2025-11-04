package com.db.cmetal.gestionale.be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketMessageDto {
    private String tipoEvento;
    private String payload;
}
