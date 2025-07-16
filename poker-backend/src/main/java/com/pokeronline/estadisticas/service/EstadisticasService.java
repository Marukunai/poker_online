package com.pokeronline.estadisticas.service;

import com.pokeronline.estadisticas.dto.*;
import com.pokeronline.model.User;
import com.pokeronline.repository.HistorialManoRepository;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.torneo.model.ParticipanteTorneo;
import com.pokeronline.torneo.repository.ParticipanteTorneoRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EstadisticasService {

    private final ParticipanteTorneoRepository participanteTorneoRepository;
    private final UserRepository userRepository;
    private final HistorialManoRepository historialManoRepository;
    private final EntityManager entityManager;

    public EstadisticasUsuarioDTO obtenerEstadisticasUsuario(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        List<ParticipanteTorneo> participaciones = participanteTorneoRepository.findByUser(user);
        int torneosJugados = participaciones.size();
        int torneosGanados = (int) participaciones.stream().filter(p -> p.getPosicion() == 1).count();
        int puntosTotales = participaciones.stream().mapToInt(ParticipanteTorneo::getPuntos).sum();
        int eliminacionesPrimeraRonda = (int) participaciones.stream()
                .filter(p -> p.getPosicion() > 0 && p.getPosicion() == participaciones.size()).count();
        int mejorPosicion = participaciones.stream().mapToInt(ParticipanteTorneo::getPosicion).min().orElse(0);
        double promedio = participaciones.stream().mapToInt(ParticipanteTorneo::getPosicion).average().orElse(0.0);

        Date ultimo = participaciones.stream().map(p -> p.getTorneo().getFechaInicio()).max(Date::compareTo).orElse(null);

        List<TorneoHistorialDTO> historial = participaciones.stream().map(p -> TorneoHistorialDTO.builder()
                .torneoId(p.getTorneo().getId())
                .nombreTorneo(p.getTorneo().getNombre())
                .posicion(p.getPosicion())
                .fechaInicio(p.getTorneo().getFechaInicio())
                .ganado(p.getPosicion() == 1)
                .puntosObtenidos(p.getPuntos())
                .build()).toList();

        // Partidas simples
        int partidasJugadas = historialManoRepository.countByJugador(user);
        int partidasGanadas = historialManoRepository.countByJugadorAndEmpateFalse(user);
        int fichasGanadas = historialManoRepository.sumFichasGanadasByJugador(user).orElse(0);

        List<ProgresoMensualDTO> progresoTorneos = obtenerProgresoMensualTorneos(user.getId());
        List<ProgresoMensualPartidasDTO> progresoPartidas = obtenerProgresoMensualPartidas(user.getId());

        return EstadisticasUsuarioDTO.builder()
                .userId(userId)
                .torneosJugados(torneosJugados)
                .torneosGanados(torneosGanados)
                .ratioVictoriasTorneo(torneosJugados == 0 ? 0 : (double) torneosGanados / torneosJugados)
                .vecesEliminadoPrimeraRonda(eliminacionesPrimeraRonda)
                .puntosTotales(puntosTotales)
                .fichasGanadas(fichasGanadas)
                .mejorPosicion(mejorPosicion)
                .posicionPromedio(promedio)
                .fechaUltimoTorneo(ultimo)
                .partidasSimplesJugadas(partidasJugadas)
                .partidasSimplesGanadas(partidasGanadas)
                .fichasGanadasSimples(fichasGanadas)
                .historialTorneos(historial)
                .progresoMensual(progresoTorneos)
                .progresoMensualPartidas(progresoPartidas)
                .build();
    }

    public List<ProgresoMensualDTO> obtenerProgresoMensualTorneos(Long userId) {
        String sql = """
            SELECT DATE_FORMAT(t.fecha_inicio, '%Y-%m') AS mes,
                   COUNT(*) AS torneosJugados,
                   SUM(CASE WHEN pt.posicion = 1 THEN 1 ELSE 0 END) AS torneosGanados,
                   SUM(pt.puntos) AS puntosObtenidos
            FROM participante_torneo pt
            JOIN torneo t ON pt.torneo_id = t.id
            WHERE pt.user_id = :userId
            GROUP BY mes
            ORDER BY mes
        """;

        @SuppressWarnings("unchecked")
        List<Object[]> resultados = entityManager.createNativeQuery(sql)
                .setParameter("userId", userId)
                .getResultList();

        List<ProgresoMensualDTO> lista = new ArrayList<>();
        for (Object[] row : resultados) {
            lista.add(ProgresoMensualDTO.builder()
                    .userId(userId)
                    .mes((String) row[0])
                    .torneosJugados(((Number) row[1]).intValue())
                    .torneosGanados(((Number) row[2]).intValue())
                    .puntosObtenidos(((Number) row[3]).intValue())
                    .build());
        }

        return lista;
    }

    public List<ProgresoMensualPartidasDTO> obtenerProgresoMensualPartidas(Long userId) {
        String sql = """
            SELECT DATE_FORMAT(fecha, '%Y-%m') AS mes,
                   COUNT(*) AS partidasJugadas,
                   SUM(CASE WHEN empate = false THEN 1 ELSE 0 END) AS partidasGanadas,
                   SUM(fichas_ganadas) AS fichasGanadas
            FROM historial_mano
            WHERE jugador_id = :userId
            GROUP BY mes
            ORDER BY mes
        """;

        @SuppressWarnings("unchecked")
        List<Object[]> resultados = entityManager.createNativeQuery(sql)
                .setParameter("userId", userId)
                .getResultList();

        List<ProgresoMensualPartidasDTO> lista = new ArrayList<>();
        for (Object[] row : resultados) {
            lista.add(ProgresoMensualPartidasDTO.builder()
                    .userId(userId)
                    .mes((String) row[0])
                    .partidasJugadas(((Number) row[1]).intValue())
                    .partidasGanadas(((Number) row[2]).intValue())
                    .fichasGanadas(((Number) row[3]).intValue())
                    .build());
        }
        return lista;
    }

    public List<RankingUsuarioDTO> obtenerRankingGlobal() {
        return participanteTorneoRepository.obtenerRankingGlobal();
    }

    public List<RankingUsuarioDTO> obtenerRankingMensual(int anio, int mes) {
        return participanteTorneoRepository.obtenerRankingMensual(anio, mes);
    }

}