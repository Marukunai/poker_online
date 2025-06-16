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

    private final UserMesaRepository userMesaRepository;

    private static final List<String> VALORES = List.of("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A");
    private static final List<String> PALOS = List.of("S", "H", "D", "C"); // Picas, Corazones, Diamantes, Tréboles

    public void repartirCartas(Mesa mesa) {
        List<Carta> baraja = new ArrayList<>();

        for (String valor : VALORES) {
            for (String palo : PALOS) {
                baraja.add(new Carta(valor, palo));
            }
        }

        Collections.shuffle(baraja);

        List<UserMesa> jugadores = userMesaRepository.findByMesa(mesa);
        for (UserMesa jugador : jugadores) {
            Carta c1 = baraja.remove(0);
            Carta c2 = baraja.remove(0);

            jugador.setCarta1(c1.toString());
            jugador.setCarta2(c2.toString());

            userMesaRepository.save(jugador);
        }

        mesa.setFlop1(baraja.remove(0).toString());
        mesa.setFlop2(baraja.remove(0).toString());
        mesa.setFlop3(baraja.remove(0).toString());
        mesa.setTurn(baraja.remove(0).toString());
        mesa.setRiver(baraja.remove(0).toString());
    }
}