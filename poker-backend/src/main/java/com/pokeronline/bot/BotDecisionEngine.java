package com.pokeronline.bot;

import com.pokeronline.model.*;

import java.util.*;
import java.util.stream.Stream;

public class BotDecisionEngine {

    public record Decision(Accion accion, int cantidad) {}

    public static Decision decidirAccion(User bot, UserMesa userMesa, Mesa mesa, ManoEvaluada mano, int apuestaMaxima) {
        DificultadBot nivel = Optional.ofNullable(bot.getNivelBot()).orElse(DificultadBot.FACIL);
        EstiloBot estilo = Optional.ofNullable(bot.getEstiloBot()).orElse(EstiloBot.DEFAULT);
        Fase fase = mesa.getFase();
        int fuerza = (mano != null) ? mano.getFuerza() : -1;
        int fichas = userMesa.getFichasEnMesa();
        int bigBlind = mesa.getBigBlind();
        List<String> cartas = List.of(userMesa.getCarta1(), userMesa.getCarta2());

        // Flags
        boolean pareja = esPareja(cartas);
        boolean parejaAlta = esParejaAlta(cartas);
        boolean conectadas = esConectadas(cartas);
        boolean suited = esSuited(cartas);
        boolean suitedConectadas = conectadas && suited;
        boolean flushDraw = hayFlushDraw(cartas, mesa);
        boolean straightDraw = hayStraightDraw(cartas, mesa);

        // Estilo → agresividad
        double agresividad = switch (estilo) {
            case AGRESIVO -> 1.4;
            case CONSERVADOR -> 0.7;
            case LOOSE -> 1.2;
            case TIGHT -> 0.8;
            case DEFAULT -> 1.0;
        };

        // Probabilidades de bluff según dificultad
        boolean bluff = switch (nivel) {
            case FACIL -> false;
            case NORMAL -> Math.random() < 0.06;
            case DIFICIL -> Math.random() < 0.12;
        };
        boolean slowplay = nivel == DificultadBot.DIFICIL && fuerza >= 8 && Math.random() < 0.2;

        // Decisión
        return switch (nivel) {
            case FACIL -> {
                if (fase == Fase.PRE_FLOP) {
                    if (parejaAlta) yield new Decision(Accion.ALL_IN, fichas);
                    if (pareja) yield new Decision(Accion.RAISE, apuestaMaxima + (int) Math.ceil(bigBlind * agresividad));
                    yield apuestaMaxima == 0 ? new Decision(Accion.CHECK, 0) : new Decision(Accion.CALL, apuestaMaxima);
                }

                if (fuerza >= 8) yield new Decision(Accion.ALL_IN, fichas);
                if (fuerza >= 5)
                    yield new Decision(Accion.RAISE, apuestaMaxima + (int) Math.ceil(bigBlind * agresividad));
                if (fuerza >= 2)
                    yield apuestaMaxima == 0 ? new Decision(Accion.CHECK, 0) : new Decision(Accion.CALL, apuestaMaxima);
                yield apuestaMaxima == 0 ? new Decision(Accion.CHECK, 0) : new Decision(Accion.FOLD, 0);
            }

            case NORMAL -> switch (fase) {
                case PRE_FLOP -> {
                    if (parejaAlta || suitedConectadas)
                        yield new Decision(Accion.RAISE, apuestaMaxima + (int) Math.ceil(bigBlind * agresividad));
                    if (pareja || conectadas || suited)
                        yield apuestaMaxima == 0 ? new Decision(Accion.CHECK, 0) : new Decision(Accion.CALL, apuestaMaxima);
                    yield apuestaMaxima == 0 ? new Decision(Accion.CHECK, 0) : new Decision(Accion.FOLD, 0);
                }
                case FLOP, TURN -> {
                    if (fuerza >= 6 || flushDraw || straightDraw)
                        yield new Decision(Accion.RAISE, apuestaMaxima + (int) Math.ceil(bigBlind * agresividad));
                    if (fuerza >= 3)
                        yield apuestaMaxima == 0 ? new Decision(Accion.CHECK, 0) : new Decision(Accion.CALL, apuestaMaxima);
                    yield apuestaMaxima == 0 ? new Decision(Accion.CHECK, 0) : new Decision(Accion.FOLD, 0);
                }
                case RIVER -> {
                    if (fuerza >= 9)
                        yield new Decision(Accion.ALL_IN, fichas);
                    if (fuerza >= 6 || bluff)
                        yield new Decision(Accion.CALL, apuestaMaxima);
                    yield apuestaMaxima == 0 ? new Decision(Accion.CHECK, 0) : new Decision(Accion.FOLD, 0);
                }
                case SHOWDOWN -> new Decision(Accion.CHECK, 0);
            };

            case DIFICIL -> switch (fase) {
                case PRE_FLOP -> {
                    if (parejaAlta || suitedConectadas || pareja)
                        yield new Decision(Accion.RAISE, apuestaMaxima + (int) Math.ceil(bigBlind * agresividad));
                    if (bluff)
                        yield apuestaMaxima == 0 ? new Decision(Accion.CHECK, 0) : new Decision(Accion.CALL, apuestaMaxima);
                    yield apuestaMaxima == 0 ? new Decision(Accion.CHECK, 0) : new Decision(Accion.FOLD, 0);
                }
                case FLOP, TURN -> {
                    if (slowplay) yield new Decision(Accion.CALL, apuestaMaxima);
                    if (fuerza >= 6 || flushDraw || straightDraw || bluff)
                        yield new Decision(Accion.RAISE, apuestaMaxima + (int) Math.ceil(bigBlind * agresividad));
                    if (fuerza >= 3)
                        yield apuestaMaxima == 0 ? new Decision(Accion.CHECK, 0) : new Decision(Accion.CALL, apuestaMaxima);
                    yield apuestaMaxima == 0 ? new Decision(Accion.CHECK, 0) : new Decision(Accion.FOLD, 0);
                }
                case RIVER -> {
                    if (fuerza >= 9 || bluff)
                        yield new Decision(Accion.ALL_IN, fichas);
                    if (fuerza >= 6)
                        yield new Decision(Accion.CALL, apuestaMaxima);
                    yield apuestaMaxima == 0 ? new Decision(Accion.CHECK, 0) : new Decision(Accion.FOLD, 0);
                }
                case SHOWDOWN -> new Decision(Accion.CHECK, 0);
            };
        };
    }


    // -------- Utilidades --------

    private static boolean esPareja(List<String> cartas) {
        return valor(cartas.get(0)).equals(valor(cartas.get(1)));
    }

    private static boolean esParejaAlta(List<String> cartas) {
        String v = valor(cartas.get(0));
        return v.equals(valor(cartas.get(1))) && List.of("J", "Q", "K", "A").contains(v);
    }

    private static boolean esConectadas(List<String> cartas) {
        int v1 = valorNumerico(cartas.get(0));
        int v2 = valorNumerico(cartas.get(1));
        return Math.abs(v1 - v2) == 1;
    }

    private static boolean esSuited(List<String> cartas) {
        return palo(cartas.get(0)) == palo(cartas.get(1));
    }

    private static boolean hayFlushDraw(List<String> cartas, Mesa mesa) {
        Map<Character, Integer> palos = new HashMap<>();
        Stream.concat(cartas.stream(), Stream.of(mesa.getFlop1(), mesa.getFlop2(), mesa.getFlop3(), mesa.getTurn(), mesa.getRiver()))
                .filter(Objects::nonNull)
                .map(c -> c.charAt(c.length() - 1))
                .forEach(p -> palos.put(p, palos.getOrDefault(p, 0) + 1));
        return palos.values().stream().anyMatch(count -> count == 4);
    }

    private static boolean hayStraightDraw(List<String> cartas, Mesa mesa) {
        List<Integer> valores = new ArrayList<>();
        Stream.concat(cartas.stream(), Stream.of(mesa.getFlop1(), mesa.getFlop2(), mesa.getFlop3(), mesa.getTurn(), mesa.getRiver()))
                .filter(Objects::nonNull)
                .map(BotDecisionEngine::valorNumerico)
                .distinct()
                .sorted()
                .forEach(valores::add);

        for (int i = 0; i <= valores.size() - 4; i++) {
            if (valores.get(i + 3) - valores.get(i) <= 4) return true;
        }
        return false;
    }

    private static String valor(String carta) {
        return carta.substring(0, carta.length() - 1);
    }

    private static int valorNumerico(String carta) {
        return switch (valor(carta)) {
            case "A" -> 14;
            case "K" -> 13;
            case "Q" -> 12;
            case "J" -> 11;
            default -> Integer.parseInt(valor(carta));
        };
    }

    private static char palo(String carta) {
        return carta.charAt(carta.length() - 1);
    }
}