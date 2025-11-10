package com.db.cmetal.gestionale.be.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.cmetal.gestionale.be.service.RetentionService;
import com.db.cmetal.gestionale.be.service.WebSocketService;
import com.db.cmetal.gestionale.be.utils.Constants;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/retention")
@RequiredArgsConstructor
public class RetentionController {

    private final RetentionService retentionService;
    private final WebSocketService wsService;

    public static record RetentionRequest(List<String> entities) {}
    public static record SpaceUsageResponse(String dbSize, String storageSize) {}

    @PostMapping("/run")
    public String runRetention(@RequestBody RetentionRequest request) {
        StringBuilder report = new StringBuilder("Pulizia completata:\n");

        if (request.entities().contains("assegnazioni")) {
            int count = retentionService.cleanupAssegnazioni();
            report.append("- Assegnazioni eliminate: ").append(count).append("\n");
        }
        if (request.entities().contains("commesse")) {
            int count = retentionService.cleanupCommesse();
            report.append("- Commesse eliminate: ").append(count).append("\n");
        }
        if (request.entities().contains("clienti")) {
            int count = retentionService.cleanupClienti();
            report.append("- Clienti eliminati: ").append(count).append("\n");
        }
        if (request.entities().contains("utenti")) {
            int count = retentionService.cleanupUtenti();
            report.append("- Utenze eliminate: ").append(count).append("\n");
        }

        wsService.broadcast(Constants.BROADCAST, null);
        return report.toString();
    }

    @GetMapping("/space")
    public SpaceUsageResponse getCurrentSpaceUsage() {
        return retentionService.getCurrentSpaceUsage();
    }
}
