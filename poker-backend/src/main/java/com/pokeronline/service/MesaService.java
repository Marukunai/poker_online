package com.pokeronline.service;

import com.pokeronline.dto.ResultadoShowdownInterno;
import com.pokeronline.model.*;
import com.pokeronline.repository.*;
import com.pokeronline.websocket.WebSocketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MesaService {

    private final WebSocketService webSocketService;
    private final HistorialManoRepository historialManoRepository;
    private final UserRepository userRepository;
    private final TurnoRepository turnoRepository;
    private final TurnoService turnoService;
    private final BarajaService barajaService;
    private final MesaRepository mesaRepository;
    private final UserMesaRepository userMesaRepository;
    private final EvaluadorManoService evaluadorManoService;

    public ResultadoShowdownInterno resolverShowdown(Mesa mesa) {
        List<UserMesa> jugadoresActivos = userMesaRepository.findByMesa(mesa).stream()
                .filter(UserMesa::isEnJuego)
                .filter(UserMesa::isConectado)
                .filter(j -> j.getCarta1() != null && j.getCarta2() != null)
                .toList();

        if (jugadoresActivos.isEmpty()) return new ResultadoShowdownInterno(List.of(), null, List.of());

        Map<UserMesa, Integer> apuestas = new HashMap<>();
        for (UserMesa jm : jugadoresActivos) {
            apuestas.put(jm, jm.getTotalApostado());
        }

        List<UserMesa> ordenados = new ArrayList<>(apuestas.keySet());
        ordenados.sort(Comparator.comparingInt(apuestas::get));

        int potRestante = mesa.getPot();
        List<User> ganadoresFinales = new ArrayList<>();
        ManoTipo tipoGanador = null;
        List<String> cartasGanadoras = List.of();

        while (!ordenados.isEmpty() && potRestante > 0) {
            int cantidadMinima = apuestas.get(ordenados.get(0));

            List<UserMesa> participantes = ordenados.stream()
                    .filter(j -> apuestas.get(j) >= cantidadMinima)
                    .toList();

            int sidePot = cantidadMinima * participantes.size();

            List<ManoEvaluada> evaluaciones = participantes.stream()
                    .map(j -> evaluadorManoService.evaluarMano(
                            j.getUser(),
                            List.of(j.getCarta1(), j.getCarta2()),
                            List.of(mesa.getFlop1(), mesa.getFlop2(), mesa.getFlop3(), mesa.getTurn(), mesa.getRiver())
                    )).toList();

            int mejorFuerza = evaluaciones.stream().mapToInt(ManoEvaluada::getFuerza).max().orElse(0);
            List<ManoEvaluada> ganadores = evaluaciones.stream()
                    .filter(e -> e.getFuerza() == mejorFuerza)
                    .toList();

            if (tipoGanador == null && !ganadores.isEmpty()) {
                tipoGanador = ganadores.get(0).getTipo();
                cartasGanadoras = ganadores.get(0).getCartasGanadoras();
            }

            int premioPorJugador = sidePot / ganadores.size();

            for (ManoEvaluada g : ganadores) {
                UserMesa jm = participantes.stream()
                        .filter(p -> p.getUser().getId().equals(g.getUser().getId()))
                        .findFirst()
                        .orElseThrow();

                jm.setFichasEnMesa(jm.getFichasEnMesa() + premioPorJugador);
                userMesaRepository.save(jm);

                User u = jm.getUser();
                u.setManosGanadas(u.getManosGanadas() + 1);
                u.setFichasGanadasHistoricas(u.getFichasGanadasHistoricas() + premioPorJugador);

                // Solo sumar fichas reales si la mesa NO es temporal
                if (!mesa.isFichasTemporales() && !u.isEsIA()) {
                    u.setFichas(u.getFichas() + premioPorJugador);
                }

                userRepository.save(u);

                ganadoresFinales.add(u);

                HistorialMano historial = HistorialMano.builder()
                        .jugador(u)
                        .mesa(mesa)
                        .cartasJugador(jm.getCarta1() + "," + jm.getCarta2())
                        .cartasGanadoras(String.join(",", g.getCartasGanadoras()))
                        .tipoManoGanadora(String.valueOf(g.getTipo()))
                        .fecha(new Date())
                        .fichasGanadas(premioPorJugador)
                        .contraJugadores(
                                participantes.stream()
                                        .filter(p -> !p.getUser().getId().equals(u.getId()))
                                        .map(p -> p.getUser().getUsername())
                                        .collect(Collectors.joining(", "))
                        )
                        .faseFinal(Fase.valueOf(mesa.getFase().name()))
                        .build();
                historialManoRepository.save(historial);
            }

            potRestante -= sidePot;

            // iterar sobre copia de claves
            List<UserMesa> claves = new ArrayList<>(apuestas.keySet());
            for (UserMesa jm : claves) {
                apuestas.compute(jm, (k, anterior) -> Math.max(0, anterior != null ? anterior - cantidadMinima : 0));
            }

            ordenados.removeIf(j -> apuestas.get(j) == 0);
        }

        mesa.setPot(0);
        mesaRepository.save(mesa);

        webSocketService.enviarMensajeMesa(mesa.getId(), "showdown", Map.of(
                "ganadores", ganadoresFinales.stream().map(User::getUsername).toList(),
                "tipo", tipoGanador != null ? tipoGanador.name() : "NINGUNO",
                "cartas", cartasGanadoras
        ));

        for (UserMesa jm : jugadoresActivos) {
            User u = jm.getUser();
            u.setManosJugadas(u.getManosJugadas() + 1);
            userRepository.save(u);
        }

        return new ResultadoShowdownInterno(ganadoresFinales, tipoGanador, cartasGanadoras);
    }

    @Transactional
    public void iniciarNuevaMano(Mesa mesa) {
        // Reiniciar pot y fase
        turnoService.cancelarTemporizador(mesa.getId());
        mesa.setPot(0);
        mesa.setFase(Fase.PRE_FLOP);

        // Resetear cartas y baraja
        barajaService.reiniciarCartasMesa(mesa);
        barajaService.reiniciarBaraja(mesa);

        // Marcar jugadores como en juego
        List<UserMesa> jugadores = userMesaRepository.findByMesa(mesa);
        for (UserMesa jm : jugadores) {
            jm.setEnJuego(true);
            userMesaRepository.save(jm);
        }

        mesaRepository.save(mesa);
        turnoRepository.deleteAllByMesa(mesa);
        asignarPosicionesYAplicarCiegas(mesa);
        turnoService.inicializarTurnos(mesa);
    }
    @Transactional
    public String finalizarMano(Mesa mesa) {
        List<UserMesa> jugadoresConFichas = userMesaRepository.findByMesa(mesa).stream()
                .filter(j -> j.getFichasEnMesa() > 0)
                .toList();

        if (jugadoresConFichas.size() <= 1) {
            mesa.setActiva(false);
            mesaRepository.save(mesa);
            return "Partida finalizada. No quedan suficientes jugadores con fichas.";
        }

        iniciarNuevaMano(mesa);
        return "Nueva mano iniciada.";
    }

    public void asignarPosicionesYAplicarCiegas(Mesa mesa) {
        List<UserMesa> jugadores = userMesaRepository.findByMesa(mesa).stream()
                .filter(UserMesa::isEnJuego)
                .sorted(Comparator.comparing(um -> um.getUser().getId()))
                .toList();

        if (jugadores.size() < 2) {
            throw new RuntimeException("Se necesitan al menos 2 jugadores para asignar posiciones.");
        }

        // Encontrar el dealer anterior
        int indexDealer = -1;
        for (int i = 0; i < jugadores.size(); i++) {
            if (jugadores.get(i).getPosicion() == Posicion.DEALER) {
                indexDealer = i;
                break;
            }
        }

        List<UserMesa> ordenados;
        if (indexDealer != -1) {
            // Iniciar desde el siguiente al dealer
            ordenados = new ArrayList<>();
            for (int i = 1; i <= jugadores.size(); i++) {
                ordenados.add(jugadores.get((indexDealer + i) % jugadores.size()));
            }
        } else {
            ordenados = new ArrayList<>(jugadores);
        }

        int smallBlind = mesa.getSmallBlind();
        int bigBlind = mesa.getBigBlind();

        for (int i = 0; i < ordenados.size(); i++) {
            UserMesa jm = ordenados.get(i);

            if (i == 0) {
                jm.setPosicion(Posicion.DEALER);
            } else if (i == 1) {
                jm.setPosicion(Posicion.SMALL_BLIND);
                aplicarCiega(jm, smallBlind, mesa);
            } else if (i == 2) {
                jm.setPosicion(Posicion.BIG_BLIND);
                aplicarCiega(jm, bigBlind, mesa);
            } else {
                jm.setPosicion(Posicion.JUGADOR);
            }

            userMesaRepository.save(jm);
        }

        mesaRepository.save(mesa);
    }

    private void aplicarCiega(UserMesa jm, int cantidad, Mesa mesa) {
        int fichas = jm.getFichasEnMesa();
        int apuesta = Math.min(cantidad, fichas); // All-in si no llega

        jm.setFichasEnMesa(fichas - apuesta);
        jm.setTotalApostado(jm.getTotalApostado() + apuesta);
        mesa.setPot(mesa.getPot() + apuesta);
    }
}