package com.db.cmetal.be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CucinaMessage {
    private String tipoEvento;
    private String payload;
}
