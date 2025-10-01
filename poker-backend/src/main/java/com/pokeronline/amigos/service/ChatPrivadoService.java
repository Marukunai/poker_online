package com.pokeronline.amigos.service;

import com.pokeronline.amigos.dto.CrearMensajeDTO;
import com.pokeronline.amigos.dto.MensajePrivadoDTO;
import com.pokeronline.amigos.model.MensajePrivado;
import com.pokeronline.amigos.model.TipoMensaje;
import com.pokeronline.amigos.repository.AmistadRepository;
import com.pokeronline.amigos.repository.MensajePrivadoRepository;
import com.pokeronline.exception.ResourceNotFoundException;
import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.util.FiltroPalabrasService;
import com.pokeronline.websocket.WebSocketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Chat privado entre amigos (con filtro de contenido y control de sanciones de chat).
 */
@Service
@RequiredArgsConstructor
public class ChatPrivadoService {

    private final MensajePrivadoRepository mensajeRepository;
    private final AmistadRepository amistadRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;
    private final NotificacionService notificacionService; // opcional
    private final PresenciaService presenciaService;
    private final FiltroPalabrasService filtroPalabrasService;

    /**
     * Envía un mensaje privado
     */
    @Transactional
    public MensajePrivadoDTO enviarMensaje(Long remitenteId, CrearMensajeDTO dto) {
        // Validar que son amigos
        if (!amistadRepository.existeAmistad(remitenteId, dto.getDestinatarioId())) {
            throw new IllegalArgumentException("Solo puedes enviar mensajes a tus amigos");
        }

        // Limitar velocidad (antispam)
        validarLimiteVelocidad(remitenteId);

        User remitente = userRepository.findById(remitenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario remitente no encontrado"));
        User destinatario = userRepository.findById(dto.getDestinatarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario destinatario no encontrado"));

        // Verificar prohibición de chat del remitente
        if (remitente.isChatBloqueado()) {
            throw new IllegalArgumentException("No puedes enviar mensajes: tienes el chat bloqueado por una sanción activa.");
        }

        // Filtrado de texto si corresponde
        String contenido = dto.getContenido();
        if (dto.getTipo() == TipoMensaje.TEXTO && contenido != null) {
            if (filtroPalabrasService.contienePalabraGrave(contenido)) {
                contenido = filtroPalabrasService.sanitizar(contenido); // o lanza excepción según tu criterio
            } else if (filtroPalabrasService.contienePalabraLeve(contenido)) {
                contenido = filtroPalabrasService.sanitizar(contenido);
            }
        }

        // Crear mensaje
        MensajePrivado mensaje = MensajePrivado.builder()
                .remitente(remitente)
                .destinatario(destinatario)
                .tipo(dto.getTipo())
                .contenido(contenido)
                .fechaEnvio(LocalDateTime.now())
                .leido(false)
                .duracionAudio(dto.getDuracionAudio())
                .build();

        // Si es respuesta a otro
        if (dto.getMensajeRespondidoId() != null) {
            MensajePrivado respondido = mensajeRepository.findById(dto.getMensajeRespondidoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mensaje respondido no encontrado"));
            mensaje.setMensajeRespondido(respondido);
        }

        mensaje = mensajeRepository.save(mensaje);

        // WebSocket en tiempo real
        MensajePrivadoDTO mensajeDTO = MensajePrivadoDTO.fromEntity(mensaje);
        webSocketService.enviarMensajeUsuario(
                dto.getDestinatarioId(),
                "/queue/chat/mensajes",
                mensajeDTO
        );

        // Notificación push si el destinatario no está conectado
        if (!presenciaService.estaConectado(dto.getDestinatarioId()) && notificacionService != null) {
            notificacionService.enviarPushNotification(
                    dto.getDestinatarioId(),
                    "Nuevo mensaje de " + remitente.getUsername(),
                    obtenerVistaPrevia(mensaje),
                    Map.of("tipo", "MENSAJE_CHAT", "remitenteId", remitenteId)
            );
        }

        return mensajeDTO;
    }

    /**
     * Conversación paginada
     */
    public Page<MensajePrivadoDTO> obtenerConversacion(Long userId, Long amigoId, Pageable pageable) {
        if (!amistadRepository.existeAmistad(userId, amigoId)) {
            throw new IllegalArgumentException("No tienes permiso para ver esta conversación");
        }
        Page<MensajePrivado> mensajes = mensajeRepository.findConversacion(userId, amigoId, pageable);
        return mensajes.map(MensajePrivadoDTO::fromEntity);
    }

    /**
     * Marca mensajes como leídos
     */
    @Transactional
    public void marcarComoLeidos(Long userId, Long remitenteId) {
        List<MensajePrivado> noLeidos = mensajeRepository.findNoLeidosDeRemitente(userId, remitenteId);
        if (!noLeidos.isEmpty()) {
            noLeidos.forEach(m -> {
                m.setLeido(true);
                m.setFechaLectura(LocalDateTime.now());
            });
            mensajeRepository.saveAll(noLeidos);

            // Notificar al remitente
            webSocketService.enviarMensajeUsuario(
                    remitenteId,
                    "/queue/chat/mensajes",
                    Map.of(
                            "tipo", "MENSAJES_LEIDOS",
                            "destinatarioId", userId,
                            "cantidad", noLeidos.size()
                    )
            );
        }
    }

    public int contarNoLeidos(Long userId) {
        return mensajeRepository.countNoLeidos(userId);
    }

    /**
     * Elimina un mensaje; si paraAmbos = true, solo el remitente puede hacerlo.
     */
    @Transactional
    public void eliminarMensaje(Long mensajeId, Long userId, boolean paraAmbos) {
        MensajePrivado mensaje = mensajeRepository.findById(mensajeId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

        boolean esRemitente = mensaje.getRemitente().getId().equals(userId);
        boolean esDestinatario = mensaje.getDestinatario().getId().equals(userId);

        if (!esRemitente && !esDestinatario) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este mensaje");
        }
        if (paraAmbos && !esRemitente) {
            throw new IllegalArgumentException("Solo el remitente puede eliminar para ambos");
        }

        if (paraAmbos) {
            mensaje.setEliminadoPorRemitente(true);
            mensaje.setEliminadoPorDestinatario(true);
        } else {
            if (esRemitente) mensaje.setEliminadoPorRemitente(true);
            else mensaje.setEliminadoPorDestinatario(true);
        }

        if (Boolean.TRUE.equals(mensaje.getEliminadoPorRemitente()) &&
                Boolean.TRUE.equals(mensaje.getEliminadoPorDestinatario())) {
            mensajeRepository.delete(mensaje);
        } else {
            mensajeRepository.save(mensaje);
        }
    }

    // === Auxiliares ===

    private void validarLimiteVelocidad(Long userId) {
        long enviadosEnMinuto = mensajeRepository.contarMensajesUltimoMinuto(userId);
        if (enviadosEnMinuto >= 20) {
            throw new IllegalArgumentException("Límite de mensajes por minuto alcanzado");
        }
    }

    private String obtenerVistaPrevia(MensajePrivado mensaje) {
        if (mensaje.getTipo() == TipoMensaje.TEXTO && mensaje.getContenido() != null) {
            return mensaje.getContenido().length() > 50
                    ? mensaje.getContenido().substring(0, 50) + "..."
                    : mensaje.getContenido();
        }
        return switch (mensaje.getTipo()) {
            case AUDIO -> "🎤 Mensaje de audio (" + mensaje.getDuracionAudio() + "s)";
            case GIF -> "🖼️ GIF";
            case STICKER -> "😀 Sticker";
            case IMAGEN -> "📷 Imagen";
            default -> "Nuevo mensaje";
        };
    }

    // Útil para AmigosService.eliminarAmigo
    @Transactional
    public void eliminarConversacion(Long userId, Long amigoId) {
        mensajeRepository.eliminarConversacion(userId, amigoId);
    }
}