package com.db.cmetal.gestionale.be.service.impl;

import java.time.LocalDateTime;
import java.util.logging.Logger;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.cmetal.gestionale.be.service.DatabaseKeepAliveService;

import jakarta.annotation.PostConstruct;

@Service
public class DatabaseKeepAliveServiceImpl implements DatabaseKeepAliveService {

    private static final Logger log = Logger.getLogger(DatabaseKeepAliveServiceImpl.class.getName());
    private final JdbcTemplate jdbcTemplate;

    public DatabaseKeepAliveServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        log.info("DatabaseKeepAliveService initialized.");
    }

    @Scheduled(fixedRate = 3600000) // 1h in ms
    @Transactional(readOnly = true)
    @Override
    public void keepDatabaseAlive() {
        try {
            jdbcTemplate.execute("SELECT 1");
            log.info("Keep-alive query executed at " + LocalDateTime.now());
        } catch (Exception e) {
            log.warning("Keep-alive query failed: " + e.getMessage());
        }
    }
}
