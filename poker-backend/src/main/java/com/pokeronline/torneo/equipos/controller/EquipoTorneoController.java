package com.pokeronline.torneo.equipos.controller;

import com.pokeronline.model.User;
import com.pokeronline.torneo.equipos.dto.*;
import com.pokeronline.torneo.equipos.model.EquipoTorneo;
import com.pokeronline.torneo.equipos.service.EquipoTorneoService;
import com.pokeronline.torneo.service.TorneoService;
import com.pokeronline.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/torneos/equipos")
@RequiredArgsConstructor
public class EquipoTorneoController {

    private final EquipoTorneoService equipoTorneoService;
    private final UserService userService;
    private final TorneoService torneoService;

    @PostMapping
    public EquipoTorneo crearEquipo(@RequestBody CrearEquipoDTO dto) {
        User capitan = userService.getById(dto.getCapitanId());
        return equipoTorneoService.crearEquipo(
                torneoService.obtenerTorneoPorId(dto.getTorneoId())
                        .orElseThrow(() -> new RuntimeException("Torneo no encontrado")),
                dto.getNombreEquipo(),
                capitan
        );
    }

    @GetMapping("/torneo/{torneoId}")
    public List<EquipoTorneoDTO> listarEquipos(@PathVariable Long torneoId) {
        return equipoTorneoService.listarEquiposDeTorneo(torneoId)
                .stream()
                .map(EquipoTorneoDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{equipoId}")
    public EquipoTorneoDTO obtenerEquipo(@PathVariable Long equipoId) {
        return EquipoTorneoDTO.fromEntity(equipoTorneoService.obtenerEquipo(equipoId));
    }

    @PutMapping("/{equipoId}/puntos/{puntos}")
    public EquipoTorneoDTO actualizarPuntos(@PathVariable Long equipoId, @PathVariable int puntos) {
        return EquipoTorneoDTO.fromEntity(equipoTorneoService.actualizarPuntos(equipoId, puntos));
    }

    @DeleteMapping("/{equipoId}")
    public void eliminarEquipo(@PathVariable Long equipoId) {
        equipoTorneoService.eliminarEquipo(equipoId);
    }

    // TODO (postman)
    @PutMapping("/actualizar-capitan")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> actualizarCapitan(
            @RequestBody UpdateCapitanDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserIdFromUserDetails(userDetails);
        equipoTorneoService.actualizarCapitan(dto, userId);
        return ResponseEntity.ok("Capitán actualizado correctamente");
    }

    @GetMapping("/torneo/{torneoId}/ranking")
    public List<RankingEquipoDTO> rankingEquipos(@PathVariable Long torneoId) {
        return equipoTorneoService.obtenerRankingEquipos(torneoId);
    }

    @GetMapping("/ranking/global")
    public List<RankingEquipoDTO> rankingGlobal() {
        return equipoTorneoService.obtenerRankingGlobal();
    }

    @GetMapping("/ranking/anual/{year}")
    public List<RankingEquipoDTO> rankingAnual(@PathVariable int year) {
        return equipoTorneoService.obtenerRankingAnual(year);
    }

    @GetMapping("/ranking/mensual/{year}/{mes}")
    public List<RankingEquipoDTO> rankingMensual(@PathVariable int year, @PathVariable int mes) {
        return equipoTorneoService.obtenerRankingMensual(year, mes);
    }

    @GetMapping("/{equipoId}/estadisticas")
    public EquipoEstadisticasDTO obtenerEstadisticasEquipo(@PathVariable Long equipoId) {
        return equipoTorneoService.obtenerEstadisticasEquipo(equipoId);
    }

    @GetMapping("/{equipoId}/historial")
    public List<HistorialEquipoDTO> historialEquipo(@PathVariable Long equipoId) {
        return equipoTorneoService.obtenerHistorialEquipo(equipoId);
    }

    // end
}