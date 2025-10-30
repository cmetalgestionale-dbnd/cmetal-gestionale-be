package com.db.cmetal.be.service.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.db.cmetal.be.dto.CucinaMessage;
import com.db.cmetal.be.service.CucinaWebSocketService;
import com.db.cmetal.be.utils.Constants;

@Service
public class CucinaWebSocketServiceImpl implements CucinaWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public CucinaWebSocketServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void handleCucinaMessage(CucinaMessage msg) {
        // ribatto verso la room di cucina
        messagingTemplate.convertAndSend("/topic/cucina", msg);
    }

    @Override
    public void notifyNewOrder(Long ordineId) {
        messagingTemplate.convertAndSend(
            "/topic/cucina",
            new CucinaMessage(Constants.MSG_ORDER_SENT, String.valueOf(ordineId))
        );
    }

    @Override
    public void notifyConsegnaChanged(Long ordineId, boolean consegnato) {
        messagingTemplate.convertAndSend(
            "/topic/cucina",
            new CucinaMessage(
                Constants.MSG_CONSEGNA_CHANGED,
                "{ \"ordineId\": " + ordineId + ", \"consegnato\": " + consegnato + " }"
            )
        );
    }
}
