package com.pokeronline.logros;

import com.pokeronline.logros.model.CategoriaLogro;
import com.pokeronline.logros.model.Logro;
import com.pokeronline.logros.repository.LogroRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LogroDataLoader {

    private final LogroRepository logroRepository;

    @PostConstruct
    public void init() {
        if (logroRepository.count() > 0) return;

        List<Logro> logros = List.of(
                // === TORNEOS ===
                newLogro("Primer Torneo", "Participar en tu primer torneo", "logros/primer_torneo.png", CategoriaLogro.TORNEOS),
                newLogro("Campeón", "Ganar un torneo", "logros/campeon.png", CategoriaLogro.TORNEOS),
                newLogro("Jugador Constante", "Jugar 10 torneos", "logros/torneos_10.png", CategoriaLogro.TORNEOS),
                newLogro("Jugador Legendario", "Ganar 10 torneos", "logros/torneos_ganados_10.png", CategoriaLogro.TORNEOS),

                // === ESTRATEGIA ===
                newLogro("All-in Master", "Hacer all-in 50 veces", "logros/allin_master.png", CategoriaLogro.ESTRATEGIA),
                newLogro("Superviviente", "Ganar con menos del 5% de fichas", "logros/superviviente.png", CategoriaLogro.ESTRATEGIA),
                newLogro("Ganador con Bluff", "Ganar una mano haciendo bluff", "logros/bluff.png", CategoriaLogro.ESTRATEGIA),
                newLogro("Full House", "Ganar una mano con full house o superior", "logros/fullhouse.png", CategoriaLogro.ESTRATEGIA),

                // === CONTRA BOTS ===
                newLogro("Vencedor Bot", "Ganar a un bot", "logros/bot_trophy.png", CategoriaLogro.CONTRA_BOTS),
                newLogro("Humillar Bot PRO", "Ganar a un bot en modo difícil", "logros/bot_dificil.png", CategoriaLogro.CONTRA_BOTS),

                // === PARTIDAS SIMPLES ===
                newLogro("Primera Partida", "Jugar tu primera partida", "logros/partida_1.png", CategoriaLogro.PARTIDAS_SIMPLES),
                newLogro("Mano Centenaria", "Jugar 100 manos simples", "logros/partidas_100.png", CategoriaLogro.PARTIDAS_SIMPLES),
                newLogro("Victoria Privada", "Ganar tu primera partida en mesa privada", "logros/privada_win.png", CategoriaLogro.PARTIDAS_SIMPLES),

                // === ACCIONES ESPECIALES ===
                newLogro("All-in Victorioso", "Ganar haciendo all-in", "logros/allin_win.png", CategoriaLogro.ACCIONES_ESPECIALES),
                newLogro("Sin fichas", "Quedarte sin fichas", "logros/sin_fichas.png", CategoriaLogro.ACCIONES_ESPECIALES),

                // === EQUIPO ===
                newLogro("Jugador en equipo", "Participar en un torneo siendo parte de un equipo", "logros/equipo_jugador.png", CategoriaLogro.EQUIPO),
                newLogro("Campeón por Equipos", "Ganar un torneo como parte de un equipo", "logros/equipo_campeon.png", CategoriaLogro.EQUIPO)
        );

        logroRepository.saveAll(logros);
    }

    private Logro newLogro(String nombre, String descripcion, String icono, CategoriaLogro categoria) {
        return Logro.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .iconoLogro("files/images/" + icono)
                .categoria(categoria)
                .build();
    }
}