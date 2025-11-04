package com.db.cmetal.gestionale.be.service;

public interface WebSocketService {
    void broadcast(String tipoEvento, String payload);
}