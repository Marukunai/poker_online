package com.pokeronline.amigos.service;

import com.pokeronline.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final WebSocketService webSocketService;

    /* API flexible: acepta enums (p.ej. TipoNotificacion) */
    public void enviarNotificacion(Long userId,
                                   Enum<?> tipo,
                                   String titulo,
                                   String cuerpo,
                                   Map<String, Object> data) {
        enviarNotificacion(userId, tipo != null ? tipo.name() : "DESCONOCIDO", titulo, cuerpo, data);
    }

    /* Versión con tipo String */
    public void enviarNotificacion(Long userId,
                                   String tipo,
                                   String titulo,
                                   String cuerpo,
                                   Map<String, Object> data) {
        Map<String, Object> payload = Map.of(
                "tipo", "NOTIFICACION",
                "subtipo", tipo,
                "titulo", titulo,
                "cuerpo", cuerpo,
                "data", data != null ? data : Map.of()
        );

        // WebSocket (cuando lo actives)
        webSocketService.enviarMensajeJugador(userId, "notificacion", payload);

        // Log de respaldo
        log.info("[NOTIFICACION] toUser={} tipo={} titulo='{}'", userId, tipo, titulo);
    }

    /* Mock de push notification: ahora mismo usa el mismo canal WS + log */
    public void enviarPushNotification(Long userId,
                                       String titulo,
                                       String cuerpo,
                                       Map<String, Object> data) {
        Map<String, Object> payload = Map.of(
                "tipo", "PUSH",
                "titulo", titulo,
                "cuerpo", cuerpo,
                "data", data != null ? data : Map.of()
        );

        webSocketService.enviarMensajeJugador(userId, "push", payload);
        log.info("[PUSH] toUser={} titulo='{}'", userId, titulo);
    }
}