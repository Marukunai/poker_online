package com.pokeronline;

import com.pokeronline.model.*;
import com.pokeronline.repository.*;
import com.pokeronline.torneo.model.*;
import com.pokeronline.torneo.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
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

    @PostConstruct
    public void init() {
        if (userRepository.findByEmail("maruku@email.com").isPresent()) {
            System.out.println("Datos ya cargados. Ignorando DataLoader.");
            return;
        }

        // Crear usuarios
        User maruku = crearUsuarioAdmin("maruku", "maruku@gmail.com");
        User admin1 = crearUsuarioAdmin("admin1", "admin1@gmail.com");
        User admin2 = crearUsuarioAdmin("admin2", "admin2@gmail.com");
        User alice = crearUsuario("alice", "alice@email.com", 5000);
        User bob = crearUsuario("bob", "bob@email.com", 8000);
        User charlie = crearUsuario("charlie", "charlie@email.com", 15000);
        User dan = crearUsuario("dan", "dan@email.com", 3000);
        User eve = crearUsuario("eve", "eve@email.com", 10000);
        User frank = crearUsuario("frank", "frank@email.com", 20000);

        userRepository.saveAll(List.of(maruku, admin1, admin2, alice, bob, charlie, dan, eve, frank));

        // Crear mesas
        Mesa mesa1 = crearMesa("Mesa Básica", 5, 10, 6);
        Mesa mesa2 = crearMesa("Mesa Intermedia", 25, 50, 6);
        Mesa mesa3 = crearMesa("Mesa High Rollers", 100, 200, 8);

        mesaRepository.saveAll(List.of(mesa1, mesa2, mesa3));

        // Vincular usuarios a mesas
        userMesaRepository.saveAll(List.of(
                vincularUsuarioMesa(alice, mesa1, 1000),
                vincularUsuarioMesa(bob, mesa1, 1000),
                vincularUsuarioMesa(charlie, mesa1, 1000),
                vincularUsuarioMesa(dan, mesa2, 3000),
                vincularUsuarioMesa(eve, mesa2, 3000),
                vincularUsuarioMesa(frank, mesa3, 5000)
        ));

        // Crear torneo
        Torneo torneo = Torneo.builder()
                .nombre("Torneo Inicial")
                .estado(TorneoEstado.PENDIENTE)
                .fichasIniciales(1500)
                .eliminacionDirecta(true)
                .fechaInicio(new Date(System.currentTimeMillis() + 3600 * 1000)) // empieza en 1h
                .nivelCiegasActual(0)
                .build();

        torneoRepository.save(torneo);

        // Niveles de ciegas
        BlindLevel nivel1 = BlindLevel.builder().torneo(torneo).smallBlind(10).bigBlind(20).duracionSegundos(300).build();
        BlindLevel nivel2 = BlindLevel.builder().torneo(torneo).smallBlind(25).bigBlind(50).duracionSegundos(300).build();
        BlindLevel nivel3 = BlindLevel.builder().torneo(torneo).smallBlind(50).bigBlind(100).duracionSegundos(300).build();
        blindLevelRepository.saveAll(List.of(nivel1, nivel2, nivel3));

        // Vincular mesa al torneo
        TorneoMesa torneoMesa = TorneoMesa.builder()
                .torneo(torneo)
                .mesa(mesa1)
                .ronda(1)
                .build();
        torneoMesaRepository.save(torneoMesa);

        // Participantes del torneo
        participanteTorneoRepository.saveAll(List.of(
                ParticipanteTorneo.builder().torneo(torneo).user(alice).fichasActuales(1500).mesa(mesa1).build(),
                ParticipanteTorneo.builder().torneo(torneo).user(bob).fichasActuales(1500).mesa(mesa1).build(),
                ParticipanteTorneo.builder().torneo(torneo).user(charlie).fichasActuales(1500).mesa(mesa1).build()
        ));

        // Historial de mano
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

        // Turnos de ejemplo
        turnoRepository.saveAll(List.of(
                Turno.builder().mesa(mesa1).user(alice).accion(Accion.CALL).apuesta(50).ordenTurno(1).activo(false).eliminado(false).build(),
                Turno.builder().mesa(mesa1).user(bob).accion(Accion.RAISE).apuesta(100).ordenTurno(2).activo(false).eliminado(false).build()
        ));

        // Espectadores
        espectadorMesaRepository.save(
                EspectadorMesa.builder()
                        .user(eve)
                        .mesa(mesa1)
                        .fechaEntrada(new Date())
                        .build()
        );

        System.out.println("DataLoader: usuarios, mesas, torneo y datos de ejemplo cargados correctamente.");
    }

    private User crearUsuario(String username, String email, int fichas) {
        return User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("123"))
                .avatarUrl(null)
                .fichas(fichas)
                .role(Role.USER)
                .partidasGanadas(0)
                .esIA(false)
                .build();
    }

    private User crearUsuarioAdmin(String username, String email) {
        return User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("admin"))
                .avatarUrl(null)
                .fichas(999999999)
                .role(Role.ADMIN)
                .partidasGanadas(0)
                .esIA(false)
                .build();
    }

    private Mesa crearMesa(String nombre, int sb, int bb, int max) {
        return Mesa.builder()
                .nombre(nombre)
                .activa(true)
                .smallBlind(sb)
                .bigBlind(bb)
                .fase(Fase.PRE_FLOP)
                .pot(0)
                .maxJugadores(max)
                .build();
    }

    private UserMesa vincularUsuarioMesa(User user, Mesa mesa, int fichas) {
        return UserMesa.builder()
                .user(user)
                .mesa(mesa)
                .fichasEnMesa(fichas)
                .fichasDisponibles(fichas)
                .fichasIniciales(fichas)
                .enJuego(true)
                .conectado(true)
                .lastSeen(new Date())
                .build();
    }
}