package com.db.cmetal.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.db.cmetal.be.dto.CucinaMessage;
import com.db.cmetal.be.service.CucinaWebSocketService;
import com.db.cmetal.be.utils.AuthUtils;
import com.db.cmetal.be.utils.Constants;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CucinaWebSocketController {
	
	@Autowired
    CucinaWebSocketService cucinaWebSocketService;
	
	@Autowired
	AuthUtils authUtils;

    @MessageMapping("/cucina")
    public void handleCucinaMessage(CucinaMessage msg) {
    	authUtils.getCurrentUserOrThrow(Constants.ROLE_DIPEN, Constants.ROLE_ADMIN);
        cucinaWebSocketService.handleCucinaMessage(msg);
    }
}
