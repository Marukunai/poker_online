package com.pokeronline.service;

import com.pokeronline.model.Carta;
import com.pokeronline.model.Mesa;
import com.pokeronline.model.UserMesa;
import com.pokeronline.repository.UserMesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BarajaService {

    private final Map<Long, Deque<Carta>> barajasPorMesa = new HashMap<>();
    private final UserMesaRepository userMesaRepository;

    private static final List<String> VALORES = List.of("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A");
    private static final List<String> PALOS = List.of("S", "H", "D", "C"); // Picas, Corazones, Diamantes, Tréboles

    public void repartirCartas(Mesa mesa) {
        inicializarBarajaParaMesa(mesa); // Inicializamos la baraja

        Deque<Carta> baraja = barajasPorMesa.get(mesa.getId());
        List<UserMesa> jugadores = userMesaRepository.findByMesa(mesa);

        for (UserMesa jugador : jugadores) {
            Carta c1 = baraja.poll();
            Carta c2 = baraja.poll();
            assert c1 != null;
            jugador.setCarta1(c1.toString());
            assert c2 != null;
            jugador.setCarta2(c2.toString());
            userMesaRepository.save(jugador);
        }
    }

    public void inicializarBarajaParaMesa(Mesa mesa) {
        List<Carta> baraja = new ArrayList<>();
        for (String valor : VALORES) {
            for (String palo : PALOS) {
                baraja.add(new Carta(valor, palo));
            }
        }
        Collections.shuffle(baraja);
        barajasPorMesa.put(mesa.getId(), new ArrayDeque<>(baraja));
    }

    public String generarCartaAleatoria(Mesa mesa) {
        Deque<Carta> baraja = barajasPorMesa.get(mesa.getId());
        if (baraja == null || baraja.isEmpty()) throw new RuntimeException("Baraja no inicializada o vacía");
        return baraja.poll().toString();
    }
}