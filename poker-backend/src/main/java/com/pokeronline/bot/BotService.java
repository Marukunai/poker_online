package com.pokeronline.bot;

import com.pokeronline.model.*;
import com.pokeronline.service.EvaluadorManoService;
import com.pokeronline.repository.UserMesaRepository;
import com.pokeronline.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotService {

    private final WebSocketService webSocketService;
    private final EvaluadorManoService evaluadorManoService;
    private final UserMesaRepository userMesaRepository;
    private final BotEngineService botEngineService;

    public void ejecutarTurnoBotConRetraso(Mesa mesa, User bot) {
        new Thread(() -> {
            try {
                int delay = ThreadLocalRandom.current().nextInt(10, 16);
                Thread.sleep(delay * 1000L);
                realizarAccionBot(mesa, bot);
            } catch (InterruptedException e) {
                log.warn("Bot {} interrumpido en mesa {}", bot.getUsername(), mesa.getId(), e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void realizarAccionBot(Mesa mesa, User bot) {
        UserMesa userMesa = userMesaRepository.findByUserAndMesa(bot, mesa).orElseThrow();

        List<String> cartasJugador = List.of(userMesa.getCarta1(), userMesa.getCarta2());
        List<String> comunitarias = Stream.of(
                mesa.getFlop1(), mesa.getFlop2(), mesa.getFlop3(),
                mesa.getTurn(), mesa.getRiver()
        ).filter(Objects::nonNull).toList();

        ManoEvaluada mano = evaluadorManoService.evaluarMano(bot, cartasJugador, comunitarias);

        int apuestaMaxima = botEngineService.getApuestaMaxima(mesa);
        BotDecisionEngine.Decision decision = BotDecisionEngine.decidirAccion(bot, userMesa, mesa, mano, apuestaMaxima);

        botEngineService.ejecutarAccionBot(mesa, bot, decision.accion(), decision.cantidad());

        String frase = fraseChatSegunAccion(bot, decision.accion());
        webSocketService.enviarMensajeMesa(mesa.getId(), "chat", Map.of(
                "jugador", bot.getUsername(),
                "mensaje", frase
        ));
    }

    public static String fraseChatSegunAccion(User user, Accion accion) {
        return FrasesBotChat.obtenerFrase(user, accion);
    }
}