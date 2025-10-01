package com.pokeronline.amigos.service;

import com.pokeronline.amigos.model.EstadoInvitacion;
import com.pokeronline.amigos.model.InvitacionPartida;
import com.pokeronline.amigos.repository.InvitacionPartidaRepository;
import com.pokeronline.websocket.WebSocketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitacionService {

    private final InvitacionPartidaRepository invitacionRepository;
    private final WebSocketService webSocketService;

    /**
     * Cancela TODAS las invitaciones PENDIENTE entre userId y amigoId (en ambos sentidos).
     */
    @Transactional
    public void cancelarInvitacionesEntre(Long userId, Long amigoId) {
        // Debes implementar este método en tu repository.
        // Alternativa: dos consultas (user->amigo y amigo->user)
        List<InvitacionPartida> pendientes = invitacionRepository
                .findPendientesEntreUsuarios(userId, amigoId);

        if (pendientes.isEmpty()) {
            log.info("[INVITACIONES] No hay pendientes entre {} y {}", userId, amigoId);
            return;
        }

        LocalDateTime ahora = LocalDateTime.now();
        pendientes.forEach(inv -> {
            inv.setEstado(EstadoInvitacion.CANCELADA);
            inv.setFechaRespuesta(ahora);
        });
        invitacionRepository.saveAll(pendientes);

        // Notificaciones WS a ambos usuarios
        webSocketService.enviarMensajeJugador(userId, "invitaciones",
                Map.of("tipo", "INVITACIONES_CANCELADAS", "con", amigoId));

        webSocketService.enviarMensajeJugador(amigoId, "invitaciones",
                Map.of("tipo", "INVITACIONES_CANCELADAS", "con", userId));

        log.info("[INVITACIONES] Canceladas {} invitaciones entre {} y {}", pendientes.size(), userId, amigoId);
    }

    public int expirarPendientes() {
        var ahora = LocalDateTime.now();
        var pendientes = invitacionRepository
                .findByEstadoAndFechaExpiracionBefore(EstadoInvitacion.PENDIENTE, ahora);
        pendientes.forEach(inv -> inv.setEstado(EstadoInvitacion.EXPIRADA));
        invitacionRepository.saveAll(pendientes);
        return pendientes.size();
    }

    /* Hooks para futuro (crear/aceptar/rechazar/expirar)
    public InvitacionPartidaDTO crearInvitacion(...) { ... }
    public void aceptarInvitacion(Long invitacionId, Long userId) { ... }
    public void rechazarInvitacion(Long invitacionId, Long userId) { ... }
    public void expirarInvitacionesAntiguas() { ... }
    */
}