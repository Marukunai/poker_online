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

import java.net.http.WebSocket;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Set<String> PALABRAS_PROHIBIDAS = Set.of(
            // Español
            "idiota", "imbecil", "tonto", "estupido", "capullo", "inutil", "gilipollas", "payaso", "anormal", "retrasado",
            "mierda", "joder", "puta", "puto", "coño", "zorra", "polla", "culiao", "cabron", "maricon", "maricón",
            "nazi", "negro de mierda", "sidoso", "mongolo", "retardado", "moromierda", "cancerigeno", "malparido",
            "p1nche", "m1erda", "pvt@", "hpt@", "c@bron", "g1l1p0llas",

            // Inglés
            "fuck", "bitch", "asshole", "dick", "faggot", "cunt", "shithead", "motherfucker", "bastard", "retard", "whore",
            "douchebag", "slut", "prick", "twat", "moron", "jerk", "nutjob", "pussy", "suck my", "kill yourself"
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

        // Lenguaje ofensivo
        if (contieneLenguajeOfensivo(contenido)) {
            moderacionService.registrarSancion(
                    remitenteId,
                    MotivoSancion.LENGUAJE_OBSCENO,
                    TipoSancion.ADVERTENCIA,
                    "Lenguaje inapropiado en el chat",
                    mesaId,
                    null
            );

            webSocketService.enviarMensajeJugador(
                    remitenteId,
                    "advertencia_chat",
                    Map.of(
                            "mensaje", "⚠️ Has recibido una advertencia por mal uso del chat."
                    )
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

    private boolean contieneLenguajeOfensivo(String contenido) {
        String texto = contenido.toLowerCase().replaceAll("[^a-zA-Z0-9áéíóúüñ]", "");

        return PALABRAS_PROHIBIDAS.stream()
                .anyMatch(palabra -> texto.contains(palabra.replaceAll("[^a-zA-Z0-9áéíóúüñ]", "")));
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