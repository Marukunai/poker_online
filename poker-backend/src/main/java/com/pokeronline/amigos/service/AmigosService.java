package com.pokeronline.amigos.service;

import com.pokeronline.amigos.dto.*;
import com.pokeronline.amigos.model.*;
import com.pokeronline.amigos.repository.AmistadRepository;
import com.pokeronline.amigos.repository.ConfiguracionPrivacidadRepository;
import com.pokeronline.amigos.repository.SolicitudAmistadRepository;
import com.pokeronline.exception.ResourceNotFoundException;
import com.pokeronline.exception.UnauthorizedException;
import com.pokeronline.model.User;
import com.pokeronline.notificacion.TipoNotificacion;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.websocket.WebSocketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de amigos (solicitudes, aceptar, eliminar, listar…)
 * NOTA: Requiere DTOs (AmigoDTO, AmistadDTO, SolicitudAmistadDTO) y repos listados.
 */
@Service
@RequiredArgsConstructor
public class AmigosService {

    private final AmistadRepository amistadRepository;
    private final SolicitudAmistadRepository solicitudRepository;
    private final UserRepository userRepository;
    private final ConfiguracionPrivacidadRepository configRepository;
    private final NotificacionService notificacionService; // si no lo tienes aún, puedes comentar estas líneas
    private final WebSocketService webSocketService;
    private final PresenciaService presenciaService;
    private final InvitacionService invitacionService;      // si aún no lo tienes, crea stub o comenta
    private final ChatPrivadoService chatPrivadoService;    // si aún no lo tienes, crea stub o comenta

    /**
     * Envía una solicitud de amistad.
     */
    public SolicitudAmistadDTO enviarSolicitud(Long remitenteId, Long destinatarioId, String mensaje) {
        // Validaciones
        validarNoEsElMismo(remitenteId, destinatarioId);
        validarNoSonAmigos(remitenteId, destinatarioId);
        validarNoExisteSolicitudPendiente(remitenteId, destinatarioId);
        validarLimiteDiario(remitenteId);
        validarPrivacidadDestinatario(remitenteId, destinatarioId);

        User remitente = userRepository.findById(remitenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario remitente no encontrado"));
        User destinatario = userRepository.findById(destinatarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario destinatario no encontrado"));

        // Crear solicitud
        SolicitudAmistad solicitud = SolicitudAmistad.builder()
                .remitente(remitente)
                .destinatario(destinatario)
                .mensaje(mensaje)
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaEnvio(LocalDateTime.now())
                .build();

        solicitud = solicitudRepository.save(solicitud);

        // Notificar al destinatario
        if (notificacionService != null) {
            notificacionService.enviarNotificacion(
                    destinatarioId,
                    TipoNotificacion.SOLICITUD_AMISTAD,
                    "Nueva solicitud de amistad",
                    remitente.getUsername() + " quiere ser tu amigo",
                    Map.of("solicitudId", solicitud.getId())
            );
        }

        // WebSocket
        webSocketService.enviarMensajeUsuario(
                destinatarioId,
                "/queue/amigos/solicitudes",
                SolicitudAmistadDTO.fromEntity(solicitud)
        );

        return SolicitudAmistadDTO.fromEntity(solicitud);
    }

    /**
     * Acepta una solicitud de amistad
     */
    @Transactional
    public AmistadDTO aceptarSolicitud(Long solicitudId, Long userId) {
        SolicitudAmistad solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        // Validar que el usuario es el destinatario
        if (!solicitud.getDestinatario().getId().equals(userId)) {
            throw new UnauthorizedException("No puedes aceptar esta solicitud");
        }

        // Validar que está pendiente
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalArgumentException("La solicitud ya fue respondida");
        }

        // Actualizar solicitud
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitudRepository.save(solicitud);

        // Crear amistad (la entidad auto-normaliza (usuario1_id < usuario2_id))
        Amistad amistad = Amistad.builder()
                .usuario1(solicitud.getRemitente())
                .usuario2(solicitud.getDestinatario())
                .fechaAmistad(LocalDateTime.now())
                .esFavorito1(false)
                .esFavorito2(false)
                .notificacionesActivas1(true)
                .notificacionesActivas2(true)
                .build();

        amistad = amistadRepository.save(amistad);

        // Notificar al remitente
        if (notificacionService != null) {
            notificacionService.enviarNotificacion(
                    solicitud.getRemitente().getId(),
                    TipoNotificacion.SOLICITUD_ACEPTADA,
                    "Solicitud aceptada",
                    solicitud.getDestinatario().getUsername() + " aceptó tu solicitud",
                    Map.of("userId", solicitud.getDestinatario().getId())
            );
        }

        // WebSocket a ambos
        webSocketService.enviarMensajeUsuario(
                solicitud.getRemitente().getId(),
                "/queue/amigos/solicitudes",
                Map.of(
                        "tipo", "SOLICITUD_ACEPTADA",
                        "userId", solicitud.getDestinatario().getId(),
                        "username", solicitud.getDestinatario().getUsername()
                )
        );

        return AmistadDTO.fromEntity(amistad, userId);
    }

    /**
     * Lista amigos con estado de presencia (filtrado/orden opcional).
     */
    public List<AmigoDTO> obtenerAmigos(Long userId, FiltroAmigos filtro, OrdenAmigos orden) {
        List<Amistad> amistades = amistadRepository.findByUsuario(userId);

        return amistades.stream()
                .map(amistad -> {
                    User amigo = amistad.getUsuario1().getId().equals(userId)
                            ? amistad.getUsuario2()
                            : amistad.getUsuario1();

                    // Obtener estado de presencia
                    EstadoPresencia estado = presenciaService.obtenerEstado(amigo.getId());

                    boolean esU1 = esUsuario1(amistad, userId);

                    return AmigoDTO.builder()
                            .userId(amigo.getId())
                            .username(amigo.getUsername())
                            .avatarUrl(amigo.getAvatarUrl())
                            .estado(estado.getEstado())
                            .detalleEstado(estado.getDetalleEstado())
                            .ultimaConexion(estado.getUltimaActividad())
                            .esFavorito(esU1 ? amistad.getEsFavorito1() : amistad.getEsFavorito2())
                            .alias(esU1 ? amistad.getAlias1() : amistad.getAlias2())
                            .puedeUnirse(Boolean.TRUE.equals(estado.getAceptaInvitaciones()) && estado.getMesaId() != null)
                            .mesaId(estado.getMesaId())
                            .torneoId(estado.getTorneoId())
                            .fichas((long) amigo.getFichas())
                            .build();
                })
                .filter(a -> aplicarFiltro(a, filtro))
                .sorted(obtenerComparador(orden))
                .collect(Collectors.toList());
    }

    /**
     * Elimina un amigo (opcionalmente borra el chat).
     */
    @Transactional
    public void eliminarAmigo(Long userId, Long amigoId, boolean eliminarHistorialChat) {
        Amistad amistad = amistadRepository.findByUsuarios(userId, amigoId)
                .orElseThrow(() -> new ResourceNotFoundException("Amistad no encontrada"));

        // Eliminar amistad
        amistadRepository.delete(amistad);

        // Cancelar invitaciones pendientes
        if (invitacionService != null) {
            invitacionService.cancelarInvitacionesEntre(userId, amigoId);
        }

        // Eliminar historial de chat si se solicita
        if (eliminarHistorialChat && chatPrivadoService != null) {
            chatPrivadoService.eliminarConversacion(userId, amigoId);
        }

        // Notificar al otro usuario
        if (notificacionService != null) {
            notificacionService.enviarNotificacion(
                    amigoId,
                    TipoNotificacion.AMIGO_ELIMINADO,
                    "Amistad eliminada",
                    "Ya no eres amigo de " + obtenerUsername(userId),
                    Map.of("userId", userId)
            );
        }

        // WebSocket
        webSocketService.enviarMensajeUsuario(
                amigoId,
                "/queue/amigos/estados",
                Map.of(
                        "tipo", "AMIGO_ELIMINADO",
                        "userId", userId
                )
        );
    }

    // ====== AUXILIARES ======

    private void validarNoEsElMismo(Long userId1, Long userId2) {
        if (Objects.equals(userId1, userId2)) {
            throw new IllegalArgumentException("No puedes enviarte solicitud a ti mismo");
        }
    }

    private void validarNoSonAmigos(Long userId1, Long userId2) {
        if (amistadRepository.existeAmistad(userId1, userId2)) {
            throw new IllegalArgumentException("Ya son amigos");
        }
    }

    private void validarNoExisteSolicitudPendiente(Long remitenteId, Long destinatarioId) {
        if (solicitudRepository.existePendiente(remitenteId, destinatarioId)) {
            throw new IllegalArgumentException("Ya existe una solicitud pendiente");
        }
    }

    private void validarLimiteDiario(Long userId) {
        long solicitudesHoy = solicitudRepository.contarSolicitudesHoy(userId);
        if (solicitudesHoy >= 20) {
            throw new IllegalArgumentException("Límite diario de solicitudes alcanzado");
        }
    }

    private void validarPrivacidadDestinatario(Long remitenteId, Long destinatarioId) {
        ConfiguracionPrivacidad config = configRepository.findByUserId(destinatarioId)
                .orElse(ConfiguracionPrivacidad.builder()
                        .quienPuedeEnviarSolicitudes(NivelPrivacidad.TODOS)
                        .build());

        switch (config.getQuienPuedeEnviarSolicitudes()) {
            case NADIE -> throw new IllegalArgumentException("Este usuario no acepta solicitudes");
            case AMIGOS -> {
                if (!amistadRepository.existeAmistad(remitenteId, destinatarioId)) {
                    throw new IllegalArgumentException("Solo acepta solicitudes de amigos");
                }
            }
            case AMIGOS_DE_AMIGOS -> {
                if (!amistadRepository.tienenAmigosEnComun(remitenteId, destinatarioId)) {
                    throw new IllegalArgumentException("Solo acepta solicitudes de amigos de amigos");
                }
            }
            case TODOS -> { /* permitir */ }
        }
    }

    private boolean esUsuario1(Amistad a, Long userId) {
        return a.getUsuario1().getId().equals(userId);
    }

    private boolean aplicarFiltro(AmigoDTO amigo, FiltroAmigos filtro) {
        if (filtro == null) return true;
        return switch (filtro) {
            case TODOS -> true;
            case FAVORITOS -> Boolean.TRUE.equals(amigo.getEsFavorito());
            case CONECTADOS -> {
                var e = amigo.getEstado();
                // Conectados = cualquiera que no sea OFFLINE/ASUENTE/INVISIBLE (ajústalo a tu gusto)
                yield e != EstadoConexion.OFFLINE
                        && e != EstadoConexion.AUSENTE
                        && e != EstadoConexion.INVISIBLE;
            }
            case EN_PARTIDA -> amigo.getEstado() == EstadoConexion.EN_PARTIDA;
            case EN_TORNEO -> amigo.getEstado() == EstadoConexion.EN_TORNEO;
        };
    }

    private Comparator<AmigoDTO> obtenerComparador(OrdenAmigos orden) {
        // Comparadores base por nombre (case-insensitive) con nulls al final
        Comparator<String> byNameAscRaw  = Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER);
        Comparator<String> byNameDescRaw = byNameAscRaw.reversed();

        Comparator<AmigoDTO> byNameAsc  = Comparator.comparing(AmigoDTO::getUsername, byNameAscRaw);
        Comparator<AmigoDTO> byNameDesc = Comparator.comparing(AmigoDTO::getUsername, byNameDescRaw);

        // Última conexión DESC (más reciente primero), nulls al final; desempate por nombre asc
        Comparator<AmigoDTO> byLastSeenDesc = Comparator
                .comparing(AmigoDTO::getUltimaConexion, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed()
                .thenComparing(AmigoDTO::getUsername, byNameAscRaw);

        // Favoritos primero (true > false), luego nombre asc
        Comparator<AmigoDTO> favoritesFirst = Comparator
                .comparing((AmigoDTO a) -> !Boolean.TRUE.equals(a.getEsFavorito())) // false (favorito) va primero
                .thenComparing(AmigoDTO::getUsername, byNameAscRaw);

        // Por estado con ranking custom + nombre asc
        Comparator<AmigoDTO> byEstado = Comparator
                .<AmigoDTO>comparingInt(a -> estadoRank(a.getEstado()))
                .thenComparing(AmigoDTO::getUsername, byNameAscRaw);

        if (orden == null) return byNameAsc;

        return switch (orden) {
            case POR_NOMBRE, NOMBRE_ASC -> byNameAsc;
            case NOMBRE_DESC -> byNameDesc;
            case ULTIMA_CONEXION_DESC -> byLastSeenDesc;
            case FAVORITOS_PRIMERO -> favoritesFirst;
            case POR_ESTADO -> byEstado;
        };
    }

    /**
     * Ranking de estado para ordenar por "disponibilidad".
     * Ajusta prioridades a tu gusto:
     * 0 EN_PARTIDA, 1 EN_TORNEO, 2 ONLINE, 3 NO_MOLESTAR, 4 AUSENTE, 5 INVISIBLE, 6 OFFLINE, 99 null
     */
    private int estadoRank(EstadoConexion e) {
        if (e == null) return 99;
        return switch (e) {
            case EN_PARTIDA     -> 0;
            case EN_TORNEO      -> 1;
            case ONLINE         -> 2;
            case NO_MOLESTAR    -> 3;
            case AUSENTE        -> 4;
            case INVISIBLE      -> 5;
            case OFFLINE        -> 6;
        };
    }

    private String obtenerUsername(Long userId) {
        return userRepository.findById(userId).map(User::getUsername).orElse("Usuario");
    }
}