package com.db.cmetal.be.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import com.db.cmetal.be.dto.LoginRequest;

public interface AuthenticationService {
    ResponseEntity<?> login(LoginRequest request, HttpServletResponse response);
    ResponseEntity<?> loginTavolo(Integer tavoloNum, HttpServletResponse response);
    ResponseEntity<?> logout(HttpServletResponse response);
    ResponseEntity<?> getCurrentUser();
}
