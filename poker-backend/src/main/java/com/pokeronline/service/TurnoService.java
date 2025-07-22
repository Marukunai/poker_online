package com.pokeronline.service;

import com.pokeronline.bot.BotEngineService;
import com.pokeronline.bot.BotService;
import com.pokeronline.logros.service.LogroService;
import com.pokeronline.model.*;
import com.pokeronline.repository.*;
import com.pokeronline.websocket.WebSocketService;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

import java.util.List;

@Service
public class TurnoService implements BotEngineService {

    private final LogroService logroService;
    private final BotService botService;
    private final WebSocketService webSocketService;
    private final AccionPartidaRepository accionPartidaRepository;
    private final BarajaService barajaService;
    private final TurnoRepository turnoRepository;
    private final UserMesaRepository userMesaRepository;
    private final UserRepository userRepository;
    private final MesaRepository mesaRepository;

    public TurnoService(
            @Lazy BotService botService,
            WebSocketService webSocketService,
            AccionPartidaRepository accionPartidaRepository,
            BarajaService barajaService,
            TurnoRepository turnoRepository,
            UserMesaRepository userMesaRepository,
            UserRepository userRepository,
            MesaRepository mesaRepository,
            LogroService logroService
    ) {
        this.botService = botService;
        this.webSocketService = webSocketService;
        this.accionPartidaRepository = accionPartidaRepository;
        this.barajaService = barajaService;
        this.turnoRepository = turnoRepository;
        this.userMesaRepository = userMesaRepository;
        this.userRepository = userRepository;
        this.mesaRepository = mesaRepository;
        this.logroService = logroService;
    }

    @Transactional
    public void inicializarTurnos(Mesa mesa) {
        turnoRepository.deleteAllByMesa(mesa);

        List<UserMesa> jugadoresEnJuego = userMesaRepository.findByMesa(mesa).stream()
                .filter(UserMesa::isEnJuego)
                .toList();

        // Buscar índice del jugador que está justo después del BIG_BLIND
        int indexInicio;

        if (jugadoresEnJuego.size() == 2) {
            // En partidas heads-up (2 jugadores), Small Blind es el dealer y empieza él
            indexInicio = jugadoresEnJuego.indexOf(
                    jugadoresEnJuego.stream()
                            .filter(j -> j.getPosicion() == Posicion.SMALL_BLIND)
                            .findFirst()
                            .orElse(jugadoresEnJuego.get(0))
            );
        } else {
            // Más de 2 jugadores: empieza el que está a la izquierda del Big Blind
            int indexBB = jugadoresEnJuego.indexOf(
                    jugadoresEnJuego.stream()
                            .filter(j -> j.getPosicion() == Posicion.BIG_BLIND)
                            .findFirst()
                            .orElse(jugadoresEnJuego.get(0))
            );
            indexInicio = (indexBB + 1) % jugadoresEnJuego.size();
        }

        // Rotamos la lista desde el jugador que debe empezar
        List<UserMesa> ordenTurnos = new ArrayList<>();
        for (int i = 0; i < jugadoresEnJuego.size(); i++) {
            ordenTurnos.add(jugadoresEnJuego.get((indexInicio + i) % jugadoresEnJuego.size()));
        }

        // Crear turnos
        for (int i = 0; i < ordenTurnos.size(); i++) {
            UserMesa jm = ordenTurnos.get(i);

            Turno turno = Turno.builder()
                    .mesa(mesa)
                    .user(jm.getUser())
                    .accion(null)
                    .apuesta(0)
                    .ordenTurno(i)
                    .activo(i == 0) // Solo el primero está activo
                    .eliminado(false)
                    .build();

            turnoRepository.save(turno);

            jm.setTotalApostado(0); // Reiniciamos su apuesta total
            userMesaRepository.save(jm);
        }

        mesa.setPot(mesa.getPot()); // Ya tiene las ciegas aplicadas
        mesa.setFase(Fase.PRE_FLOP);
        mesaRepository.save(mesa);

        // Notificar al frontend quién tiene el turno
        turnoRepository.findByMesaAndActivoTrue(mesa).ifPresent(turnoActivo -> webSocketService.enviarMensajeMesa(mesa.getId(), "turno", Map.of(
                "jugador", turnoActivo.getUser().getUsername()
        )));

        // Activamos temporizador del primer turno
        iniciarTemporizadorTurno(mesa);
    }

    public Turno getTurnoActual(Mesa mesa) {
        Turno turno = turnoRepository.findByMesaAndActivoTrue(mesa)
                .orElseThrow(() -> new RuntimeException("No hay turno activo"));

        UserMesa userMesa = userMesaRepository.findByUserAndMesa(turno.getUser(), mesa)
                .orElse(null);

        if (userMesa != null) {
            // 💡 Verificar si ha estado inactivo más de 120s
            if (userMesa.isConectado() && userMesa.getLastSeen() != null) {
                long diffMillis = System.currentTimeMillis() - userMesa.getLastSeen().getTime();
                if (diffMillis > 120000) { // 120 segundos sin señales
                    userMesa.setConectado(false);
                    userMesaRepository.save(userMesa);

                    System.out.printf("Jugador %s se marcó como desconectado por inactividad%n",
                            userMesa.getUser().getUsername());
                }
            }

            // Si sigue estando desconectado → forzar FOLD
            if (!userMesa.isConectado()) {
                userMesa.setLastSeen(new Date());
                userMesaRepository.save(userMesa);

                System.out.printf("⏱️ Jugador %s está desconectado desde %s → FOLD forzado.%n",
                        userMesa.getUser().getUsername(), userMesa.getLastSeen());

                turno.setAccion(Accion.FOLD);
                turno.setEliminado(true);
                turno.setActivo(false);
                turnoRepository.save(turno);

                avanzarTurno(mesa);
                return getTurnoActual(mesa); // Buscar siguiente jugador activo
            }
        }

        return turno;
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<Long, ScheduledFuture<?>> tareasProgramadas = new ConcurrentHashMap<>();

    public void iniciarTemporizadorTurno(Mesa mesa) {
        cancelarTemporizador(mesa.getId());

        Runnable tarea = () -> {
            try {
                Turno turno = getTurnoActual(mesa);
                realizarAccion(mesa, turno.getUser(), Accion.FOLD, 0);
                System.out.println("Turno forzado: FOLD por timeout.");
            } catch (Exception e) {
                System.err.println("Error forzando FOLD automático: " + e.getMessage());
            }
        };

        ScheduledFuture<?> tareaProgramada = scheduler.schedule(tarea, 60, TimeUnit.SECONDS);
        tareasProgramadas.put(mesa.getId(), tareaProgramada);
    }

    public void cancelarTemporizador(Long mesaId) {
        ScheduledFuture<?> tarea = tareasProgramadas.remove(mesaId);
        if (tarea != null) tarea.cancel(true);
    }

    private boolean esFinDeRonda(Mesa mesa) {
        List<Turno> turnos = turnoRepository.findByMesaOrderByOrdenTurno(mesa).stream()
                .filter(t -> !t.isEliminado())
                .toList();

        int apuestaMaxima = turnos.stream()
                .mapToInt(Turno::getApuesta)
                .max().orElse(0);

        Turno ultimoRaise = turnos.stream()
                .filter(t -> t.getAccion() == Accion.RAISE || t.getAccion() == Accion.ALL_IN)
                .reduce((first, second) -> second) // obtener el último raise
                .orElse(null);

        if (ultimoRaise == null) {
            // Nadie ha subido, la ronda termina si todos hicieron CHECK
            return turnos.stream().allMatch(t ->
                    t.getAccion() == Accion.CHECK || t.getAccion() == Accion.FOLD
            );
        }

        int indiceUltimoRaise = turnos.indexOf(ultimoRaise);

        // Deben actuar todos después del último raise
        for (int i = indiceUltimoRaise + 1; i < turnos.size(); i++) {
            Turno t = turnos.get(i);
            if (t.getAccion() == null || t.getApuesta() < apuestaMaxima) return false;
        }

        // Y también antes del raise si ya dio la vuelta
        for (int i = 0; i < indiceUltimoRaise; i++) {
            Turno t = turnos.get(i);
            if (t.getAccion() == null || t.getApuesta() < apuestaMaxima) return false;
        }

        return true;
    }

    private void inicializarNuevaRonda(Mesa mesa) {
        List<Turno> turnos = turnoRepository.findByMesaOrderByOrdenTurno(mesa).stream()
                .filter(t -> !t.isEliminado())
                .toList();

        for (Turno turno : turnos) {
            turno.setAccion(null);
            turno.setActivo(false);
            turnoRepository.save(turno);
        }

        if (!turnos.isEmpty()) {
            turnos.get(0).setActivo(true);
            turnoRepository.save(turnos.get(0));
            iniciarTemporizadorTurno(mesa);
        }
    }

    public void avanzarTurno(Mesa mesa) {
        List<Turno> turnos = turnoRepository.findByMesaOrderByOrdenTurno(mesa);

        int indiceActual = -1;
        for (int i = 0; i < turnos.size(); i++) {
            if (turnos.get(i).isActivo()) {
                turnos.get(i).setActivo(false);
                turnoRepository.save(turnos.get(i));
                indiceActual = i;
                break;
            }
        }
        if (indiceActual == -1) {
            for (int i = turnos.size() - 1; i >= 0; i--) {
                if (turnos.get(i).getAccion() != null) {
                    indiceActual = i;
                    break;
                }
            }
        }
        if (indiceActual == -1) {
            System.err.println("No se pudo encontrar el índice del turno actual.");
            return;
        }

        for (int j = 1; j < turnos.size(); j++) {
            int siguiente = (indiceActual + j) % turnos.size();
            UserMesa userMesa = userMesaRepository.findByUserAndMesa(turnos.get(siguiente).getUser(), mesa).orElse(null);
            if (!turnos.get(siguiente).isEliminado() && userMesa != null && userMesa.isConectado() && userMesa.getFichasEnMesa() > 0) {
                Turno nuevoTurno = turnos.get(siguiente);
                nuevoTurno.setActivo(true);
                turnoRepository.save(nuevoTurno);

                webSocketService.enviarMensajeMesa(mesa.getId(), "turno", Map.of(
                        "jugador", nuevoTurno.getUser().getUsername()
                ));

                iniciarTemporizadorTurno(mesa);

                if (nuevoTurno.getUser().isEsIA()) {
                    webSocketService.enviarMensajeMesa(mesa.getId(), "bot_actuando", Map.of(
                            "jugador", nuevoTurno.getUser().getUsername()
                    ));
                    botService.ejecutarTurnoBotConRetraso(mesa, nuevoTurno.getUser());
                }
                return;
            }
        }
        iniciarTemporizadorTurno(mesa);
    }

    public void avanzarFase(Mesa mesa) {
        Fase actual = mesa.getFase();
        switch (actual) {
            case PRE_FLOP -> {
                mesa.setFase(Fase.FLOP);
                mesa.setFlop1(barajaService.generarCartaAleatoria(mesa));
                mesa.setFlop2(barajaService.generarCartaAleatoria(mesa));
                mesa.setFlop3(barajaService.generarCartaAleatoria(mesa));
            }
            case FLOP -> {
                mesa.setFase(Fase.TURN);
                mesa.setTurn(barajaService.generarCartaAleatoria(mesa));
            }
            case TURN -> {
                mesa.setFase(Fase.RIVER);
                mesa.setRiver(barajaService.generarCartaAleatoria(mesa));
            }
            case RIVER -> mesa.setFase(Fase.SHOWDOWN);
            case SHOWDOWN -> throw new RuntimeException("La partida ya ha terminado");
        }

        mesaRepository.save(mesa);

        // Construcción segura de lista sin nulls
        List<String> cartas = new ArrayList<>();
        if (mesa.getFlop1() != null) cartas.add(mesa.getFlop1());
        if (mesa.getFlop2() != null) cartas.add(mesa.getFlop2());
        if (mesa.getFlop3() != null) cartas.add(mesa.getFlop3());
        if (mesa.getTurn() != null)  cartas.add(mesa.getTurn());
        if (mesa.getRiver() != null) cartas.add(mesa.getRiver());

        webSocketService.enviarMensajeMesa(mesa.getId(), "fase", Map.of(
                "fase", mesa.getFase().name(),
                "cartas", cartas
        ));
    }

    public void realizarAccion(Mesa mesa, User user, Accion accion, int cantidad) {
        cancelarTemporizador(mesa.getId());
        Turno turno = turnoRepository.findByMesaAndUser(mesa, user)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado para este jugador"));

        if (!turno.isActivo()) {
            throw new RuntimeException("No es tu turno");
        }

        UserMesa userMesa = userMesaRepository.findByUserAndMesa(user, mesa)
                .orElseThrow(() -> new RuntimeException("UserMesa no encontrado"));

        int apuestaMaxima = turnoRepository.findByMesaOrderByOrdenTurno(mesa).stream()
                .mapToInt(Turno::getApuesta)
                .max().orElse(0);

        turno.setAccion(accion);

        switch (accion) {
            case FOLD -> turno.setEliminado(true);

            case RAISE -> {
                int incremento = cantidad - turno.getApuesta();

                if (cantidad <= apuestaMaxima) {
                    throw new RuntimeException("Debes apostar más que la apuesta actual para hacer raise");
                }

                // Validar subida mínima
                List<Turno> turnos = turnoRepository.findByMesaOrderByOrdenTurno(mesa);
                List<Integer> raises = turnos.stream()
                        .filter(t -> t.getAccion() == Accion.RAISE || t.getAccion() == Accion.ALL_IN)
                        .map(Turno::getApuesta)
                        .toList();

                int ultimoIncremento = 0;
                if (raises.size() >= 2) {
                    int last = raises.get(raises.size() - 1);
                    int previous = raises.get(raises.size() - 2);
                    ultimoIncremento = last - previous;
                } else if (raises.size() == 1) {
                    ultimoIncremento = raises.get(0); // Primera subida desde las ciegas
                }

                if (incremento < ultimoIncremento) {
                    throw new RuntimeException("La subida mínima debe ser al menos de " + ultimoIncremento + " fichas");
                }

                if (userMesa.getFichasEnMesa() < incremento) {
                    throw new RuntimeException("No tienes suficientes fichas para hacer raise");
                }

                turno.setApuesta(cantidad);
                userMesa.setFichasEnMesa(userMesa.getFichasEnMesa() - incremento);
                userMesa.setTotalApostado(userMesa.getTotalApostado() + incremento);
                mesa.setPot(mesa.getPot() + incremento);

                // 💡 Considerar bluff si raise fuerte en fase temprana
                if (mesa.getFase() == Fase.PRE_FLOP || mesa.getFase() == Fase.FLOP) {
                    if (incremento >= mesa.getBigBlind() * 3) {
                        user.setVecesHizoBluff(user.getVecesHizoBluff() + 1);
                        userRepository.save(user); // Persistimos la estadística

                        logroService.otorgarLogroSiNoTiene(user.getId(), "Ganador con Bluff");
                        logroService.otorgarLogroSiNoTiene(user.getId(), "Bluff Master");
                    }
                }
            }

            case CALL -> {
                int diferencia = apuestaMaxima - turno.getApuesta();
                if (userMesa.getFichasEnMesa() < diferencia) {
                    throw new RuntimeException("No tienes suficientes fichas para hacer call");
                }

                if (diferencia > 0) {
                    turno.setApuesta(apuestaMaxima);
                    userMesa.setFichasEnMesa(userMesa.getFichasEnMesa() - diferencia);
                    userMesa.setTotalApostado(userMesa.getTotalApostado() + diferencia);
                    mesa.setPot(mesa.getPot() + diferencia);
                }
            }

            case CHECK -> {
                if (apuestaMaxima > 0) {
                    throw new RuntimeException("No puedes hacer check si hay apuestas en la mesa");
                }
                turno.setApuesta(0);
            }

            case ALL_IN -> {
                if (userMesa.getFichasEnMesa() < 1) {
                    throw new RuntimeException("Debes tener al menos 1 ficha para hacer All-In");
                }

                int incremento = userMesa.getFichasEnMesa();
                int nuevoTotal = turno.getApuesta() + incremento;

                turno.setApuesta(nuevoTotal);
                userMesa.setTotalApostado(userMesa.getTotalApostado() + incremento);
                mesa.setPot(mesa.getPot() + incremento);
                userMesa.setFichasEnMesa(0);
                logroService.otorgarLogroSiNoTiene(user.getId(), "Sin fichas");

                user.setVecesAllIn(user.getVecesAllIn() + 1);
                userRepository.save(user); // Persistimos la estadística
                logroService.otorgarLogroSiNoTiene(user.getId(), "All-in");
                if (user.getVecesAllIn() >= 50) {
                    logroService.otorgarLogroSiNoTiene(user.getId(), "All-in Master");
                }
            }
        }

        turno.setActivo(false);
        turnoRepository.save(turno);
        userMesaRepository.save(userMesa);
        mesaRepository.save(mesa);

        accionPartidaRepository.save(AccionPartida.builder()
                .mesa(mesa)
                .user(user)
                .accion(accion)
                .cantidad(cantidad)
                .timestamp(new Date())
                .build());

        webSocketService.enviarMensajeMesa(mesa.getId(), "accion", Map.of(
                "jugador", user.getUsername(),
                "accion", accion.name(),
                "cantidad", cantidad
        ));

        // Verificar si termina ronda
        if (esFinDeRonda(mesa)) {
            System.out.println("Fin de ronda detectado. Avanzando a la siguiente fase...");
            avanzarFase(mesa);
            inicializarNuevaRonda(mesa);
        } else {
            avanzarTurno(mesa);
        }
    }

    @Override
    public int getApuestaMaxima(Mesa mesa) {
        return turnoRepository.findByMesaOrderByOrdenTurno(mesa).stream()
                .mapToInt(Turno::getApuesta)
                .max()
                .orElse(0);
    }

    @Override
    public void ejecutarAccionBot(Mesa mesa, User bot, Accion accion, int cantidad) {
        realizarAccion(mesa, bot, accion, cantidad);
    }
}