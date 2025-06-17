package com.pokeronline.service;

import com.pokeronline.model.*;
import com.pokeronline.repository.MesaRepository;
import com.pokeronline.repository.TurnoRepository;
import com.pokeronline.repository.UserMesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoService {
    private final BarajaService barajaService;
    private final TurnoRepository turnoRepository;
    private final UserMesaRepository userMesaRepository;
    private final MesaRepository mesaRepository;

    public void inicializarTurnos(Mesa mesa) {
        turnoRepository.deleteAllByMesa(mesa);

        List<UserMesa> jugadores = userMesaRepository.findByMesa(mesa).stream()
                .filter(UserMesa::isEnJuego)
                .toList();

        for (int i = 0; i < jugadores.size(); i++) {
            Turno turno = Turno.builder()
                    .mesa(mesa)
                    .user(jugadores.get(i).getUser())
                    .accion(null)
                    .apuesta(0)
                    .ordenTurno(i)
                    .activo(i == 0)
                    .eliminado(false)
                    .build();
            turnoRepository.save(turno);

            jugadores.get(i).setTotalApostado(0);
            userMesaRepository.save(jugadores.get(i));
        }

        mesa.setPot(0);
        mesa.setFase(Fase.PRE_FLOP);
        mesaRepository.save(mesa);
    }

    public Turno getTurnoActual(Mesa mesa) {
        return turnoRepository.findByMesaAndActivoTrue(mesa)
                .orElseThrow(() -> new RuntimeException("No hay turno activo"));
    }

    public void avanzarTurno(Mesa mesa) {
        List<Turno> turnos = turnoRepository.findByMesaOrderByOrdenTurno(mesa);

        for (int i = 0; i < turnos.size(); i++) {
            if (turnos.get(i).isActivo()) {
                turnos.get(i).setActivo(false);
                turnoRepository.save(turnos.get(i));

                for (int j = 1; j < turnos.size(); j++) {
                    int siguiente = (i + j) % turnos.size();
                    if (!turnos.get(siguiente).isEliminado()) {
                        turnos.get(siguiente).setActivo(true);
                        turnoRepository.save(turnos.get(siguiente));
                        return;
                    }
                }
            }
        }
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
    }

    public void realizarAccion(Mesa mesa, User user, Accion accion, int cantidad) {
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
                if (cantidad <= apuestaMaxima) {
                    throw new RuntimeException("Debes apostar más que la apuesta actual para hacer raise");
                }
                int incremento = cantidad - turno.getApuesta();
                turno.setApuesta(cantidad);
                userMesa.setTotalApostado(userMesa.getTotalApostado() + incremento);
                mesa.setPot(mesa.getPot() + incremento);
            }
            case CALL -> {
                int diferencia = apuestaMaxima - turno.getApuesta();
                if (diferencia > 0) {
                    turno.setApuesta(apuestaMaxima);
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
                int incremento = cantidad - turno.getApuesta();
                turno.setApuesta(cantidad);
                userMesa.setTotalApostado(userMesa.getTotalApostado() + incremento);
                mesa.setPot(mesa.getPot() + incremento);
            }
        }

        turno.setActivo(false);
        turnoRepository.save(turno);
        userMesaRepository.save(userMesa);
        mesaRepository.save(mesa);

        avanzarTurno(mesa);
    }
}