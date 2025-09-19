package com.pokeronline.torneo.controller;

import com.pokeronline.torneo.dto.EsperaTorneoDTO;
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
    public EsperaTorneoDTO registrar(@RequestParam Long torneoId, @RequestParam Long userId) {
        EsperaTorneo e = esperaTorneoService.registrarPresencia(torneoId, userId);
        return EsperaTorneoDTO.fromEntity(e);
    }

    @GetMapping("/{torneoId}")
    public List<EsperaTorneoDTO> getEsperando(@PathVariable Long torneoId) {
        return esperaTorneoService.obtenerEsperando(torneoId)
                .stream()
                .map(EsperaTorneoDTO::fromEntity)
                .toList();
    }

    @DeleteMapping("/{torneoId}")
    public void limpiar(@PathVariable Long torneoId) {
        esperaTorneoService.limpiarEsperaTorneo(torneoId);
    }
}