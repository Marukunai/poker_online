package com.pokeronline.service;

import com.pokeronline.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EvaluadorManoService {

    public ManoEvaluada evaluarMano(User user, List<String> cartasJugador, List<String> comunitarias) {
        List<String> todas = new ArrayList<>();
        todas.addAll(cartasJugador);
        todas.addAll(comunitarias);

        // Generar todas las combinaciones de 5 cartas posibles
        List<List<String>> combinaciones = generarCombinacionesDeCinco(todas);

        ManoEvaluada mejor = null;

        for (List<String> combinacion : combinaciones) {
            ManoEvaluada actual = evaluarCombinacion(user, combinacion);
            if (mejor == null || actual.getFuerza() > mejor.getFuerza()) {
                mejor = actual;
            }
        }

        return mejor;
    }

    private List<List<String>> generarCombinacionesDeCinco(List<String> cartas) {
        List<List<String>> combinaciones = new ArrayList<>();
        combinar(cartas, 0, new ArrayList<>(), combinaciones);
        return combinaciones;
    }

    private void combinar(List<String> entrada, int index, List<String> actual, List<List<String>> resultado) {
        if (actual.size() == 5) {
            resultado.add(new ArrayList<>(actual));
            return;
        }
        for (int i = index; i < entrada.size(); i++) {
            actual.add(entrada.get(i));
            combinar(entrada, i + 1, actual, resultado);
            actual.remove(actual.size() - 1);
        }
    }

    private ManoEvaluada evaluarCombinacion(User user, List<String> cincoCartas) {
        if (esEscaleraReal(cincoCartas))
            return new ManoEvaluada(user, ManoTipo.ESCALERA_REAL, cincoCartas, 10);
        if (esEscaleraColor(cincoCartas))
            return new ManoEvaluada(user, ManoTipo.ESCALERA_COLOR, cincoCartas, 9);
        if (esPoker(cincoCartas))
            return new ManoEvaluada(user, ManoTipo.POKER, cincoCartas, 8);
        if (esFullHouse(cincoCartas))
            return new ManoEvaluada(user, ManoTipo.FULL_HOUSE, cincoCartas, 7);
        if (esColor(cincoCartas))
            return new ManoEvaluada(user, ManoTipo.COLOR, cincoCartas, 6);
        if (esEscalera(cincoCartas))
            return new ManoEvaluada(user, ManoTipo.ESCALERA, cincoCartas, 5);
        if (esTrio(cincoCartas))
            return new ManoEvaluada(user, ManoTipo.TRIO, cincoCartas, 4);
        if (esDoblePareja(cincoCartas))
            return new ManoEvaluada(user, ManoTipo.DOBLE_PAREJA, cincoCartas, 3);
        if (esPareja(cincoCartas))
            return new ManoEvaluada(user, ManoTipo.PAREJA, cincoCartas, 2);

        // Caso por defecto: carta alta
        return new ManoEvaluada(user, ManoTipo.CARTA_ALTA, cincoCartas, 1);
    }

    public List<ManoEvaluada> repartirBote(List<UserMesa> jugadores, Mesa mesa) {
        List<String> comunitarias = List.of(
                mesa.getFlop1(), mesa.getFlop2(), mesa.getFlop3(),
                mesa.getTurn(), mesa.getRiver()
        );

        // Evaluar todas las manos
        List<ManoEvaluada> evaluaciones = new ArrayList<>(jugadores.stream()
                .filter(UserMesa::isEnJuego)
                .map(j -> evaluarMano(j.getUser(), List.of(j.getCarta1(), j.getCarta2()), comunitarias))
                .toList());

        // Ordenar por fuerza y luego por desempate
        evaluaciones.sort((a, b) -> {
            if (b.getFuerza() != a.getFuerza()) {
                return Integer.compare(b.getFuerza(), a.getFuerza());
            }
            return desempatarManos(b.getCartasGanadoras(), a.getCartasGanadoras());
        });

        ManoEvaluada mejorMano = evaluaciones.get(0);

        // Filtrar ganadores exactos
        List<ManoEvaluada> ganadores = evaluaciones.stream()
                .filter(m -> m.getFuerza() == mejorMano.getFuerza() && desempatarManos(m.getCartasGanadoras(), mejorMano.getCartasGanadoras()) == 0)
                .toList();

        int potTotal = mesa.getPot();
        int premioPorJugador = potTotal / ganadores.size();

        for (ManoEvaluada ganador : ganadores) {
            UserMesa userMesa = jugadores.stream()
                    .filter(j -> j.getUser().getId().equals(ganador.getUser().getId()))
                    .findFirst()
                    .orElseThrow();

            userMesa.setFichasEnMesa(userMesa.getFichasEnMesa() + premioPorJugador);
        }

        mesa.setPot(0); // Reset pot

        return ganadores;
    }

    private int desempatarManos(List<String> a, List<String> b) {
        List<Integer> valoresA = a.stream()
                .map(this::valorNumerico)
                .sorted(Comparator.reverseOrder())
                .toList();

        List<Integer> valoresB = b.stream()
                .map(this::valorNumerico)
                .sorted(Comparator.reverseOrder())
                .toList();

        for (int i = 0; i < Math.min(valoresA.size(), valoresB.size()); i++) {
            int cmp = Integer.compare(valoresA.get(i), valoresB.get(i));
            if (cmp != 0) return cmp;
        }

        return 0; // empate exacto
    }

    private int valorNumerico(String carta) {
        String valor = carta.substring(0, carta.length() - 1);
        return switch (valor) {
            case "A" -> 14;
            case "K" -> 13;
            case "Q" -> 12;
            case "J" -> 11;
            default -> Integer.parseInt(valor);
        };
    }

    // Valores a las figuras
    private int valorANumero(String valor) {
        return switch (valor) {
            case "A" -> 14;
            case "K" -> 13;
            case "Q" -> 12;
            case "J" -> 11;
            default -> Integer.parseInt(valor);
        };
    }

    // auxiliar para comprobar que tiene escalera
    private boolean tieneEscalera(List<Integer> valores) {
        for (int i = 0; i <= valores.size() - 5; i++) {
            boolean escalera = true;
            for (int j = 0; j < 4; j++) {
                if (valores.get(i + j) + 1 != valores.get(i + j + 1)) {
                    escalera = false;
                    break;
                }
            }
            if (escalera) return true;
        }
        // Escalera con A como 1
        return new HashSet<>(valores).containsAll(List.of(2, 3, 4, 5, 14));
    }

    private boolean esEscaleraReal(List<String> cartas) {
        Set<String> palos = Set.of("S", "H", "D", "C");

        for (String palo : palos) {
            List<String> escalera = List.of(
                    "10" + palo,
                    "J" + palo,
                    "Q" + palo,
                    "K" + palo,
                    "A" + palo
            );
            if (new HashSet<>(cartas).containsAll(escalera)) {
                return true;
            }
        }
        return false;
    }

    private boolean esEscaleraColor(List<String> cartas) {
        Map<String, List<Integer>> porPalo = new HashMap<>();

        for (String carta : cartas) {
            String valor = carta.substring(0, carta.length() - 1);
            String palo = carta.substring(carta.length() - 1);

            int num = valorANumero(valor);
            porPalo.computeIfAbsent(palo, k -> new ArrayList<>()).add(num);
        }

        for (List<Integer> valores : porPalo.values()) {
            if (valores.size() < 5) continue;

            Set<Integer> unicos = new HashSet<>(valores);
            List<Integer> ordenados = new ArrayList<>(unicos);
            Collections.sort(ordenados);

            if (tieneEscalera(ordenados)) return true;
        }

        return false;
    }

    private boolean esPoker(List<String> cartas) {
        Map<String, Integer> contador = new HashMap<>();

        for (String carta : cartas) {
            String valor = carta.substring(0, carta.length() - 1);
            contador.put(valor, contador.getOrDefault(valor, 0) + 1);
        }

        return contador.values().stream().anyMatch(c -> c == 4);
    }

    private boolean esFullHouse(List<String> cartas) {
        Map<String, Integer> contador = new HashMap<>();

        for (String carta : cartas) {
            String valor = carta.substring(0, carta.length() - 1);
            contador.put(valor, contador.getOrDefault(valor, 0) + 1);
        }

        boolean tieneTrio = contador.values().stream().anyMatch(c -> c >= 3);
        boolean tienePareja = contador.values().stream()
                .filter(c -> c >= 2)
                .count() >= 2; // puede haber un trío y una pareja distinta, o dos tríos

        return tieneTrio && tienePareja;
    }

    private boolean esColor(List<String> cartas) {
        Map<String, List<String>> porPalo = new HashMap<>();

        for (String carta : cartas) {
            String palo = carta.substring(carta.length() - 1);
            porPalo.computeIfAbsent(palo, k -> new ArrayList<>()).add(carta);
        }

        for (List<String> lista : porPalo.values()) {
            if (lista.size() >= 5) {
                return true;
            }
        }

        return false;
    }

    private boolean esEscalera(List<String> cartas) {
        Set<Integer> valores = new HashSet<>();

        for (String carta : cartas) {
            String valor = carta.substring(0, carta.length() - 1);
            valores.add(valorANumero(valor));
        }

        List<Integer> ordenados = new ArrayList<>(valores);
        Collections.sort(ordenados);

        return tieneEscalera(ordenados);
    }

    private boolean esTrio(List<String> cartas) {
        Map<String, Integer> contador = new HashMap<>();

        for (String carta : cartas) {
            String valor = carta.substring(0, carta.length() - 1);
            contador.put(valor, contador.getOrDefault(valor, 0) + 1);
        }

        return contador.values().stream().anyMatch(c -> c == 3);
    }

    private boolean esDoblePareja(List<String> cartas) {
        Map<String, Integer> contador = new HashMap<>();

        for (String carta : cartas) {
            String valor = carta.substring(0, carta.length() - 1);
            contador.put(valor, contador.getOrDefault(valor, 0) + 1);
        }

        long parejas = contador.values().stream().filter(c -> c >= 2).count();

        return parejas >= 2;
    }

    private boolean esPareja(List<String> cartas) {
        Map<String, Integer> contador = new HashMap<>();

        for (String carta : cartas) {
            String valor = carta.substring(0, carta.length() - 1);
            contador.put(valor, contador.getOrDefault(valor, 0) + 1);
        }

        return contador.values().stream().anyMatch(c -> c == 2);
    }
}