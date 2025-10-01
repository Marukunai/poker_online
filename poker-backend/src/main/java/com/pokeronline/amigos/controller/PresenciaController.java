package com.pokeronline.amigos.controller;

import com.pokeronline.amigos.model.EstadoConexion;
import com.pokeronline.amigos.model.EstadoPresencia;
import com.pokeronline.amigos.service.PresenciaService;
import com.pokeronline.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presencia")
@RequiredArgsConstructor
public class PresenciaController {

    private final PresenciaService presenciaService;
    private final UserService userService;

    // Estados de mis amigos
    @GetMapping("/amigos")
    public List<EstadoPresencia> estadosAmigos(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        return presenciaService.obtenerEstadosAmigos(userId);
    }

    // Actualizar mi estado (ej. NO_MOLESTAR / AUSENTE / ONLINE con detalle)
    @PostMapping("/estado")
    public String actualizarEstado(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CambiarEstadoRequest req
    ) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        presenciaService.actualizarEstado(userId, req.getEstado(), req.getDetalle());
        return "Estado actualizado";
    }

    // Heartbeat (actividad)
    @PostMapping("/heartbeat")
    public String heartbeat(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        presenciaService.actualizarActividad(userId);
        return "OK";
    }

    @Data
    public static class CambiarEstadoRequest {
        private EstadoConexion estado;
        private String detalle;
    }
}