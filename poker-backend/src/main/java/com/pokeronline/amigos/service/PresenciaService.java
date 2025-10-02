package com.pokeronline.amigos.service;

import com.pokeronline.amigos.model.*;
import com.pokeronline.amigos.repository.AmistadRepository;
import com.pokeronline.amigos.repository.ConfiguracionPrivacidadRepository;
import com.pokeronline.amigos.repository.EstadoPresenciaRepository;
import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.websocket.WebSocketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresenciaService {

    private final Map<Long, EstadoPresencia> estadosActivos = new ConcurrentHashMap<>();

    private final WebSocketService webSocketService;
    private final AmistadRepository amistadRepository;
    private final ConfiguracionPrivacidadRepository configRepository;
    private final UserRepository userRepository;
    private final EstadoPresenciaRepository estadoRepo;

    /** Actualiza y PERSISTE el estado de un usuario */
    @Transactional
    public void actualizarEstado(Long userId, EstadoConexion nuevoEstado, String detalle) {
        LocalDateTime ahora = LocalDateTime.now();

        // 1) Cache
        EstadoPresencia previo = estadosActivos.get(userId);
        EstadoPresencia actual = (previo != null ? previo : new EstadoPresencia());
        actual.setUserId(userId);
        actual.setEstado(nuevoEstado);
        actual.setDetalleEstado(detalle);
        actual.setMesaId(previo != null ? previo.getMesaId() : null);
        actual.setTorneoId(previo != null ? previo.getTorneoId() : null);
        actual.setAceptaInvitaciones(nuevoEstado != EstadoConexion.NO_MOLESTAR);
        actual.setUltimaActividad(ahora);
        estadosActivos.put(userId, actual);

        // 2) BD (update → insert si no existe)
        int updated = estadoRepo.updateSnapshot(
                userId, nuevoEstado, detalle, actual.getMesaId(), actual.getTorneoId(),
                actual.getAceptaInvitaciones(), ahora
        );
        if (updated == 0) {
            User refUser = userRepository.getReferenceById(userId);
            EstadoPresencia entidad = EstadoPresencia.builder()
                    .userId(userId)
                    .user(refUser) // requerido por @MapsId
                    .estado(nuevoEstado)
                    .detalleEstado(detalle)
                    .mesaId(actual.getMesaId())
                    .torneoId(actual.getTorneoId())
                    .aceptaInvitaciones(actual.getAceptaInvitaciones())
                    .ultimaActividad(ahora)
                    .build();
            estadoRepo.save(entidad);
        }

        // 3) Notificación si cambió el estado
        if (previo == null || previo.getEstado() != nuevoEstado) {
            notificarCambioEstadoAAmigos(userId, actual);
        }
    }

    /** Heartbeat: solo toca últimaActividad; si estaba AUSENTE, vuelve a ONLINE. */
    @Transactional
    public void actualizarActividad(Long userId) {
        LocalDateTime ahora = LocalDateTime.now();
        EstadoPresencia estado = estadosActivos.get(userId);
        if (estado != null) {
            estado.setUltimaActividad(ahora);
        }
        estadoRepo.touch(userId, ahora);
        if (estado != null && estado.getEstado() == EstadoConexion.AUSENTE) {
            actualizarEstado(userId, EstadoConexion.ONLINE, "En línea");
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

    @Transactional
    public void entrarAPartida(Long userId, Long mesaId, String nombreMesa) {
        EstadoPresencia estado = estadosActivos.computeIfAbsent(
                userId, k -> EstadoPresencia.builder().userId(userId).estado(EstadoConexion.ONLINE).build()
        );
        estado.setEstado(EstadoConexion.EN_PARTIDA);
        estado.setDetalleEstado("Jugando en " + nombreMesa);
        estado.setMesaId(mesaId);
        estado.setUltimaActividad(LocalDateTime.now());

        int updated = estadoRepo.updateSnapshot(
                userId, estado.getEstado(), estado.getDetalleEstado(),
                estado.getMesaId(), estado.getTorneoId(),
                estado.getAceptaInvitaciones(), estado.getUltimaActividad()
        );
        if (updated == 0) {
            User refUser = userRepository.getReferenceById(userId);
            estado.setUser(refUser);
            estadoRepo.save(estado);
        }

        notificarCambioEstadoAAmigos(userId, estado);
    }

    @Transactional
    public void salirDePartida(Long userId) {
        EstadoPresencia estado = estadosActivos.computeIfAbsent(
                userId, k -> EstadoPresencia.builder().userId(userId).estado(EstadoConexion.ONLINE).build()
        );
        estado.setEstado(EstadoConexion.ONLINE);
        estado.setDetalleEstado("En línea");
        estado.setMesaId(null);
        estado.setUltimaActividad(LocalDateTime.now());

        int updated = estadoRepo.updateSnapshot(
                userId, estado.getEstado(), estado.getDetalleEstado(),
                null, estado.getTorneoId(),
                estado.getAceptaInvitaciones(), estado.getUltimaActividad()
        );
        if (updated == 0) {
            User refUser = userRepository.getReferenceById(userId);
            estado.setUser(refUser);
            estadoRepo.save(estado);
        }

        notificarCambioEstadoAAmigos(userId, estado);
    }

    /** Lee cache; si no está, intenta cargar de BD y cachear. */
    public EstadoPresencia obtenerEstado(Long userId) {
        EstadoPresencia enMem = estadosActivos.get(userId);
        if (enMem != null) return enMem;

        return estadoRepo.findById(userId).map(db -> {
            estadosActivos.put(userId, db);
            return db;
        }).orElseGet(() -> EstadoPresencia.builder()
                .userId(userId)
                .estado(EstadoConexion.OFFLINE)
                .detalleEstado("Desconectado")
                .build());
    }

    public boolean estaConectado(Long userId) {
        EstadoPresencia e = obtenerEstado(userId);
        return e.getEstado() != EstadoConexion.OFFLINE;
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

    /** Marca AUSENTE si lleva >5 min inactivo estando ONLINE (y persiste). */
    @Scheduled(fixedRate = 300_000) // 5 min
    public void detectarUsuariosInactivos() {
        LocalDateTime umbral = LocalDateTime.now().minusMinutes(10);
        estadosActivos.entrySet().stream()
                .filter(e -> e.getValue().getEstado() == EstadoConexion.ONLINE
                        && e.getValue().getUltimaActividad() != null
                        && e.getValue().getUltimaActividad().isBefore(umbral))
                .forEach(e -> actualizarEstado(e.getKey(), EstadoConexion.AUSENTE, "Ausente"));
    }
}