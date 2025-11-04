package com.db.cmetal.gestionale.be.service.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.db.cmetal.gestionale.be.dto.WebSocketMessageDto;
import com.db.cmetal.gestionale.be.service.WebSocketService;
import com.db.cmetal.gestionale.be.utils.Constants;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void broadcast(String tipoEvento, String payload) {
        WebSocketMessageDto message = new WebSocketMessageDto(tipoEvento, payload);
        messagingTemplate.convertAndSend(Constants.BROADCAST, message);
    }
}
