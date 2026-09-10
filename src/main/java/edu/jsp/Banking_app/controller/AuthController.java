package edu.jsp.Banking_app.controller;

import org.springframework.web.bind.annotation.*;

import edu.jsp.Banking_app.dto.LoginRequest;
import edu.jsp.Banking_app.dto.LoginResponse;
import edu.jsp.Banking_app.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }
}