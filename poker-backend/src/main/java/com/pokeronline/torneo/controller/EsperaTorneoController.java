package com.pokeronline.torneo.controller;

import com.pokeronline.torneo.model.EsperaTorneo;
import com.pokeronline.torneo.service.EsperaTorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/torneos/espera")
@RequiredArgsConstructor
public class EsperaTorneoController {

    private final EsperaTorneoService esperaTorneoService;

    @PostMapping("/registrar")
    public EsperaTorneo registrar(@RequestParam Long torneoId, @RequestParam Long userId) {
        return esperaTorneoService.registrarPresencia(torneoId, userId);
    }

    @GetMapping("/{torneoId}")
    public List<EsperaTorneo> getEsperando(@PathVariable Long torneoId) {
        return esperaTorneoService.obtenerEsperando(torneoId);
    }

    @DeleteMapping("/{torneoId}")
    public void limpiar(@PathVariable Long torneoId) {
        esperaTorneoService.limpiarEsperaTorneo(torneoId);
    }
}