package com.pokeronline.torneo.controller;

import com.pokeronline.torneo.model.BlindLevel;
import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.repository.BlindLevelRepository;
import com.pokeronline.torneo.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/torneos/{torneoId}/blinds")
@RequiredArgsConstructor
public class BlindLevelController {

    private final BlindLevelRepository blindLevelRepository;
    private final TorneoService torneoService;

    @PostMapping
    public BlindLevel crearNivelCiegas(@PathVariable Long torneoId, @RequestBody BlindLevel nivel) {
        Torneo torneo = torneoService.obtenerTorneoPorId(torneoId)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
        nivel.setTorneo(torneo);
        return blindLevelRepository.save(nivel);
    }

    @GetMapping
    public List<BlindLevel> listarCiegas(@PathVariable Long torneoId) {
        Torneo torneo = torneoService.obtenerTorneoPorId(torneoId)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
        return torneo.getBlindLevels();
    }
}
