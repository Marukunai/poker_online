package com.pokeronline.torneo.controller;

import com.pokeronline.torneo.dto.CrearTorneoDTO;
import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.model.TorneoEstado;
import com.pokeronline.torneo.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/torneos")
@RequiredArgsConstructor
public class TorneoController {

    private final TorneoService torneoService;

    @GetMapping
    public List<Torneo> listarTorneos() {
        return torneoService.listarTorneos();
    }

    @GetMapping("/torneo")
    public Optional<Torneo> listarTorneoPorNombre(@RequestParam String nombre) {
        return torneoService.obtenerTorneoPorNombre(nombre);
    }

    @PostMapping
    public Torneo crearTorneo(@RequestBody CrearTorneoDTO dto) {
        return torneoService.crearTorneo(dto);
    }

    @PatchMapping("/{id}/estado")
    public void cambiarEstado(@PathVariable Long id, @RequestParam TorneoEstado nuevoEstado) {
        torneoService.obtenerTorneoPorId(id).ifPresent(torneo -> {
            torneoService.actualizarEstadoTorneo(torneo, nuevoEstado);
        });
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        torneoService.eliminarTorneo(id);
    }

    @GetMapping("/{id}")
    public Torneo obtener(@PathVariable Long id) {
        return torneoService.obtenerTorneoPorId(id).orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
    }
}