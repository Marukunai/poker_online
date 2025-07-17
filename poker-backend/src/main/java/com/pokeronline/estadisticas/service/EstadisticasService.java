package com.pokeronline.estadisticas.service;

import com.pokeronline.estadisticas.dto.*;
import com.pokeronline.model.User;
import com.pokeronline.repository.HistorialManoRepository;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.torneo.equipos.repository.MiembroEquipoTorneoRepository;
import com.pokeronline.torneo.model.ParticipanteTorneo;
import com.pokeronline.torneo.repository.ParticipanteTorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstadisticasService {

    private final ParticipanteTorneoRepository participanteTorneoRepository;
    private final UserRepository userRepository;
    private final HistorialManoRepository historialManoRepository;
    private final MiembroEquipoTorneoRepository miembroEquipoTorneoRepository;

    public EstadisticasUsuarioDTO obtenerEstadisticasUsuario(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        List<ParticipanteTorneo> participaciones = participanteTorneoRepository.findByUser(user);

        // Totales
        int torneosJugados = participaciones.size();
        int torneosGanados = (int) participaciones.stream().filter(p -> p.getPosicion() == 1).count();
        int puntosTotales = participaciones.stream().mapToInt(ParticipanteTorneo::getPuntos).sum();
        int eliminacionesPrimeraRonda = (int) participaciones.stream()
                .filter(p -> p.getPosicion() > 0 && p.getPosicion() == participaciones.size()).count();
        int mejorPosicion = participaciones.stream().mapToInt(ParticipanteTorneo::getPosicion).min().orElse(0);
        double promedio = participaciones.stream().mapToInt(ParticipanteTorneo::getPosicion).average().orElse(0.0);
        Date ultimo = participaciones.stream().map(p -> p.getTorneo().getFechaInicio()).max(Date::compareTo).orElse(null);

        // Por equipos
        int torneosJugadosEnEquipo = (int) participaciones.stream()
                .filter(p -> miembroEquipoTorneoRepository.existsByUserAndEquipo_Torneo(user, p.getTorneo())).count();
        int torneosJugadosIndividuales = torneosJugados - torneosJugadosEnEquipo;

        List<TorneoHistorialDTO> historial = participaciones.stream().map(p -> {
            boolean enEquipo = p.getEquipo() != null;
            String nombreEquipo = enEquipo ? p.getEquipo().getNombre() : null;

            return TorneoHistorialDTO.builder()
                    .torneoId(p.getTorneo().getId())
                    .nombreTorneo(p.getTorneo().getNombre())
                    .fechaInicio(p.getTorneo().getFechaInicio())
                    .posicion(p.getPosicion())
                    .ganado(p.getPosicion() == 1)
                    .puntosObtenidos(p.getPuntos())
                    .enEquipo(enEquipo)
                    .nombreEquipo(nombreEquipo)
                    .build();
        }).toList();

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
                .torneosJugadosEquipo(torneosJugadosEnEquipo)
                .torneosJugadosIndividual(torneosJugadosIndividuales)
                .build();
    }

    public List<ProgresoMensualDTO> obtenerProgresoMensualTorneos(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<ParticipanteTorneo> participaciones = participanteTorneoRepository.findByUser(user);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");

        return participaciones.stream()
                .collect(Collectors.groupingBy(p -> sdf.format(p.getTorneo().getFechaInicio())))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<ParticipanteTorneo> lista = entry.getValue();
                    int jugados = lista.size();
                    int ganados = (int) lista.stream().filter(p -> p.getPosicion() == 1).count();
                    int puntos = lista.stream().mapToInt(ParticipanteTorneo::getPuntos).sum();

                    return ProgresoMensualDTO.builder()
                            .userId(userId)
                            .mes(entry.getKey())
                            .torneosJugados(jugados)
                            .torneosGanados(ganados)
                            .puntosObtenidos(puntos)
                            .build();
                }).toList();
    }

    public List<ProgresoMensualPartidasDTO> obtenerProgresoMensualPartidas(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        var historial = historialManoRepository.findByJugador(user);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");

        return historial.stream()
                .collect(Collectors.groupingBy(m -> sdf.format(m.getFecha())))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    var lista = entry.getValue();
                    int jugadas = lista.size();
                    int ganadas = (int) lista.stream().filter(m -> !m.isEmpate()).count();
                    int fichas = lista.stream().mapToInt(m -> Optional.of(m.getFichasGanadas()).orElse(0)).sum();

                    return ProgresoMensualPartidasDTO.builder()
                            .userId(userId)
                            .mes(entry.getKey())
                            .partidasJugadas(jugadas)
                            .partidasGanadas(ganadas)
                            .fichasGanadas(fichas)
                            .build();
                }).toList();
    }

    public List<RankingUsuarioDTO> obtenerRankingGlobal() {
        List<ParticipanteTorneo> participaciones = participanteTorneoRepository.findAll();
        return agruparRankingDesdeParticipaciones(participaciones);
    }

    public List<RankingUsuarioDTO> obtenerRankingMensual(int anio, int mes) {
        Calendar calInicio = getCalendar(anio, mes);

        Calendar calFin = (Calendar) calInicio.clone();
        calFin.set(Calendar.DAY_OF_MONTH, calInicio.getActualMaximum(Calendar.DAY_OF_MONTH));
        calFin.set(Calendar.HOUR_OF_DAY, 23);
        calFin.set(Calendar.MINUTE, 59);
        calFin.set(Calendar.SECOND, 59);
        calFin.set(Calendar.MILLISECOND, 999);

        List<ParticipanteTorneo> participaciones = participanteTorneoRepository
                .findByTorneo_FechaInicioBetween(calInicio.getTime(), calFin.getTime());

        return agruparRankingDesdeParticipaciones(participaciones);
    }

    private static Calendar getCalendar(int anio, int mes) {
        Calendar calInicio = Calendar.getInstance();
        int[] meses = {
                Calendar.JANUARY, Calendar.FEBRUARY, Calendar.MARCH,
                Calendar.APRIL, Calendar.MAY, Calendar.JUNE,
                Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER,
                Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.DECEMBER
        };
        //noinspection MagicConstant
        calInicio.set(anio, meses[mes - 1], 1, 0, 0, 0);
        calInicio.set(Calendar.MILLISECOND, 0);
        return calInicio;
    }

    private List<RankingUsuarioDTO> agruparRankingDesdeParticipaciones(List<ParticipanteTorneo> participaciones) {
        Map<Long, List<ParticipanteTorneo>> agrupado = participaciones.stream()
                .collect(Collectors.groupingBy(p -> p.getUser().getId()));

        List<RankingUsuarioDTO> resultado = new ArrayList<>();

        for (Map.Entry<Long, List<ParticipanteTorneo>> entry : agrupado.entrySet()) {
            Long userId = entry.getKey();
            List<ParticipanteTorneo> lista = entry.getValue();
            String username = lista.get(0).getUser().getUsername();
            int puntosTotales = lista.stream().mapToInt(ParticipanteTorneo::getPuntos).sum();
            int torneosGanados = (int) lista.stream().filter(p -> p.getPosicion() == 1).count();
            int torneosJugados = lista.size();

            resultado.add(RankingUsuarioDTO.builder()
                    .userId(userId)
                    .username(username)
                    .puntosTotales(puntosTotales)
                    .torneosGanados(torneosGanados)
                    .torneosJugados(torneosJugados)
                    .build());
        }

        resultado.sort(Comparator.comparingInt(RankingUsuarioDTO::getPuntosTotales).reversed());
        return resultado;
    }
}