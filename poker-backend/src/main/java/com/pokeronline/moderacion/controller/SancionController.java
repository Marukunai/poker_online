package com.pokeronline.moderacion.controller;

import com.pokeronline.moderacion.dto.CrearSancionDTO;
import com.pokeronline.moderacion.dto.SancionDTO;
import com.pokeronline.moderacion.service.SancionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sanciones")
@RequiredArgsConstructor
public class SancionController {

    private final SancionService sancionService;

    @PostMapping
    public SancionDTO asignarSancion(@RequestBody CrearSancionDTO dto) {
        return sancionService.asignarSancion(dto);
    }

    @GetMapping("/usuario/{userId}")
    public List<SancionDTO> obtenerSancionesDeUsuario(@PathVariable Long userId) {
        return sancionService.obtenerSancionesUsuario(userId);
    }

    @DeleteMapping("/{sancionId}")
    public void desactivarSancion(@PathVariable Long sancionId) {
        sancionService.desactivarSancion(sancionId);
    }
}