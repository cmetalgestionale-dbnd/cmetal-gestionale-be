package com.db.cmetal.gestionale.be.service;

import org.springframework.http.ResponseEntity;

import com.db.cmetal.gestionale.be.dto.LoginRequest;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    ResponseEntity<?> login(LoginRequest request, HttpServletResponse response);
    ResponseEntity<?> logout(HttpServletResponse response);
    ResponseEntity<?> getCurrentUser();
}
