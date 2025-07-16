package com.pokeronline.torneo.equipos.controller;

import com.pokeronline.model.User;
import com.pokeronline.torneo.equipos.dto.CrearEquipoDTO;
import com.pokeronline.torneo.equipos.dto.EquipoTorneoDTO;
import com.pokeronline.torneo.equipos.model.EquipoTorneo;
import com.pokeronline.torneo.equipos.service.EquipoTorneoService;
import com.pokeronline.torneo.service.TorneoService;
import com.pokeronline.service.UserService;
import lombok.RequiredArgsConstructor;
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
                torneoService.getById(dto.getTorneoId()),
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
}