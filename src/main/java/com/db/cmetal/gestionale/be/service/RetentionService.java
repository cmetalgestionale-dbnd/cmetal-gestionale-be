package com.db.cmetal.gestionale.be.service;

import com.db.cmetal.gestionale.be.controller.RetentionController.SpaceUsageResponse;

public interface RetentionService {
    int cleanupAssegnazioni();
    int cleanupCommesse();
    int cleanupClienti();
    int cleanupUtenti();
    SpaceUsageResponse getCurrentSpaceUsage();
}
