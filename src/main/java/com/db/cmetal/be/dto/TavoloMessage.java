package com.db.cmetal.be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TavoloMessage {
    private String tipoEvento; // ADD_ITEM_TEMP, REMOVE_ITEM_TEMP, UPDATE_TEMP, ORDER_SENT
    private Long sessioneId;
    private String payload; // JSON string (TavoloMessagePayload o stato completo)
}