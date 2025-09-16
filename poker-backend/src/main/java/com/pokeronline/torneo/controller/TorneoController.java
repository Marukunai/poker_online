package com.pokeronline.torneo.controller;

import com.pokeronline.estadisticas.dto.TorneoHistorialDTO;
import com.pokeronline.estadisticas.service.EstadisticasService;
import com.pokeronline.torneo.dto.CrearTorneoDTO;
import com.pokeronline.torneo.dto.TorneoDTO;
import com.pokeronline.torneo.model.*;
import com.pokeronline.torneo.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/torneos")
@RequiredArgsConstructor
public class TorneoController {

    private final EstadisticasService estadisticasService;
    private final TorneoService torneoService;

    @GetMapping
    public List<TorneoDTO> listarTorneos() {
        return torneoService.listarTorneos()
                .stream().map(TorneoDTO::toDTO).toList();
    }

    @GetMapping("/torneo")
    public Optional<Torneo> listarTorneoPorNombre(@RequestParam String nombre) {
        return torneoService.obtenerTorneoPorNombre(nombre);
    }

    @GetMapping("/pendientes")
    public List<TorneoDTO> listarPendientes() {
        return torneoService.listarTorneosPendientes().stream().map(TorneoDTO::toDTO).toList();
    }

    @GetMapping("/encurso")
    public List<TorneoDTO> listarEnCurso() {
        return torneoService.listarTorneosEnCurso().stream().map(TorneoDTO::toDTO).toList();
    }

    @GetMapping("/finalizados")
    public List<TorneoDTO> listarFinalizados() {
        return torneoService.listarTorneosFinalizados().stream().map(TorneoDTO::toDTO).toList(); // Asegúrate de tenerlo en el servicio
    }

    @GetMapping("/usuario/{userId}/historial")
    public List<TorneoHistorialDTO> historialUsuario(@PathVariable Long userId) {
        return estadisticasService.obtenerHistorialTorneos(userId); // ya tienes este servicio definido
    }

    @GetMapping("/{id}/estado")
    public Map<String, Object> estadoTorneo(@PathVariable Long id) {
        Torneo torneo = torneoService.obtenerTorneoPorId(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        List<ParticipanteTorneo> activos = torneo.getParticipantes().stream()
                .filter(p -> !p.isEliminado()).toList();

        int rondaMax = torneo.getMesas().stream()
                .mapToInt(TorneoMesa::getRonda).max().orElse(1);

        Map<String, Object> estado = new HashMap<>();
        estado.put("nombre", torneo.getNombre());
        estado.put("estado", torneo.getEstado());
        estado.put("participantesActivos", activos.size());
        estado.put("rondaActual", rondaMax);
        estado.put("premioTotal", torneo.getPremioTotal());
        return estado;
    }

    @GetMapping("/{id}/nivel-ciegas")
    public BlindLevel getNivelCiegasActual(@PathVariable Long id) {
        Torneo torneo = torneoService.obtenerTorneoPorId(id).orElseThrow();
        List<BlindLevel> niveles = torneo.getBlindLevels();
        int nivelActual = torneo.getNivelCiegasActual();
        if (nivelActual < niveles.size()) {
            return niveles.get(nivelActual);
        } else {
            return niveles.get(niveles.size() - 1); // último nivel
        }
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
    public TorneoDTO obtener(@PathVariable Long id) {
        var torneo = torneoService.obtenerTorneoPorId(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
        return TorneoDTO.toDTO(torneo);
    }
}