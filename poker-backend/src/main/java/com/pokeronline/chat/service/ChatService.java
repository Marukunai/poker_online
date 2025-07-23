package com.pokeronline.chat.service;

import com.pokeronline.chat.dto.MensajeChatDTO;
import com.pokeronline.chat.model.MensajeChat;
import com.pokeronline.chat.repository.MensajeChatRepository;
import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MensajeChatRepository mensajeChatRepository;
    private final UserRepository userRepository;

    public MensajeChatDTO guardarMensaje(Long remitenteId, Long mesaId, String contenido) {
        User remitente = userRepository.findById(remitenteId).orElseThrow();

        MensajeChat mensaje = MensajeChat.builder()
                .contenido(contenido)
                .remitente(remitente)
                .mesaId(mesaId)
                .timestamp(new Date())
                .build();

        mensajeChatRepository.save(mensaje);

        return MensajeChatDTO.builder()
                .mesaId(mesaId)
                .remitenteId(remitenteId)
                .remitenteUsername(remitente.getUsername())
                .contenido(contenido)
                .timestamp(mensaje.getTimestamp())
                .build();
    }

    public List<MensajeChatDTO> obtenerHistorialPorMesa(Long mesaId) {
        return mensajeChatRepository.findByMesaIdOrderByTimestampAsc(mesaId)
                .stream()
                .map(m -> MensajeChatDTO.builder()
                        .mesaId(m.getMesaId())
                        .remitenteId(m.getRemitente().getId())
                        .remitenteUsername(m.getRemitente().getUsername())
                        .contenido(m.getContenido())
                        .timestamp(m.getTimestamp())
                        .build())
                .toList();
    }
}