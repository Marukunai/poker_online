package com.pokeronline.chat.controller;

import com.pokeronline.chat.dto.MensajeChatDTO;
import com.pokeronline.chat.service.ChatService;
import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @MessageMapping("/chat.enviar") // /app/chat.enviar
    @SendTo("/topic/chat")
    public MensajeChatDTO enviarMensaje(MensajeChatDTO dto) {
        User remitente = userRepository.findById(dto.getRemitenteId()).orElseThrow();

        if (remitente.isChatBloqueado()) {
            throw new RuntimeException("Estás bloqueado del chat");
        }

        return chatService.guardarMensaje(dto.getRemitenteId(), dto.getMesaId(), dto.getContenido());
    }
}