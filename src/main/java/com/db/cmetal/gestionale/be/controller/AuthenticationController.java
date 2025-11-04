package com.db.cmetal.gestionale.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.cmetal.gestionale.be.dto.LoginRequestDto;
import com.db.cmetal.gestionale.be.service.AuthenticationService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    
    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        return authenticationService.login(request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        return authenticationService.logout(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        return authenticationService.getCurrentUser();
    }
}
