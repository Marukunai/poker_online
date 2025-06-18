package com.pokeronline.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void enviarMensajeMesa(Long mesaId, String tipo, Object payload) {
        MesaMessage message = new MesaMessage(tipo, mesaId, payload);
        messagingTemplate.convertAndSend("/topic/mesa/" + mesaId, message);
    }
}