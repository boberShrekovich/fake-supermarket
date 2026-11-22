package ru.psu.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.psu.userservice.dto.AuthRequest;
import ru.psu.userservice.dto.AuthResponse;
import ru.psu.userservice.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Регистрация покупателя
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    // Вход в аккаунт
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return new ResponseEntity<>(authService.login(request), HttpStatus.OK);
    }
}
