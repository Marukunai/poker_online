package com.pokeronline.chat.service;

import com.pokeronline.chat.dto.MensajeChatDTO;
import com.pokeronline.chat.model.MensajeChat;
import com.pokeronline.chat.repository.MensajeChatRepository;
import com.pokeronline.model.User;
import com.pokeronline.moderacion.model.MotivoSancion;
import com.pokeronline.moderacion.model.TipoSancion;
import com.pokeronline.moderacion.service.ModeracionService;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Set<String> PALABRAS_OBSCENAS = Set.of(
            "mierda", "joder", "puto", "coño", "zorra", "polla", "culiao", "nazi",
            "fuck", "asshole", "dick", "cunt", "shit", "verga", "pene", "cojones",
            "maldito", "cabrón", "puta madre", "chingar", "pendejo", "huevón",
            "damn", "bitch", "bastard", "motherfucker", "whore", "slut", "ass",
            "cock", "pussy", "tits", "bollocks", "wanker", "prick", "sod off"
    );

    private static final Set<String> PALABRAS_INSULTOS = Set.of(
            "idiota", "imbecil", "puta", "tonto", "bitch", "estupido", "capullo", "inutil", "gilipollas", "payaso", "anormal",
            "sidoso", "mongolo", "retardado", "malparido", "faggot", "retard", "bastard", "motherfucker", "whore", "slut",
            "cretino", "desgraciado", "cobarde", "cerdo", "gusano", "víbora", "hipócrita", "mentiroso", "traidor", "lerdo",
            "bobazo", "cabeza hueca", "engendro", "patán", "necio", "majadero",
            "moron", "dumbass", "jerk", "loser", "idiot", "cretin", "asshat",
            "scum", "pig", "worm", "snake", "hypocrite", "liar", "traitor", "dimwit",
            "nincompoop", "blockhead", "buffoon", "ignorant", "charlatan", "weasel"
    );

    private final WebSocketService webSocketService;
    private final ModeracionService moderacionService;
    private final MensajeChatRepository mensajeChatRepository;
    private final UserRepository userRepository;

    private static final int LIMITE_MENSAJES = 5;
    private static final int TIEMPO_VENTANA_MS = 10 * 1000; // 10 segundos

    public MensajeChatDTO guardarMensaje(Long remitenteId, Long mesaId, String contenido) {
        User remitente = userRepository.findById(remitenteId).orElseThrow();

        if (remitente.isChatBloqueado()) {
            throw new RuntimeException("No puedes enviar mensajes. Estás bloqueado del chat.");
        }

        // Lenguaje ofensivo o insultos
        MotivoSancion motivo = detectarMotivoLenguaje(contenido);
        if (motivo != null) {
            moderacionService.registrarSancion(
                    remitenteId,
                    motivo,
                    TipoSancion.ADVERTENCIA,
                    motivo == MotivoSancion.LENGUAJE_OBSCENO ?
                            "Lenguaje obsceno en el chat" :
                            "Insultos o amenazas en el chat",
                    mesaId,
                    null
            );

            webSocketService.enviarMensajeJugador(
                    remitenteId,
                    "advertencia_chat",
                    Map.of("mensaje", motivo == MotivoSancion.LENGUAJE_OBSCENO ?
                            "⚠️ Has recibido una advertencia por lenguaje obsceno." :
                            "⚠️ Has recibido una advertencia por insultos o amenazas.")
            );

            moderacionService.evaluarProhibicionChat(remitenteId, mesaId);
            throw new RuntimeException("Tu mensaje contiene lenguaje inapropiado.");
        }

        // Spam
        if (estaSpameando(remitente, mesaId)) {
            moderacionService.registrarSancion(
                    remitenteId,
                    MotivoSancion.ABUSO_DEL_CHAT,
                    TipoSancion.ADVERTENCIA,
                    "Has enviado demasiados mensajes en poco tiempo",
                    mesaId,
                    null
            );
            throw new RuntimeException("Estás escribiendo demasiado rápido. Relájate un poco.");
        }

        // Guardar mensaje
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

    private MotivoSancion detectarMotivoLenguaje(String contenido) {
        String texto = contenido.toLowerCase().replaceAll("[^a-zA-Z0-9áéíóúüñ]", "");

        if (PALABRAS_INSULTOS.stream().anyMatch(texto::contains)) {
            return MotivoSancion.INSULTOS_O_AMENAZAS;
        }

        if (PALABRAS_OBSCENAS.stream().anyMatch(texto::contains)) {
            return MotivoSancion.LENGUAJE_OBSCENO;
        }

        return null;
    }

    private boolean estaSpameando(User remitente, Long mesaId) {
        Date ahora = new Date();
        Date haceUnosSegundos = new Date(ahora.getTime() - TIEMPO_VENTANA_MS);

        long mensajesRecientes = mensajeChatRepository
                .findByMesaIdAndRemitente_IdAndTimestampAfterOrderByTimestampDesc(mesaId, remitente.getId(), haceUnosSegundos)
                .size();

        return mensajesRecientes >= LIMITE_MENSAJES;
    }
}