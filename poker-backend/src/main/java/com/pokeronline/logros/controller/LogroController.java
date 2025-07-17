package com.pokeronline.logros.controller;

import com.pokeronline.logros.dto.LogroDTO;
import com.pokeronline.logros.service.LogroService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public void otorgarLogro(
            @RequestParam Long userId,
            @RequestParam String nombreLogro
    ) {
        logroService.otorgarLogroSiNoTiene(userId, nombreLogro);
    }
}