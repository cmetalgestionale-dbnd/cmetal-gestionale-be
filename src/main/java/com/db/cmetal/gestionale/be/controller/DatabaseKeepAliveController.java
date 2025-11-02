package com.db.cmetal.gestionale.be.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.cmetal.gestionale.be.service.DatabaseKeepAliveService;

@RestController
@RequestMapping("/system")
public class DatabaseKeepAliveController {

    private final DatabaseKeepAliveService keepAliveService;

    public DatabaseKeepAliveController(DatabaseKeepAliveService keepAliveService) {
        this.keepAliveService = keepAliveService;
    }

    @PostMapping("/keepalive")
    public ResponseEntity<Map<String, Object>> triggerKeepAlive() {
        keepAliveService.keepDatabaseAlive();
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "executedAt", LocalDateTime.now().toString()
        ));
    }
}
