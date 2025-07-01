package com.pokeronline.bot;

import com.pokeronline.model.Accion;
import com.pokeronline.model.EstiloBot;
import com.pokeronline.model.User;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FrasesBotChat {

    private static final Map<EstiloBot, Map<Accion, List<String>>> frases = Map.of(
            EstiloBot.AGRESIVO, Map.of(
                    Accion.RAISE, List.of("¡A ver si aguantas esta!", "No me tiembla el pulso.", "¿Tienes lo que hay que tener?"),
                    Accion.ALL_IN, List.of("¡Voy con todo! 😈", "All in. ¡No hay marcha atrás!", "¡A tumba abierta!"),
                    Accion.CALL, List.of("Acepto tu reto.", "Veamos qué pasa.", "Estoy dentro."),
                    Accion.CHECK, List.of("Tu turno.", "Paso, por ahora...", "Vamos lentos..."),
                    Accion.FOLD, List.of("Bah, suerte tuviste...", "Nos vemos en la siguiente.")
            ),
            EstiloBot.CONSERVADOR, Map.of(
                    Accion.RAISE, List.of("Creo que puedo ganar esta.", "Subo, pero con cuidado."),
                    Accion.ALL_IN, List.of("Estoy seguro... espero...", "Todo o nada."),
                    Accion.CALL, List.of("No estoy convencido, pero voy.", "Igualaré, solo esta vez."),
                    Accion.CHECK, List.of("Paso, a ver qué haces.", "No me la juego todavía."),
                    Accion.FOLD, List.of("Demasiado arriesgado.", "Prefiero retirarme.")
            ),
            EstiloBot.LOOSE, Map.of(
                    Accion.RAISE, List.of("¿Por qué no? Subamos.", "¡Sube la apuesta!"),
                    Accion.ALL_IN, List.of("All in, total... ¿qué puede pasar?", "¡A ver qué tal me sale esto!"),
                    Accion.CALL, List.of("Voy, aunque no debería.", "No tengo nada que perder."),
                    Accion.CHECK, List.of("Paso, por si acaso.", "Vamos a ver otra carta."),
                    Accion.FOLD, List.of("Esta mano no era para mí.", "Ya volveré más fuerte.")
            ),
            EstiloBot.TIGHT, Map.of(
                    Accion.RAISE, List.of("Solo juego buenas manos.", "Subo con confianza."),
                    Accion.ALL_IN, List.of("Esta mano lo vale.", "Apuesto todo porque sé que gano."),
                    Accion.CALL, List.of("Igualaré, tengo algo sólido.", "Voy con cuidado."),
                    Accion.CHECK, List.of("No necesito presionar.", "Tu jugada."),
                    Accion.FOLD, List.of("No es el momento.", "Esta no es mi guerra.")
            ),
            EstiloBot.DEFAULT, Map.of(
                    Accion.RAISE, List.of("Subo.", "Veamos si sigues."),
                    Accion.ALL_IN, List.of("Voy con todo.", "¡All in!"),
                    Accion.CALL, List.of("Voy.", "A igualar."),
                    Accion.CHECK, List.of("Paso.", "Te toca."),
                    Accion.FOLD, List.of("Me retiro.", "Paso esta vez.")
            )
    );

    public static String obtenerFrase(User bot, Accion accion) {
        EstiloBot estilo = Optional.ofNullable(bot.getEstiloBot()).orElse(EstiloBot.DEFAULT);
        List<String> opciones = frases
                .getOrDefault(estilo, frases.get(EstiloBot.DEFAULT))
                .getOrDefault(accion, List.of("..."));
        return opciones.get(ThreadLocalRandom.current().nextInt(opciones.size()));
    }
}