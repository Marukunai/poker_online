package com.pokeronline.torneo.equipos.controller;

import com.pokeronline.model.User;
import com.pokeronline.service.UserService;
import com.pokeronline.torneo.equipos.dto.AgregarMiembroDTO;
import com.pokeronline.torneo.equipos.dto.MiembroEquipoDTO;
import com.pokeronline.torneo.equipos.model.MiembroEquipoTorneo;
import com.pokeronline.torneo.equipos.service.EquipoTorneoService;
import com.pokeronline.torneo.equipos.service.MiembroEquipoTorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/torneos/equipos/miembros")
@RequiredArgsConstructor
public class MiembroEquipoTorneoController {

    private final MiembroEquipoTorneoService miembroService;
    private final EquipoTorneoService equipoService;
    private final UserService userService;

    @PostMapping
    public MiembroEquipoDTO agregarMiembro(@RequestBody AgregarMiembroDTO dto,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        Long userIdSolicitante = userService.getUserIdFromUserDetails(userDetails);

        User user = userService.getById(dto.getUserId());

        MiembroEquipoTorneo miembro = miembroService.anadirMiembro(
                equipoService.obtenerEquipo(dto.getEquipoId()),
                user,
                userIdSolicitante
        );
        return MiembroEquipoDTO.fromEntity(miembro);
    }

    @GetMapping("/equipo/{equipoId}")
    public List<MiembroEquipoDTO> listarMiembros(@PathVariable Long equipoId) {
        return miembroService.obtenerMiembrosEquipo(equipoId)
                .stream()
                .map(MiembroEquipoDTO::fromEntity)
                .toList();
    }

    @DeleteMapping("/{miembroId}")
    public void eliminarMiembro(@PathVariable Long miembroId,
                                @AuthenticationPrincipal UserDetails userDetails) {
        Long userIdSolicitante = userService.getUserIdFromUserDetails(userDetails);
        miembroService.eliminarMiembro(miembroId, userIdSolicitante);
    }

    @DeleteMapping("/equipo/{equipoId}")
    public void eliminarTodosDeEquipo(@PathVariable Long equipoId,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        Long userIdSolicitante = userService.getUserIdFromUserDetails(userDetails);
        miembroService.eliminarTodosLosMiembrosDeEquipo(equipoId, userIdSolicitante);
    }

    @GetMapping("/equipo/{equipoId}/user/{userId}")
    public MiembroEquipoDTO obtenerMiembroEspecifico(@PathVariable Long equipoId,
                                                     @PathVariable Long userId) {
        MiembroEquipoTorneo miembro = miembroService.obtenerMiembro(equipoId, userId);
        return MiembroEquipoDTO.fromEntity(miembro);
    }
}