package com.pokeronline.amigos.service;

import com.pokeronline.amigos.model.*;
import com.pokeronline.amigos.repository.AmistadRepository;
import com.pokeronline.amigos.repository.ConfiguracionPrivacidadRepository;
import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Estado de presencia (en memoria). Fácil de migrar a Redis si lo necesitas.
 */
@Service
@RequiredArgsConstructor
public class PresenciaService {

    private final Map<Long, EstadoPresencia> estadosActivos = new ConcurrentHashMap<>();

    private final WebSocketService webSocketService;
    private final AmistadRepository amistadRepository;
    private final ConfiguracionPrivacidadRepository configRepository;
    private final UserRepository userRepository;

    /** Actualiza el estado de un usuario */
    public void actualizarEstado(Long userId, EstadoConexion nuevoEstado, String detalle) {
        EstadoPresencia prev = estadosActivos.get(userId);

        EstadoPresencia actual = EstadoPresencia.builder()
                .userId(userId)
                .estado(nuevoEstado)
                .detalleEstado(detalle)
                .ultimaActividad(LocalDateTime.now())
                .aceptaInvitaciones(nuevoEstado != EstadoConexion.NO_MOLESTAR)
                .build();

        estadosActivos.put(userId, actual);

        if (prev == null || prev.getEstado() != nuevoEstado) {
            notificarCambioEstadoAAmigos(userId, actual);
        }
    }

    /** Heartbeat de actividad */
    public void actualizarActividad(Long userId) {
        EstadoPresencia estado = estadosActivos.get(userId);
        if (estado != null) {
            estado.setUltimaActividad(LocalDateTime.now());
            if (estado.getEstado() == EstadoConexion.AUSENTE) {
                actualizarEstado(userId, EstadoConexion.ONLINE, "En línea");
            }
        }
    }

    public void conectar(Long userId) {
        actualizarEstado(userId, EstadoConexion.ONLINE, "En línea");
        notificarConexionAAmigos(userId);
    }

    public void desconectar(Long userId) {
        actualizarEstado(userId, EstadoConexion.OFFLINE, "Desconectado");
        estadosActivos.remove(userId);
        notificarDesconexionAAmigos(userId);
    }

    public void entrarAPartida(Long userId, Long mesaId, String nombreMesa) {
        EstadoPresencia estado = estadosActivos.get(userId);
        if (estado != null) {
            estado.setEstado(EstadoConexion.EN_PARTIDA);
            estado.setDetalleEstado("Jugando en " + nombreMesa);
            estado.setMesaId(mesaId);
            notificarCambioEstadoAAmigos(userId, estado);
        }
    }

    public void salirDePartida(Long userId) {
        EstadoPresencia estado = estadosActivos.get(userId);
        if (estado != null) {
            estado.setEstado(EstadoConexion.ONLINE);
            estado.setDetalleEstado("En línea");
            estado.setMesaId(null);
            notificarCambioEstadoAAmigos(userId, estado);
        }
    }

    public EstadoPresencia obtenerEstado(Long userId) {
        return estadosActivos.getOrDefault(
                userId,
                EstadoPresencia.builder()
                        .userId(userId)
                        .estado(EstadoConexion.OFFLINE)
                        .detalleEstado("Desconectado")
                        .build()
        );
    }

    public boolean estaConectado(Long userId) {
        EstadoPresencia e = estadosActivos.get(userId);
        return e != null && e.getEstado() != EstadoConexion.OFFLINE;
    }

    public List<EstadoPresencia> obtenerEstadosAmigos(Long userId) {
        List<Long> amigosIds = amistadRepository.findAmigosIds(userId);
        return amigosIds.stream().map(this::obtenerEstado).collect(Collectors.toList());
    }

    private void notificarCambioEstadoAAmigos(Long userId, EstadoPresencia estado) {
        List<Long> amigosIds = amistadRepository.findAmigosIds(userId);
        for (Long amigoId : amigosIds) {
            if (debeNotificar(userId, amigoId)) {
                webSocketService.enviarMensajeUsuario(
                        amigoId,
                        "/queue/amigos/estados",
                        Map.of(
                                "tipo", "CAMBIO_ESTADO",
                                "userId", userId,
                                "estado", estado.getEstado(),
                                "detalleEstado", estado.getDetalleEstado()
                        )
                );
            }
        }
    }

    private void notificarConexionAAmigos(Long userId) {
        List<Long> amigosIds = amistadRepository.findAmigosIds(userId);
        String username = obtenerUsername(userId);

        for (Long amigoId : amigosIds) {
            if (debeNotificarConexion(userId, amigoId)) {
                webSocketService.enviarMensajeUsuario(
                        amigoId,
                        "/queue/amigos/estados",
                        Map.of(
                                "tipo", "AMIGO_CONECTADO",
                                "userId", userId,
                                "username", username
                        )
                );
            }
        }
    }

    private void notificarDesconexionAAmigos(Long userId) {
        List<Long> amigosIds = amistadRepository.findAmigosIds(userId);
        String username = obtenerUsername(userId);

        for (Long amigoId : amigosIds) {
            if (debeNotificar(userId, amigoId)) {
                webSocketService.enviarMensajeUsuario(
                        amigoId,
                        "/queue/amigos/estados",
                        Map.of(
                                "tipo", "AMIGO_DESCONECTADO",
                                "userId", userId,
                                "username", username
                        )
                );
            }
        }
    }

    private boolean debeNotificar(Long userId, Long amigoId) {
        // Chequea flags por amistad (notificaciones activas)
        return amistadRepository.findByUsuarios(userId, amigoId)
                .map(a -> a.getUsuario1().getId().equals(amigoId)
                        ? a.getNotificacionesActivas1()
                        : a.getNotificacionesActivas2())
                .orElse(false);
    }

    private boolean debeNotificarConexion(Long userId, Long amigoId) {
        // Respeta configuración de privacidad del usuario
        return configRepository.findByUserId(userId)
                .map(ConfiguracionPrivacidad::getNotificarConexion)
                .orElse(true) && debeNotificar(userId, amigoId);
    }

    private String obtenerUsername(Long userId) {
        return userRepository.findById(userId).map(User::getUsername).orElse("Usuario");
    }

    /** Marca AUSENTE si lleva >10 min inactivo estando ONLINE */
    @Scheduled(fixedRate = 300_000) // 5 minutos
    public void detectarUsuariosInactivos() {
        LocalDateTime umbral = LocalDateTime.now().minusMinutes(10);
        estadosActivos.entrySet().stream()
                .filter(e -> e.getValue().getEstado() == EstadoConexion.ONLINE
                        && e.getValue().getUltimaActividad() != null
                        && e.getValue().getUltimaActividad().isBefore(umbral))
                .forEach(e -> actualizarEstado(e.getKey(), EstadoConexion.AUSENTE, "Ausente"));
    }
}