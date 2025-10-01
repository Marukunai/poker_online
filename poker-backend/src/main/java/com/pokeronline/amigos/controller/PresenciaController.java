package com.pokeronline.amigos.controller;

import com.pokeronline.amigos.model.EstadoConexion;
import com.pokeronline.amigos.model.EstadoPresencia;
import com.pokeronline.amigos.service.PresenciaService;
import com.pokeronline.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    // === NUEVO: Mi estado actual (rápido)
    @GetMapping("/me")
    public ResponseEntity<EstadoPresencia> miEstado(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(presenciaService.obtenerEstado(userId));
    }

    // Estados de mis amigos (tu endpoint original, mantenemos ruta)
    @GetMapping("/amigos")
    public ResponseEntity<List<EstadoPresencia>> estadosAmigos(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(presenciaService.obtenerEstadosAmigos(userId));
    }

    // Actualizar mi estado (tu endpoint original). Si prefieres, puedes duplicar con @PutMapping("/estado")
    @PostMapping("/estado")
    public ResponseEntity<String> actualizarEstado(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CambiarEstadoRequest req
    ) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        presenciaService.actualizarEstado(userId, req.getEstado(), req.getDetalle());

        // Si en el futuro quieres admitir mesaId/torneoId aquí:
        // var estado = presenciaService.obtenerEstado(userId);
        // estado.setMesaId(req.getMesaId());
        // estado.setTorneoId(req.getTorneoId());

        return ResponseEntity.ok("Estado actualizado");
    }

    // Heartbeat (actividad) — tu endpoint original
    @PostMapping("/heartbeat")
    public ResponseEntity<String> heartbeat(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        presenciaService.actualizarActividad(userId);
        return ResponseEntity.ok("OK");
    }

    // === NUEVO: Toggle No Molestar rápido
    @PostMapping("/no-molestar")
    public ResponseEntity<String> setNoMolestar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam boolean enabled
    ) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        presenciaService.actualizarEstado(
                userId,
                enabled ? EstadoConexion.NO_MOLESTAR : EstadoConexion.ONLINE,
                enabled ? "No molestar" : "En línea"
        );
        return ResponseEntity.ok(enabled ? "No molestar activado" : "No molestar desactivado");
    }

    @Data
    public static class CambiarEstadoRequest {
        private EstadoConexion estado;
        private String detalle;
        // Opcional si decides usarlos:
        // private Long mesaId;
        // private Long torneoId;
    }
}