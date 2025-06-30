package com.pokeronline.websocket;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    private final SimpMessagingTemplate messagingTemplate;

    public void enviarMensajeMesa(Long mesaId, String tipo, Object payload) {
        // WebSocket desactivado temporalmente (se usará cuando el frontend esté listo)

        // MesaMessage message = new MesaMessage(tipo, mesaId, payload);
        // messagingTemplate.convertAndSend("/topic/mesa/" + mesaId, message);

        // logger.info("Simulación de envío de mensaje tipo '{}' a la mesa {}.", tipo, mesaId);
    }
}