package com.pokeronline.service;

import com.pokeronline.dto.ResultadoShowdownInterno;
import com.pokeronline.logros.service.LogroService;
import com.pokeronline.model.*;
import com.pokeronline.moderacion.model.MotivoSancion;
import com.pokeronline.moderacion.service.ModeracionService;
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

    private final RegistroAbandonoRepository registroAbandonoRepository;
    private final ModeracionService moderacionService;
    private final LogroService logroService;
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

        Map<Long, Integer> fichasInicioMano = mesa.getJugadores().stream()
                .collect(Collectors.toMap(jm -> jm.getUser().getId(), UserMesa::getFichasEnMesa));

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

                if (jm.getFichasEnMesa() == 0 && jm.getTotalApostado() > 0 && g.getUser().getId().equals(jm.getUser().getId())) {
                    logroService.otorgarLogroSiNoTiene(jm.getUser().getId(), "All-in Victorioso");
                }

                jm.setFichasEnMesa(jm.getFichasEnMesa() + premioPorJugador);

                userMesaRepository.save(jm);

                User u = jm.getUser();

                int fichasIniciales = fichasInicioMano.getOrDefault(u.getId(), 0);
                int totalFichasMesaInicio = fichasInicioMano.values().stream().mapToInt(Integer::intValue).sum();

                if (fichasIniciales > 0 && totalFichasMesaInicio > 0) {
                    double porcentaje = (double) fichasIniciales / totalFichasMesaInicio;
                    if (porcentaje < 0.10) {
                        logroService.otorgarLogroSiNoTiene(u.getId(), "Comeback");
                    }
                }

                int totalFichasMesa = mesa.getJugadores().stream().mapToInt(UserMesa::getFichasEnMesa).sum();
                if (totalFichasMesa > 0 && jm.getFichasEnMesa() < totalFichasMesa * 0.05) {
                    logroService.otorgarLogroSiNoTiene(u.getId(), "Superviviente");
                }

                if (premioPorJugador >= 20_000) {
                    logroService.otorgarLogroSiNoTiene(u.getId(), "Subidón");
                }

                u.setManosGanadas(u.getManosGanadas() + 1);
                u.setFichasGanadasHistoricas(u.getFichasGanadasHistoricas() + premioPorJugador);

                // Solo sumar fichas reales si la mesa NO es temporal
                if (!mesa.isFichasTemporales() && !u.isEsIA()) {
                    u.setFichas(u.getFichas() + premioPorJugador);
                }

                if (u.getFichas() >= 100_000) {
                    logroService.otorgarLogroSiNoTiene(u.getId(), "Jugador Rico");
                }

                if (u.getFichas() >= 1_000_000) {
                    logroService.otorgarLogroSiNoTiene(u.getId(), "Millonario");
                }

                userRepository.save(u);

                if (esMilagro(jm, participantes, mesa)) {
                    logroService.otorgarLogroSiNoTiene(u.getId(), "Milagro");
                }

                // OTORGAR LOGROS ESTRATEGIA SEGÚN MANO
                logroService.otorgarLogroSiNoTiene(u.getId(), switch (g.getTipo()) {
                    case TRIO -> "Trips";
                    case FULL_HOUSE -> "Full House";
                    case POKER -> "Poker";
                    case ESCALERA -> "Straight";
                    case COLOR -> "Flush";
                    case ESCALERA_REAL -> "Royal Flush";
                    default -> null;
                });

                final Map<Long, Integer> escaleraConsecutivas = new HashMap<>();
                final Map<Long, Integer> comboFuertes = new HashMap<>();

                // === ESCALERA DOBLES ===
                if (g.getTipo() == ManoTipo.ESCALERA) {
                    escaleraConsecutivas.put(u.getId(), escaleraConsecutivas.getOrDefault(u.getId(), 0) + 1);
                    if (escaleraConsecutivas.get(u.getId()) == 2) {
                        logroService.otorgarLogroSiNoTiene(u.getId(), "Escalera Dobles");
                        escaleraConsecutivas.put(u.getId(), 0);
                    }
                } else {
                    escaleraConsecutivas.put(u.getId(), 0);
                }

                // === COMBO LETAL ===
                if (List.of(ManoTipo.ESCALERA, ManoTipo.COLOR, ManoTipo.FULL_HOUSE, ManoTipo.POKER, ManoTipo.ESCALERA_REAL).contains(g.getTipo())) {
                    comboFuertes.put(u.getId(), comboFuertes.getOrDefault(u.getId(), 0) + 1);
                    if (comboFuertes.get(u.getId()) == 3) {
                        logroService.otorgarLogroSiNoTiene(u.getId(), "Combo Letal");
                        comboFuertes.put(u.getId(), 0);
                    }
                } else {
                    comboFuertes.put(u.getId(), 0);
                }

                // OTORGAR LOGRO CONTRA BOTS
                if (mesa.isFichasTemporales() && mesa.getJugadores().stream().anyMatch(j -> j.getUser().isEsIA())) {
                    logroService.otorgarLogroSiNoTiene(u.getId(), "Vencedor Bot");

                    long victoriasVsBots = historialManoRepository.contarVictoriasVsBots(u);
                    if (victoriasVsBots >= 10) {
                        logroService.otorgarLogroSiNoTiene(u.getId(), "Derrotador de Máquinas");
                    }

                    long botsDificiles = mesa.getJugadores().stream()
                            .filter(j -> j.getUser().isEsIA() && j.getUser().getNivelBot() != null && j.getUser().getNivelBot().name().equals("DIFICIL"))
                            .count();
                    if (botsDificiles > 0) {
                        logroService.otorgarLogroSiNoTiene(u.getId(), "Humillador de IA");
                    }

                    long totalBots = mesa.getJugadores().stream().filter(j -> j.getUser().isEsIA()).count();
                    if (totalBots >= 3) {
                        logroService.otorgarLogroSiNoTiene(u.getId(), "El futuro contra mí");
                    }
                }

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
            // LOGROS POR NÚMERO DE MANOS JUGADAS
            if (u.getManosJugadas() >= 100) {
                logroService.otorgarLogroSiNoTiene(u.getId(), "Mano Centenaria");
            } else if (u.getManosJugadas() >= 50) {
                logroService.otorgarLogroSiNoTiene(u.getId(), "Profesional");
            } else if (u.getManosJugadas() >= 10) {
                logroService.otorgarLogroSiNoTiene(u.getId(), "Veterano");
            } else if (u.getManosJugadas() >= 1) {
                logroService.otorgarLogroSiNoTiene(u.getId(), "Primera Partida");
            }

            if (ganadoresFinales.contains(u)) {

                if (mesa.getPrivada()) {
                    logroService.otorgarLogroSiNoTiene(u.getId(), "Victoria Privada");
                }

                u.setRachaVictorias(u.getRachaVictorias() + 1);
                u.setRachaDerrotas(0);

                if (u.getRachaVictorias() == 3) {
                    logroService.otorgarLogroSiNoTiene(u.getId(), "Racha de la Suerte");
                }
            } else {
                u.setRachaVictorias(0);
                u.setRachaDerrotas(u.getRachaDerrotas() + 1);

                if (u.getRachaDerrotas() == 3) {
                    logroService.otorgarLogroSiNoTiene(u.getId(), "Perdedor Persistente");
                }
            }

            userRepository.save(u);
            comprobarLogroJugadorDiario(u);
        }

        // === LOGRO PARTIDA EXPRESS ===
        if (mesa.getInicioMano() != null) {
            long duracionSegundos = (new Date().getTime() - mesa.getInicioMano().getTime()) / 1000;

            if (duracionSegundos < 120) {
                for (User ganador : ganadoresFinales) {
                    logroService.otorgarLogroSiNoTiene(ganador.getId(), "Partida Express");
                }
            }
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
            User user = jm.getUser();

            if (tieneSancionGrave(user)) {
                // Evitar que participe en la mano
                jm.setEnJuego(false);
                webSocketService.enviarMensajeJugador(user.getId(), "sancion", Map.of(
                        "mensaje", "No puedes participar en esta mano debido a una sanción activa."
                ));
                continue;
            }

            jm.setEnJuego(true);
            userMesaRepository.save(jm);
        }


        mesa.setInicioMano(new Date());
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

        for (UserMesa jm : jugadores) {
            if (tieneSancionGrave(jm.getUser())) {
                throw new IllegalStateException("El jugador " + jm.getUser().getUsername() + " tiene una sanción activa y no puede jugar.");
            }
        }

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

    private void comprobarLogroJugadorDiario(User user) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);
        Date hace7Dias = cal.getTime();

        List<Date> dias = historialManoRepository
                .obtenerDiasConPartida(user, hace7Dias)
                .stream()
                .map(date -> {
                    Calendar d = Calendar.getInstance();
                    d.setTime(date);
                    d.set(Calendar.HOUR_OF_DAY, 0);
                    d.set(Calendar.MINUTE, 0);
                    d.set(Calendar.SECOND, 0);
                    d.set(Calendar.MILLISECOND, 0);
                    return d.getTime();
                })
                .distinct()
                .toList();

        if (dias.size() >= 7) {
            logroService.otorgarLogroSiNoTiene(user.getId(), "Jugador Diario");
        }
    }

    private boolean esMilagro(UserMesa ganador, List<UserMesa> participantes, Mesa mesa) {
        // Cartas hasta el TURN
        List<String> comunidadTurn = List.of(
                mesa.getFlop1(), mesa.getFlop2(), mesa.getFlop3(), mesa.getTurn()
        );

        // Cartas completas con el RIVER
        List<String> comunidadRiver = new ArrayList<>(comunidadTurn);
        comunidadRiver.add(mesa.getRiver());

        // Evaluación final
        ManoEvaluada manoFinal = evaluadorManoService.evaluarMano(
                ganador.getUser(),
                List.of(ganador.getCarta1(), ganador.getCarta2()),
                comunidadRiver
        );

        // Evaluación sin river
        ManoEvaluada manoAntesRiver = evaluadorManoService.evaluarMano(
                ganador.getUser(),
                List.of(ganador.getCarta1(), ganador.getCarta2()),
                comunidadTurn
        );

        int fuerzaGanadorAntes = manoAntesRiver.getFuerza();
        int fuerzaGanadorDespues = manoFinal.getFuerza();

        // Evaluar si otro jugador ganaba antes del river
        for (UserMesa p : participantes) {
            if (p.getUser().getId().equals(ganador.getUser().getId())) continue;

            ManoEvaluada manoRivalAntes = evaluadorManoService.evaluarMano(
                    p.getUser(),
                    List.of(p.getCarta1(), p.getCarta2()),
                    comunidadTurn
            );

            if (manoRivalAntes.getFuerza() > fuerzaGanadorAntes) {
                // El ganador iba perdiendo antes del river
                return fuerzaGanadorDespues > fuerzaGanadorAntes;
            }
        }

        return false;
    }

    private boolean tieneSancionGrave(User user) {
        return user.getSanciones().stream().anyMatch(s -> {
            MotivoSancion m = s.getMotivo();
            boolean activa = s.getFechaFin() == null || s.getFechaFin().after(new Date());

            return activa && Set.of(
                    MotivoSancion.INFRACCIONES_GRAVES,
                    MotivoSancion.REITERACION_INFRACCIONES,
                    MotivoSancion.FRAUDE_DE_FICHAS,
                    MotivoSancion.MANIPULACION_RESULTADOS,
                    MotivoSancion.TRAMPAS_EN_PARTIDAS_PRIVADAS,
                    MotivoSancion.COLUSION_ENTRE_JUGADORES,
                    MotivoSancion.MULTICUENTA,
                    MotivoSancion.USO_DE_BOTS
            ).contains(m);
        });
    }

    private void registrarAbandono(User user, Mesa mesa) {
        RegistroAbandono abandono = RegistroAbandono.builder()
                .user(user)
                .mesa(mesa)
                .fecha(new Date())
                .build();
        registroAbandonoRepository.save(abandono);

        // Comprobar si ha abandonado demasiadas veces en los últimos días
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -3); // últimos 3 días
        long totalAbandonos = registroAbandonoRepository.contarAbandonosRecientes(user, cal.getTime());

        if (totalAbandonos >= 3) {
            moderacionService.sancionarAutomaticamente(user, "ABANDONO_REITERADO");
        }
    }
}