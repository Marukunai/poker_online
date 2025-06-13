package com.pokeronline.controller;

import com.pokeronline.dto.LoginDTO;
import com.pokeronline.dto.RegisterDTO;
import com.pokeronline.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO dto) {
        String token = authService.register(dto);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO dto) {
        String token = authService.login(dto);
        return ResponseEntity.ok(token);
    }
}
