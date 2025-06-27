package com.pokeronline.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessagingException; // <--- ¡Asegúrate de que esta sea la importación!
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public void enviarMensajeMesa(Long mesaId, String tipo, Object payload) {
        MesaMessage message = new MesaMessage(tipo, mesaId, payload);

        /*try {
            messagingTemplate.convertAndSend("/topic/mesa/" + mesaId, message);
            logger.info("Mensaje de tipo '{}' enviado exitosamente a la mesa {}.", tipo, mesaId);

        } catch (Exception | MessagingException e) {
            logger.error("Error al enviar mensaje de tipo '{}' a la mesa {}: {}", tipo, mesaId, e.getMessage(), e);
        }*/
    }
}