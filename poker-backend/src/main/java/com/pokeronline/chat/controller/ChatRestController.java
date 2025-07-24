package com.pokeronline.chat.controller;

import com.pokeronline.chat.dto.MensajeChatDTO;
import com.pokeronline.chat.dto.ReporteMensajeDTO;
import com.pokeronline.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/reportar")
    public ResponseEntity<String> reportarMensaje(@RequestBody ReporteMensajeDTO dto) {
        // Aquí puedes guardar en una tabla de reportes o simplemente registrar en logs
        System.out.printf("🔔 Mensaje %d reportado por %d: %s%n", dto.getMensajeId(), dto.getReportadoPorId(), dto.getRazon());

        // Opción: Si quieres actuar, podrías iniciar una investigación automática.
        return ResponseEntity.ok("Mensaje reportado correctamente");
    }
}