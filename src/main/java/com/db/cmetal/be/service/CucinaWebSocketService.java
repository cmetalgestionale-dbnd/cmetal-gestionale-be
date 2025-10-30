package com.db.cmetal.be.service;

import com.db.cmetal.be.dto.CucinaMessage;

public interface CucinaWebSocketService {
    void handleCucinaMessage(CucinaMessage msg);
    void notifyNewOrder(Long ordineId);
    void notifyConsegnaChanged(Long ordineId, boolean consegnato);
}
