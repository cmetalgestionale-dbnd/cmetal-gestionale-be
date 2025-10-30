package com.db.cmetal.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.cmetal.be.dto.LoginRequest;
import com.db.cmetal.be.service.AuthenticationService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
	
	@Autowired
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        return authenticationService.login(request, response);
    }

    @PostMapping("/login-tavolo/{tavoloNum}")
    public ResponseEntity<?> loginTavolo(@PathVariable Integer tavoloNum, HttpServletResponse response) {
        return authenticationService.loginTavolo(tavoloNum, response);
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
