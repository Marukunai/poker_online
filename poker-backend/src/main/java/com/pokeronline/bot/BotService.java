package com.pokeronline.bot;

import com.pokeronline.model.*;
import com.pokeronline.repository.TurnoRepository;
import com.pokeronline.repository.UserMesaRepository;
import com.pokeronline.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BotService {

    private final TurnoService turnoService;
    private final UserMesaRepository userMesaRepository;
    private final TurnoRepository turnoRepository;

    public void ejecutarTurnoBotConRetraso(Mesa mesa, User bot) {
        new Thread(() -> {
            try {
                int delay = ThreadLocalRandom.current().nextInt(10, 16); // 10–15 segundos
                Thread.sleep(delay * 1000L);

                Optional<Turno> turnoOpt = turnoRepository.findByMesaAndUser(mesa, bot);
                if (turnoOpt.isEmpty()) return;

                Turno turno = turnoOpt.get();
                if (!turno.isActivo()) return;

                Accion accion = decidirAccion(turno);
                int cantidad = calcularCantidad(turno, accion);

                turnoService.realizarAccion(mesa, bot, accion, cantidad);

            } catch (InterruptedException e) {
                System.err.println("Error en el delay del bot: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("Error ejecutando turno del bot: " + e.getMessage());
            }
        }).start();
    }

    private Accion decidirAccion(Turno turno) {
        // Lógica básica: el bot hace CHECK si puede, CALL si hay apuestas, y a veces RAISE
        int decision = ThreadLocalRandom.current().nextInt(100);
        int apuestaMaxima = turnoService.getApuestaMaxima(turno.getMesa());

        if (apuestaMaxima == 0) {
            if (decision < 70) return Accion.CHECK;
            else return Accion.RAISE;
        } else {
            if (decision < 60) return Accion.CALL;
            else if (decision < 85) return Accion.FOLD;
            else return Accion.RAISE;
        }
    }

    private int calcularCantidad(Turno turno, Accion accion) {
        User user = turno.getUser();
        Mesa mesa = turno.getMesa();
        UserMesa userMesa = userMesaRepository.findByUserAndMesa(user, mesa).orElse(null);

        if (userMesa == null) return 0;

        return switch (accion) {
            case RAISE -> Math.min(userMesa.getFichasEnMesa(), 100); // Raise fijo por ahora
            case ALL_IN -> userMesa.getFichasEnMesa();
            default -> 0;
        };
    }
}