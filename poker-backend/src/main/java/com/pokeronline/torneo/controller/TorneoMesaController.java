package com.pokeronline.torneo.controller;

import com.pokeronline.model.Mesa;
import com.pokeronline.repository.MesaRepository;
import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.model.TorneoMesa;
import com.pokeronline.torneo.service.TorneoMesaService;
import com.pokeronline.torneo.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/torneos/mesas")
@RequiredArgsConstructor
public class TorneoMesaController {

    private final TorneoService torneoService;
    private final TorneoMesaService torneoMesaService;
    private final MesaRepository mesaRepository;

    @GetMapping("/{torneoId}")
    public List<TorneoMesa> listarMesasPorTorneo(@PathVariable Long torneoId) {
        Torneo torneo = torneoService.obtenerTorneoPorId(torneoId)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
        return torneoMesaService.obtenerMesasDelTorneo(torneo);
    }

    @PostMapping("/{torneoId}/vincular")
    public TorneoMesa vincularMesa(@PathVariable Long torneoId, @RequestParam Long mesaId) {
        Torneo torneo = torneoService.obtenerTorneoPorId(torneoId)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        return torneoMesaService.vincularMesaATorneo(torneo, mesa);
    }

    @DeleteMapping("/desvincular/{mesaId}")
    public void desvincularMesa(@PathVariable Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));
        torneoMesaService.desvincularMesa(mesa);
    }

    @GetMapping("/buscar")
    public TorneoMesa obtenerTorneoMesa(@RequestParam Long torneoId, @RequestParam Long mesaId) {
        Torneo torneo = torneoService.obtenerTorneoPorId(torneoId)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        return torneoMesaService.obtenerPorTorneoYMesa(torneo, mesa)
                .orElseThrow(() -> new RuntimeException("Vínculo torneo-mesa no encontrado"));
    }
}