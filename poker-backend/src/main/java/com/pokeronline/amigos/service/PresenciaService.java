package com.pokeronline.amigos.service;

import com.pokeronline.amigos.model.*;
import com.pokeronline.amigos.repository.AmistadRepository;
import com.pokeronline.amigos.repository.ConfiguracionPrivacidadRepository;
import com.pokeronline.amigos.repository.EstadoPresenciaRepository;
import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /* ===================== ENTRADAS PÚBLICAS (todas con TX) ===================== */

    /** Wrapper simple para (estado, detalle). */
    @Transactional
    public void setEstado(Long userId, EstadoConexion estado, String detalle) {
        actualizarEstado(userId, estado, detalle);
    }

    /** Wrapper avanzado para setear mesa/torneo/aceptaInvitaciones además del estado. */
    @Transactional
    public void setEstadoAvanzado(Long userId,
                                  EstadoConexion estado,
                                  String detalle,
                                  Long mesaId,
                                  Long torneoId,
                                  Boolean aceptaInvitaciones) {
        LocalDateTime ahora = LocalDateTime.now();

        EstadoPresencia previo = estadosActivos.get(userId);
        EstadoPresencia cache = (previo != null ? previo : new EstadoPresencia());
        cache.setEstado(estado);
        cache.setDetalleEstado(detalle);
        cache.setMesaId(mesaId);
        cache.setTorneoId(torneoId);
        if (aceptaInvitaciones != null) {
            cache.setAceptaInvitaciones(aceptaInvitaciones);
        } else if (previo == null) {
            // default razonable cuando no existe cache aún
            cache.setAceptaInvitaciones(estado != EstadoConexion.NO_MOLESTAR);
        }
        cache.setUltimaActividad(ahora);
        estadosActivos.put(userId, cache);

        int updated = estadoRepo.updateSnapshot(
                userId,
                cache.getEstado(),
                cache.getDetalleEstado(),
                cache.getMesaId(),
                cache.getTorneoId(),
                cache.getAceptaInvitaciones(),
                cache.getUltimaActividad()
        );

        if (updated == 0) {
            User refUser = userRepository.getReferenceById(userId);
            EstadoPresencia entidad = new EstadoPresencia();
            entidad.setUser(refUser); // @MapsId: PK = user.id
            entidad.setEstado(cache.getEstado());
            entidad.setDetalleEstado(cache.getDetalleEstado());
            entidad.setMesaId(cache.getMesaId());
            entidad.setTorneoId(cache.getTorneoId());
            entidad.setAceptaInvitaciones(cache.getAceptaInvitaciones());
            entidad.setUltimaActividad(cache.getUltimaActividad());
            estadoRepo.save(entidad);
        }

        if (previo == null || previo.getEstado() != estado) {
            notificarCambioEstadoAAmigos(userId, cache);
        }
    }

    @Transactional
    public void conectar(Long userId) {
        actualizarEstado(userId, EstadoConexion.ONLINE, "En línea");
        notificarConexionAAmigos(userId);
    }

    @Transactional
    public void desconectar(Long userId) {
        actualizarEstado(userId, EstadoConexion.OFFLINE, "Desconectado");
        estadosActivos.remove(userId);
        notificarDesconexionAAmigos(userId);
    }

    /** Heartbeat: toca últimaActividad; si estaba AUSENTE, vuelve a ONLINE. */
    @Transactional
    public void actualizarActividad(Long userId) {
        LocalDateTime ahora = LocalDateTime.now();
        EstadoPresencia cache = estadosActivos.get(userId);
        if (cache != null) cache.setUltimaActividad(ahora);

        estadoRepo.touch(userId, ahora);

        if (cache != null && cache.getEstado() == EstadoConexion.AUSENTE) {
            actualizarEstado(userId, EstadoConexion.ONLINE, "En línea");
        }
    }

    @Transactional
    public void entrarAPartida(Long userId, Long mesaId, String nombreMesa) {
        EstadoPresencia cache = estadosActivos.computeIfAbsent(userId, k -> {
            EstadoPresencia ep = new EstadoPresencia();
            ep.setEstado(EstadoConexion.ONLINE);
            ep.setAceptaInvitaciones(true);
            return ep;
        });

        cache.setEstado(EstadoConexion.EN_PARTIDA);
        cache.setDetalleEstado("Jugando en " + nombreMesa);
        cache.setMesaId(mesaId);
        cache.setUltimaActividad(LocalDateTime.now());

        int updated = estadoRepo.updateSnapshot(
                userId,
                cache.getEstado(),
                cache.getDetalleEstado(),
                cache.getMesaId(),
                cache.getTorneoId(),
                cache.getAceptaInvitaciones(),
                cache.getUltimaActividad()
        );
        if (updated == 0) {
            User refUser = userRepository.getReferenceById(userId);
            EstadoPresencia entidad = new EstadoPresencia();
            entidad.setUser(refUser); // @MapsId: PK = user.id
            entidad.setEstado(cache.getEstado());
            entidad.setDetalleEstado(cache.getDetalleEstado());
            entidad.setMesaId(cache.getMesaId());
            entidad.setTorneoId(cache.getTorneoId());
            entidad.setAceptaInvitaciones(cache.getAceptaInvitaciones());
            entidad.setUltimaActividad(cache.getUltimaActividad());
            estadoRepo.save(entidad);
        }

        notificarCambioEstadoAAmigos(userId, cache);
    }

    @Transactional
    public void salirDePartida(Long userId) {
        EstadoPresencia cache = estadosActivos.computeIfAbsent(userId, k -> {
            EstadoPresencia ep = new EstadoPresencia();
            ep.setEstado(EstadoConexion.ONLINE);
            ep.setAceptaInvitaciones(true);
            return ep;
        });

        cache.setEstado(EstadoConexion.ONLINE);
        cache.setDetalleEstado("En línea");
        cache.setMesaId(null);
        cache.setUltimaActividad(LocalDateTime.now());

        int updated = estadoRepo.updateSnapshot(
                userId,
                cache.getEstado(),
                cache.getDetalleEstado(),
                null,
                cache.getTorneoId(),
                cache.getAceptaInvitaciones(),
                cache.getUltimaActividad()
        );
        if (updated == 0) {
            User refUser = userRepository.getReferenceById(userId);
            EstadoPresencia entidad = new EstadoPresencia();
            entidad.setUser(refUser);
            entidad.setEstado(cache.getEstado());
            entidad.setDetalleEstado(cache.getDetalleEstado());
            entidad.setMesaId(null);
            entidad.setTorneoId(cache.getTorneoId());
            entidad.setAceptaInvitaciones(cache.getAceptaInvitaciones());
            entidad.setUltimaActividad(cache.getUltimaActividad());
            estadoRepo.save(entidad);
        }

        notificarCambioEstadoAAmigos(userId, cache);
    }

    /* ============================ LECTURAS (sin TX) ============================ */

    /** Lee cache; si no está, intenta cargar de BD y cachear. */
    public EstadoPresencia obtenerEstado(Long userId) {
        EstadoPresencia enMem = estadosActivos.get(userId);
        if (enMem != null) return enMem;

        return estadoRepo.findById(userId).map(db -> {
            estadosActivos.put(userId, db);
            return db;
        }).orElseGet(() -> {
            EstadoPresencia ep = new EstadoPresencia();
            ep.setEstado(EstadoConexion.OFFLINE);
            ep.setDetalleEstado("Desconectado");
            ep.setAceptaInvitaciones(true);
            return ep;
        });
    }

    public boolean estaConectado(Long userId) {
        return obtenerEstado(userId).getEstado() != EstadoConexion.OFFLINE;
    }

    public List<EstadoPresencia> obtenerEstadosAmigos(Long userId) {
        List<Long> amigosIds = amistadRepository.findAmigosIds(userId);
        return amigosIds.stream().map(this::obtenerEstado).collect(Collectors.toList());
    }

    /* ============================= SCHEDULED (TX) ============================= */

    /** Marca AUSENTE si lleva >10 min inactivo estando ONLINE (y persiste). */
    @Transactional
    @Scheduled(fixedRate = 600_000) // cada 10 min
    public void detectarUsuariosInactivos() {
        LocalDateTime umbral = LocalDateTime.now().minusMinutes(10);
        estadosActivos.entrySet().stream()
                .filter(e -> e.getValue().getEstado() == EstadoConexion.ONLINE
                        && e.getValue().getUltimaActividad() != null
                        && e.getValue().getUltimaActividad().isBefore(umbral))
                .forEach(e -> actualizarEstado(e.getKey(), EstadoConexion.AUSENTE, "Ausente"));
    }

    /* ============================ Privados (sin TX) =========================== */

    /** ÚNICO sitio que escribe cache + BD. Siempre se llama desde un Transactional. */
    private void actualizarEstado(Long userId, EstadoConexion nuevoEstado, String detalle) {
        LocalDateTime ahora = LocalDateTime.now();

        // 1) Cache (objeto NO gestionado por JPA)
        EstadoPresencia previo = estadosActivos.get(userId);
        EstadoPresencia cache = (previo != null ? previo : new EstadoPresencia());
        cache.setEstado(nuevoEstado);
        cache.setDetalleEstado(detalle);
        cache.setMesaId(previo != null ? previo.getMesaId() : null);
        cache.setTorneoId(previo != null ? previo.getTorneoId() : null);
        cache.setAceptaInvitaciones(nuevoEstado != EstadoConexion.NO_MOLESTAR);
        cache.setUltimaActividad(ahora);
        estadosActivos.put(userId, cache);

        // 2) BD (UPDATE; si no existe -> INSERT usando @MapsId)
        int updated = estadoRepo.updateSnapshot(
                userId, nuevoEstado, detalle, cache.getMesaId(), cache.getTorneoId(),
                cache.getAceptaInvitaciones(), ahora
        );
        if (updated == 0) {
            User refUser = userRepository.getReferenceById(userId);
            EstadoPresencia entidad = new EstadoPresencia();
            entidad.setUser(refUser); // clave con @MapsId
            entidad.setEstado(nuevoEstado);
            entidad.setDetalleEstado(detalle);
            entidad.setMesaId(cache.getMesaId());
            entidad.setTorneoId(cache.getTorneoId());
            entidad.setAceptaInvitaciones(cache.getAceptaInvitaciones());
            entidad.setUltimaActividad(ahora);
            estadoRepo.save(entidad);
        }

        // 3) Notificación si cambia el estado
        if (previo == null || previo.getEstado() != nuevoEstado) {
            notificarCambioEstadoAAmigos(userId, cache);
        }
    }

    /* ============================== Notificaciones ============================ */

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
        return amistadRepository.findByUsuarios(userId, amigoId)
                .map(a -> a.getUsuario1().getId().equals(amigoId)
                        ? a.getNotificacionesActivas1()
                        : a.getNotificacionesActivas2())
                .orElse(false);
    }

    private boolean debeNotificarConexion(Long userId, Long amigoId) {
        return configRepository.findByUserId(userId)
                .map(ConfiguracionPrivacidad::getNotificarConexion)
                .orElse(true) && debeNotificar(userId, amigoId);
    }

    private String obtenerUsername(Long userId) {
        return userRepository.findById(userId).map(User::getUsername).orElse("Usuario");
    }
}