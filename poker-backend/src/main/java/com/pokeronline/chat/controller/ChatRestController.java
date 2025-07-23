package com.pokeronline.chat.controller;

import com.pokeronline.chat.dto.MensajeChatDTO;
import com.pokeronline.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/historial/{mesaId}")
    public List<MensajeChatDTO> obtenerHistorial(@PathVariable Long mesaId) {
        return chatService.obtenerHistorialPorMesa(mesaId);
    }
}