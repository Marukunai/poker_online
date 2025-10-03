package com.pokeronline;

import com.pokeronline.amigos.model.*;
import com.pokeronline.amigos.repository.*;
import com.pokeronline.model.*;
import com.pokeronline.repository.*;
import com.pokeronline.torneo.model.*;
import com.pokeronline.torneo.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataLoader {

    private final UserRepository userRepository;
    private final MesaRepository mesaRepository;
    private final UserMesaRepository userMesaRepository;
    private final TorneoRepository torneoRepository;
    private final ParticipanteTorneoRepository participanteTorneoRepository;
    private final BlindLevelRepository blindLevelRepository;
    private final TorneoMesaRepository torneoMesaRepository;
    private final HistorialManoRepository historialManoRepository;
    private final TurnoRepository turnoRepository;
    private final EspectadorMesaRepository espectadorMesaRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // === Repos Amigos ===
    private final AmistadRepository amistadRepository;
    private final SolicitudAmistadRepository solicitudAmistadRepository;
    private final MensajePrivadoRepository mensajePrivadoRepository;
    private final TransferenciaFichasRepository transferenciaFichasRepository;
    private final InvitacionPartidaRepository invitacionPartidaRepository;
    private final ConfiguracionPrivacidadRepository configuracionPrivacidadRepository;
    private final EstadoPresenciaRepository estadoPresenciaRepository;

    @PostConstruct
    @Transactional
    public void init() {
        System.out.println("DataLoader DEV: iniciando…");

        if (userRepository.count() > 0) {
            System.out.println("Datos ya cargados. Ignorando DataLoader.");
            return;
        }

        // ===== 1) Usuarios (crear si no existen) =====
        User adminOwner = getOrCreateAdmin("maruku",  "maruku@gmail.com");
        User admin1     = getOrCreateAdmin("admin1",  "admin1@gmail.com");
        User admin2     = getOrCreateAdmin("admin2",  "admin2@gmail.com");

        User alice   = getOrCreateUser("alice",   "alice@email.com", 5_000);
        User bob     = getOrCreateUser("bob",     "bob@email.com", 8_000);
        User charlie = getOrCreateUser("charlie", "charlie@email.com", 15_000);
        User dan     = getOrCreateUser("dan",     "dan@email.com", 3_000);
        User eve     = getOrCreateUser("eve",     "eve@email.com", 10_000);
        User frank   = getOrCreateUser("frank",   "frank@email.com", 20_000);

        // ===== 2) Mesas (crear si no existen) =====
        Mesa mesa1 = getOrCreateMesa("Mesa Básica",       5, 10,  6, adminOwner);
        Mesa mesa2 = getOrCreateMesa("Mesa Intermedia",  25, 50,  6, adminOwner);
        Mesa mesa3 = getOrCreateMesa("Mesa High Rollers",100,200,  8, adminOwner);

        // Vinculaciones iniciales de ejemplo (si no existen)
        ensureVinculo(alice, mesa1, 1000);
        ensureVinculo(bob,   mesa1, 1000);
        ensureVinculo(charlie, mesa1, 1000);
        ensureVinculo(dan,   mesa2, 3000);
        ensureVinculo(eve,   mesa2, 3000);
        ensureVinculo(frank, mesa3, 5000);

        // ===== 3) Torneo básico (si no existe) =====
        Torneo torneo = torneoRepository.findByNombre("Torneo Inicial").orElseGet(() -> {
            Torneo t = Torneo.builder()
                    .nombre("Torneo Inicial")
                    .estado(TorneoEstado.PENDIENTE)
                    .fichasIniciales(1500)
                    .eliminacionDirecta(true)
                    .fechaInicio(new Date(System.currentTimeMillis() + 3600_000))
                    .nivelCiegasActual(0)
                    .build();
            return torneoRepository.save(t);
        });

        if (blindLevelRepository.findByTorneoOrderByNivelAsc(torneo).isEmpty()) {
            blindLevelRepository.saveAll(List.of(
                    BlindLevel.builder().torneo(torneo).smallBlind(10).bigBlind(20).duracionSegundos(300).build(),
                    BlindLevel.builder().torneo(torneo).smallBlind(25).bigBlind(50).duracionSegundos(300).build(),
                    BlindLevel.builder().torneo(torneo).smallBlind(50).bigBlind(100).duracionSegundos(300).build()
            ));
        }

        var mesasTorneo = torneoMesaRepository.findByTorneo(torneo); // devuelve List<TorneoMesa>
        if (mesasTorneo.isEmpty()) {
            torneoMesaRepository.save(
                    TorneoMesa.builder().torneo(torneo).mesa(mesa1).ronda(1).build()
            );
        }

        if (participanteTorneoRepository.findByTorneo(torneo).isEmpty()) {
            participanteTorneoRepository.saveAll(List.of(
                    ParticipanteTorneo.builder().torneo(torneo).user(alice).fichasActuales(1500).mesa(mesa1).build(),
                    ParticipanteTorneo.builder().torneo(torneo).user(bob).fichasActuales(1500).mesa(mesa1).build(),
                    ParticipanteTorneo.builder().torneo(torneo).user(charlie).fichasActuales(1500).mesa(mesa1).build()
            ));
        }

        if (historialManoRepository.count() == 0) {
            historialManoRepository.save(
                    HistorialMano.builder()
                            .jugador(alice)
                            .mesa(mesa1)
                            .fecha(new Date())
                            .fichasGanadas(500)
                            .tipoManoGanadora("Escalera")
                            .cartasJugador("K♠ Q♠")
                            .cartasComunitarias("J♠ 10♦ 9♠ 3♥ 2♣")
                            .cartasGanadoras("K♠ Q♠ J♠ 10♦ 9♠")
                            .contraJugadores("bob,charlie")
                            .faseFinal(Fase.RIVER)
                            .empate(false)
                            .build()
            );
        }

        if (turnoRepository.count() == 0) {
            turnoRepository.saveAll(List.of(
                    Turno.builder().mesa(mesa1).user(alice).accion(Accion.CALL).apuesta(50).ordenTurno(1).activo(false).eliminado(false).build(),
                    Turno.builder().mesa(mesa1).user(bob).accion(Accion.RAISE).apuesta(100).ordenTurno(2).activo(false).eliminado(false).build()
            ));
        }

        if (espectadorMesaRepository.count() == 0) {
            espectadorMesaRepository.save(
                    EspectadorMesa.builder().user(eve).mesa(mesa1).fechaEntrada(new Date()).build()
            );
        }

        // ===== 4) SEMILLA MÓDULO AMIGOS =====

        // 4.1 Configuración de privacidad (solo si no existe por usuario)
        ensurePrivacidad(alice,   NivelPrivacidad.AMIGOS, NivelPrivacidad.AMIGOS, NivelPrivacidad.AMIGOS, true,  true);
        ensurePrivacidad(bob,     NivelPrivacidad.TODOS,  NivelPrivacidad.AMIGOS, NivelPrivacidad.AMIGOS, true,  true);
        ensurePrivacidad(charlie, NivelPrivacidad.NADIE,  NivelPrivacidad.NADIE,  NivelPrivacidad.NADIE,  false, false);
        ensurePrivacidad(dan,     NivelPrivacidad.AMIGOS_DE_AMIGOS, NivelPrivacidad.AMIGOS, NivelPrivacidad.AMIGOS, true,  false);

        // 4.2 Amistades (solo si no existen)
        ensureAmistad(alice, bob,     true,  false, "Bobi",  null,   LocalDateTime.now().minusDays(15));
        ensureAmistad(alice, charlie, false, true,  null,    "Ali",  LocalDateTime.now().minusDays(8));
        ensureAmistad(bob,   dan,     false, false, null,    null,   LocalDateTime.now().minusDays(2)); // reciente

        // 4.3 Solicitudes de amistad de ejemplo (pendiente y rechazada)
        ensureSolicitud(dan,  alice,  "¡Hola, jugamos ayer!", EstadoSolicitud.PENDIENTE,  LocalDateTime.now().minusDays(1), null);
        ensureSolicitud(eve,  charlie,"Te agrego para torneos",EstadoSolicitud.RECHAZADA, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(9));

        // 4.4 Mensajes privados entre amigos
        ensureMensaje(alice, bob, TipoMensaje.TEXTO, "¡Buena partida ayer! 😉", null, false, LocalDateTime.now().minusHours(5));
        ensureMensaje(bob,   alice, TipoMensaje.AUDIO, "https://cdn.example.com/audios/bob/a1.webm", 12, true, LocalDateTime.now().minusHours(4));
        ensureMensaje(alice, charlie, TipoMensaje.GIF, "https://media.giphy.com/media/xyz/giphy.gif", null, false, LocalDateTime.now().minusHours(2));

        // 4.5 Transferencias de fichas
        ensureTransferencia(alice, bob,  500L, true,  "Para tu próximo torneo!", LocalDateTime.now().minusDays(3));
        ensureTransferencia(bob,   dan,  200L, false, "Prueba, devuélveme luego", LocalDateTime.now().minusDays(1));

        // 4.6 Invitaciones a partidas
        ensureInvitacion(alice, bob, mesa1, TipoInvitacion.JUGADOR,    EstadoInvitacion.PENDIENTE, "¡Ven a jugar!", LocalDateTime.now().minusMinutes(2), LocalDateTime.now().plusMinutes(3));
        ensureInvitacion(bob,   eve, mesa2, TipoInvitacion.ESPECTADOR, EstadoInvitacion.ACEPTADA,  "Mira esta mano", LocalDateTime.now().minusMinutes(20), LocalDateTime.now().minusMinutes(15));

        // 4.7 Estado de presencia (snapshots persistidos)
        ensureEstado(alice,   EstadoConexion.EN_PARTIDA, "Jugando en " + mesa1.getNombre(), mesa1.getId(), true,  LocalDateTime.now().minusMinutes(1));
        ensureEstado(bob,     EstadoConexion.ONLINE,     "En lobby",     null, true,  LocalDateTime.now().minusMinutes(3));
        ensureEstado(charlie, EstadoConexion.AUSENTE,    "Ausente",      null, true,  LocalDateTime.now().minusMinutes(30));
        ensureEstado(admin2,  EstadoConexion.NO_MOLESTAR,"No molestar",  null, false, LocalDateTime.now().minusMinutes(5));
        ensureEstado(admin1,  EstadoConexion.OFFLINE,"Desconectado",  null, false, LocalDateTime.now().minusMinutes(60));

        System.out.println("DataLoader DEV: OK (usuarios, mesas/torneo y amigos sembrados).");
    }

    // ===== Helpers de creación/aseguramiento =====

    private User getOrCreateUser(String username, String email, int fichas) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode("123"))
                    .avatarUrl(null)
                    .fichas(fichas)
                    .role(Role.USER)
                    .partidasGanadas(0)
                    .esIA(false)
                    .build();
            return userRepository.save(u);
        });
    }

    private User getOrCreateAdmin(String username, String email) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode("admin"))
                    .avatarUrl(null)
                    .fichas(999_999_999)
                    .role(Role.ADMIN)
                    .partidasGanadas(0)
                    .esIA(false)
                    .build();
            return userRepository.save(u);
        });
    }

    private Mesa getOrCreateMesa(String nombre, int sb, int bb, int max, User owner) {
        return mesaRepository.findByNombre(nombre).orElseGet(() -> {
            Mesa m = Mesa.builder()
                    .nombre(nombre)
                    .activa(true)
                    .smallBlind(sb)
                    .bigBlind(bb)
                    .fase(Fase.PRE_FLOP)
                    .pot(0)
                    .maxJugadores(max)
                    .creador(owner)
                    .build();
            return mesaRepository.save(m);
        });
    }

    private void ensureVinculo(User user, Mesa mesa, int fichas) {
        userMesaRepository.findByUserAndMesa(user, mesa).orElseGet(() -> {
            UserMesa um = UserMesa.builder()
                    .user(user)
                    .mesa(mesa)
                    .fichasEnMesa(fichas)
                    .fichasDisponibles(fichas)
                    .fichasIniciales(fichas)
                    .enJuego(true)
                    .conectado(true)
                    .lastSeen(new Date())
                    .build();
            return userMesaRepository.save(um);
        });
    }

    private void ensurePrivacidad(User user,
                                  NivelPrivacidad puedeSolicitudes,
                                  NivelPrivacidad puedeVerEstado,
                                  NivelPrivacidad puedeInvitar,
                                  boolean notificarConexion,
                                  boolean notificarInicioPartida) {
        configuracionPrivacidadRepository.findByUserId(user.getId()).orElseGet(() -> {
            ConfiguracionPrivacidad cfg = ConfiguracionPrivacidad.builder()
                    .usuario(user)
                    .quienPuedeEnviarSolicitudes(puedeSolicitudes)
                    .quienPuedeVerEstado(puedeVerEstado)
                    .quienPuedeInvitar(puedeInvitar)
                    .quienPuedeTransferirFichas(NivelPrivacidad.AMIGOS)
                    .mostrarEstadisticas(true)
                    .aceptarSolicitudesAutomaticamente(false)
                    .notificarConexion(notificarConexion)
                    .notificarInicioPartida(notificarInicioPartida)
                    .modoPerturbacion(false)
                    .build();
            return configuracionPrivacidadRepository.save(cfg);
        });
    }

    private void ensureAmistad(User u1, User u2,
                               boolean fav1, boolean fav2,
                               String alias1, String alias2,
                               LocalDateTime fecha) {
        if (!amistadRepository.existeAmistad(u1.getId(), u2.getId())) {
            Amistad a = Amistad.builder()
                    .usuario1(u1)
                    .usuario2(u2)
                    .fechaAmistad(fecha != null ? fecha : LocalDateTime.now())
                    .esFavorito1(fav1)
                    .esFavorito2(fav2)
                    .alias1(alias1)
                    .alias2(alias2)
                    .notificacionesActivas1(true)
                    .notificacionesActivas2(true)
                    .build();
            amistadRepository.save(a);
        }
    }

    private void ensureSolicitud(User remitente, User destinatario, String mensaje,
                                 EstadoSolicitud estado, LocalDateTime envio, LocalDateTime respuesta) {
        boolean exists = solicitudAmistadRepository
                .findByRemitenteIdAndDestinatarioId(remitente.getId(), destinatario.getId())
                .isPresent();
        if (!exists) {
            SolicitudAmistad s = SolicitudAmistad.builder()
                    .remitente(remitente)
                    .destinatario(destinatario)
                    .mensaje(mensaje)
                    .estado(estado != null ? estado : EstadoSolicitud.PENDIENTE)
                    .fechaEnvio(envio != null ? envio : LocalDateTime.now())
                    .fechaRespuesta(respuesta)
                    .build();
            solicitudAmistadRepository.save(s);
        }
    }

    private void ensureMensaje(User remitente, User destinatario, TipoMensaje tipo,
                               String contenido, Integer duracionAudio,
                               boolean leido, LocalDateTime fecha) {
        // Muy simple: crear uno si no hay ninguno entre esta pareja
        boolean exists = mensajePrivadoRepository.existsAnyBetween(remitente.getId(), destinatario.getId());
        if (!exists) {
            MensajePrivado m1 = MensajePrivado.builder()
                    .remitente(remitente)
                    .destinatario(destinatario)
                    .tipo(tipo)
                    .contenido(contenido)
                    .duracionAudio(duracionAudio)
                    .fechaEnvio(fecha != null ? fecha : LocalDateTime.now())
                    .leido(leido)
                    .eliminadoPorRemitente(false)
                    .eliminadoPorDestinatario(false)
                    .build();
            mensajePrivadoRepository.save(m1);
        }
    }

    private void ensureTransferencia(User remitente, User destinatario, Long cantidad,
                                     boolean esRegalo, String mensaje,
                                     LocalDateTime fecha) {
        boolean exists = transferenciaFichasRepository.existsAnyBetween(remitente.getId(), destinatario.getId());
        if (!exists) {
            TransferenciaFichas t = TransferenciaFichas.builder()
                    .remitente(remitente)
                    .destinatario(destinatario)
                    .cantidad(cantidad)
                    .mensaje(mensaje)
                    .esRegalo(esRegalo)
                    .estado(EstadoTransferencia.COMPLETADA)
                    .fecha(fecha != null ? fecha : LocalDateTime.now())
                    .build();
            transferenciaFichasRepository.save(t);
        }
    }

    private void ensureInvitacion(User remitente, User destinatario, Mesa mesa,
                                  TipoInvitacion tipo, EstadoInvitacion estado,
                                  String mensaje, LocalDateTime envio, LocalDateTime expira) {
        boolean exists = invitacionPartidaRepository.existsAnyBetween(remitente.getId(), destinatario.getId());
        if (!exists) {
            InvitacionPartida inv = InvitacionPartida.builder()
                    .remitente(remitente)
                    .destinatario(destinatario)
                    .mesa(mesa)
                    .tipo(tipo != null ? tipo : TipoInvitacion.JUGADOR)
                    .estado(estado != null ? estado : EstadoInvitacion.PENDIENTE)
                    .mensaje(mensaje)
                    .fechaEnvio(envio != null ? envio : LocalDateTime.now())
                    .fechaExpiracion(expira)
                    .build();
            invitacionPartidaRepository.save(inv);
        }
    }

    private void ensureEstado(User user,
                              EstadoConexion estado,
                              String detalle,
                              Long mesaId,
                              boolean aceptaInvitaciones,
                              LocalDateTime ultimaActividad) {

        estadoPresenciaRepository.findById(user.getId()).orElseGet(() -> {
            // Trae el User gestionado en *esta* sesión/tx
            User managed = userRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalStateException("User no existe: " + user.getId()));

            EstadoPresencia ep = new EstadoPresencia();
            ep.setUser(managed); // <-- usa el 'managed', no el parámetro original
            ep.setEstado(estado != null ? estado : EstadoConexion.OFFLINE);
            ep.setDetalleEstado(detalle);
            ep.setMesaId(mesaId);
            ep.setTorneoId(null);
            ep.setAceptaInvitaciones(aceptaInvitaciones);
            ep.setUltimaActividad(ultimaActividad != null ? ultimaActividad : LocalDateTime.now());

            return estadoPresenciaRepository.save(ep); // no setees ep.setUserId(...)
        });
    }
}