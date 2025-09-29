package com.pokeronline.logros.controller;

import com.pokeronline.logros.dto.LogroDTO;
import com.pokeronline.logros.service.LogroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logros")
@RequiredArgsConstructor
public class LogroController {

    private final LogroService logroService;

    @GetMapping
    public List<LogroDTO> obtenerTodos() {
        return logroService.obtenerTodosLosLogros();
    }

    @PostMapping("/otorgar")
    public ResponseEntity<?> otorgarLogro(
            @RequestParam Long userId,
            @RequestParam String nombreLogro
    ) {
        logroService.otorgarLogroSiNoTiene(userId, nombreLogro);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Logro otorgado correctamente",
                        "userId", userId,
                        "logro", nombreLogro
                )
        );
    }
}