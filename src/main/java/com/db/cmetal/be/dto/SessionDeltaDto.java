package com.db.cmetal.be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionDeltaDto {
    private Long sessioneId;
    private Double lordo;
    private Double netto;
    private Double profit; // netto
    private Double costi;
}
