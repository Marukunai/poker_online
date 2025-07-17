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
                newLogro("Jugador Competitivo Constante", "Jugar 10 torneos", "logros/torneos_10.png", CategoriaLogro.TORNEOS),
                newLogro("Jugador Legendario", "Ganar 10 torneos", "logros/torneos_ganados_10.png", CategoriaLogro.TORNEOS),
                newLogro("Amante de los Torneos", "Jugar 25 torneos", "logros/torneos_25.png", CategoriaLogro.TORNEOS),
                newLogro("Top 3", "Quedar entre los 3 primeros en un torneo", "logros/top3.png", CategoriaLogro.TORNEOS),
                newLogro("Finalista", "Llegar a la mesa final de un torneo", "logros/finalista.png", CategoriaLogro.TORNEOS),
                newLogro("Clasificado Pro", "Clasificarse en al menos 5 torneos", "logros/clasificado_5.png", CategoriaLogro.TORNEOS),

                // === ESTRATEGIA ===
                newLogro("All-in", "Hacer all-in una vez", "logros/allin.png", CategoriaLogro.ESTRATEGIA),
                newLogro("All-in Master", "Hacer all-in 10 veces", "logros/allin_master.png", CategoriaLogro.ESTRATEGIA),
                newLogro("Superviviente", "Ganar con menos del 5% de fichas", "logros/superviviente.png", CategoriaLogro.ESTRATEGIA),
                newLogro("Ganador con Bluff", "Ganar una mano haciendo bluff", "logros/bluff.png", CategoriaLogro.ESTRATEGIA),
                newLogro("Bluff Master", "Ganar 10 manos haciendo bluff", "logros/bluff2.png", CategoriaLogro.ESTRATEGIA),
                newLogro("Trips", "Ganar una mano con trío", "logros/trips.png", CategoriaLogro.ESTRATEGIA),
                newLogro("Full House", "Ganar una mano con full house", "logros/fullhouse.png", CategoriaLogro.ESTRATEGIA),
                newLogro("Poker", "Ganar una mano con poker", "logros/poker.png", CategoriaLogro.ESTRATEGIA),
                newLogro("Straight", "Ganar una mano con escalera", "logros/straight.png", CategoriaLogro.ESTRATEGIA),
                newLogro("Flush", "Ganar una mano con flush", "logros/flush.png", CategoriaLogro.ESTRATEGIA),
                newLogro("Royal Flush", "Ganar una mano con Royal Flush", "logros/royalFlush.png", CategoriaLogro.ESTRATEGIA),
                newLogro("Escalera Dobles", "Ganar 2 manos seguidas con escalera", "logros/straight_combo.png", CategoriaLogro.ESTRATEGIA),
                newLogro("Combo Letal", "Ganar 3 manos seguidas con manos fuertes", "logros/combo.png", CategoriaLogro.ESTRATEGIA),

                // === CONTRA BOTS ===
                newLogro("Vencedor Bot", "Ganar a un bot", "logros/bot_trophy.png", CategoriaLogro.CONTRA_BOTS),
                newLogro("El futuro contra mí", "Ganar una partida en una mesa en la que haya 3 o más bots", "logros/bot_trophy2.png", CategoriaLogro.CONTRA_BOTS),
                newLogro("Humillador de IA", "Ganar a un bot en modo difícil", "logros/bot_dificil.png", CategoriaLogro.CONTRA_BOTS),
                newLogro("Derrotador de Máquinas", "Ganar 10 partidas contra bots", "logros/bots_10.png", CategoriaLogro.CONTRA_BOTS),

                // === PARTIDAS SIMPLES ===
                newLogro("Primera Partida", "Jugar tu primera partida", "logros/partida_1.png", CategoriaLogro.PARTIDAS_SIMPLES),
                newLogro("Veterano", "Jugar 10 partidas", "logros/partida_10.png", CategoriaLogro.PARTIDAS_SIMPLES),
                newLogro("Profesional", "Jugar 50 partidas", "logros/partida_50.png", CategoriaLogro.PARTIDAS_SIMPLES),
                newLogro("Mano Centenaria", "Jugar 100 partidas", "logros/partida_100.png", CategoriaLogro.PARTIDAS_SIMPLES),
                newLogro("Victoria Privada", "Ganar tu primera partida en mesa privada", "logros/privada_win.png", CategoriaLogro.PARTIDAS_SIMPLES),
                newLogro("Partida Express", "Ganar una mano en menos de 2 minutos", "logros/express.png", CategoriaLogro.PARTIDAS_SIMPLES),
                newLogro("Jugador Diario", "Jugar partidas 7 días seguidos", "logros/diario.png", CategoriaLogro.PARTIDAS_SIMPLES),

                // === ACCIONES ESPECIALES ===
                newLogro("All-in Victorioso", "Ganar haciendo all-in", "logros/allin_win.png", CategoriaLogro.ACCIONES_ESPECIALES),
                newLogro("Sin fichas", "Quedarte sin fichas", "logros/sin_fichas.png", CategoriaLogro.ACCIONES_ESPECIALES),
                newLogro("Jugador Rico", "Consigue 100K fichas", "logros/100k_fichas.png", CategoriaLogro.ACCIONES_ESPECIALES),
                newLogro("Millonario", "Consigue 1M fichas", "logros/millon_fichas.png", CategoriaLogro.ACCIONES_ESPECIALES),
                newLogro("Milagro", "Ganar una mano con una carta salvadora al final", "logros/milagro.png", CategoriaLogro.ACCIONES_ESPECIALES),
                newLogro("Comeback", "Remontar y ganar con menos del 10% de fichas", "logros/comeback.png", CategoriaLogro.ACCIONES_ESPECIALES),
                newLogro("Subidón", "Ganar 20.000 fichas en una partida", "logros/subidon.png", CategoriaLogro.ACCIONES_ESPECIALES),
                newLogro("Racha de la Suerte", "Ganar 5 manos seguidas", "logros/racha.png", CategoriaLogro.ACCIONES_ESPECIALES),
                newLogro("Perdedor Persistente", "Jugar 20 manos sin ganar ninguna", "logros/persistente.png", CategoriaLogro.ACCIONES_ESPECIALES),

                // === EQUIPO ===
                newLogro("Jugador en equipo", "Participar en un torneo siendo parte de un equipo", "logros/equipo_jugador.png", CategoriaLogro.EQUIPO),
                newLogro("Campeón por Equipos", "Ganar un torneo como parte de un equipo", "logros/equipo_campeon.png", CategoriaLogro.EQUIPO),
                newLogro("Equipo o familia?", "Ganar tres torneos con el mismo equipo", "logros/equipoFamilia.png", CategoriaLogro.EQUIPO),
                newLogro("Arrasador en Equipo", "Ganar tres torneos como parte de un equipo", "logros/arrasadorEquipo.png", CategoriaLogro.EQUIPO),
                newLogro("Capitán Estratégico", "Ser capitán y ganar un torneo", "logros/capitan_estratega.png", CategoriaLogro.EQUIPO),
                newLogro("Todos a una", "Que todo tu equipo llegue a la final", "logros/equipo_finalistas.png", CategoriaLogro.EQUIPO),

                // === LOGROS POR UNIRSE A MESAS PRIVADAS ===
                newLogro("Bienvenido a lo privado", "Unirse por primera vez a una mesa privada", "logros/privado1.png", CategoriaLogro.PRIVADAS),
                newLogro("Jugador VIP", "Unirse a 5 mesas privadas diferentes", "logros/privado5.png", CategoriaLogro.PRIVADAS),
                newLogro("Privacidad al máximo", "Unirse a 10 mesas privadas", "logros/privado10.png", CategoriaLogro.PRIVADAS),

                // === LOGROS POR CREAR MESAS PRIVADAS (opcional si decides usarlos más adelante) ===
                newLogro("Creador de salas", "Crear tu primera mesa privada", "logros/creador1.png", CategoriaLogro.PRIVADAS),
                newLogro("Organizador Pro", "Crear 5 mesas privadas", "logros/creador5.png", CategoriaLogro.PRIVADAS),
                newLogro("Amo de las mesas privadas", "Crear 10 mesas privadas", "logros/creador10.png", CategoriaLogro.PRIVADAS)

                );

        logroRepository.saveAll(logros);
    }

    private Logro newLogro(String nombre, String descripcion, String icono, CategoriaLogro categoria) {
        return Logro.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .iconoLogro("files/images/logros/" + icono)
                .categoria(categoria)
                .build();
    }
}