package com.pokeronline.torneo.equipos.service;

import com.pokeronline.logros.service.LogroService;
import com.pokeronline.model.User;
import com.pokeronline.model.UserMesa;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.service.UserService;
import com.pokeronline.torneo.equipos.dto.EquipoEstadisticasDTO;
import com.pokeronline.torneo.equipos.dto.HistorialEquipoDTO;
import com.pokeronline.torneo.equipos.dto.RankingEquipoDTO;
import com.pokeronline.torneo.equipos.dto.UpdateCapitanDTO;
import com.pokeronline.torneo.equipos.model.MiembroEquipoTorneo;
import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.equipos.model.EquipoTorneo;
import com.pokeronline.torneo.equipos.repository.EquipoTorneoRepository;
import com.pokeronline.torneo.equipos.repository.MiembroEquipoTorneoRepository;
import com.pokeronline.torneo.model.TorneoMesa;
import com.pokeronline.torneo.repository.TorneoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Calendar;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipoTorneoService {

    private final LogroService logroService;
    private final MiembroEquipoTorneoRepository miembroEquipoTorneoRepository;
    private final EquipoTorneoRepository equipoTorneoRepository;
    private final TorneoRepository torneoRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public EquipoTorneo crearEquipo(Torneo torneo, String nombreEquipo, User capitan) {
        EquipoTorneo equipo = EquipoTorneo.builder()
                .torneo(torneo)
                .nombre(nombreEquipo)
                .capitan(capitan)
                .puntosTotales(0)
                .build();

        equipo = equipoTorneoRepository.save(equipo);

        // Crear el registro de miembro para el capitán con esCapitan = true
        if (!miembroEquipoTorneoRepository.existsByEquipoAndUser(equipo, capitan)) {
            MiembroEquipoTorneo miembroCapitan = MiembroEquipoTorneo.builder()
                    .equipo(equipo)
                    .user(capitan)
                    .esCapitan(true)   // <<< IMPORTANTE
                    .build();
            miembroEquipoTorneoRepository.save(miembroCapitan);
        } else {
            // Si ya existiese como miembro por cualquier motivo, asegúrate del flag
            miembroEquipoTorneoRepository.findByEquipoAndUser(equipo, capitan).ifPresent(m -> {
                if (!m.isEsCapitan()) {
                    m.setEsCapitan(true);
                    miembroEquipoTorneoRepository.save(m);
                }
            });
        }

        return equipo;
    }

    public List<EquipoTorneo> listarEquiposDeTorneo(Long torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId).orElseThrow();
        return equipoTorneoRepository.findByTorneo(torneo);
    }

    public EquipoTorneo obtenerEquipo(Long equipoId) {
        return equipoTorneoRepository.findById(equipoId).orElseThrow();
    }

    public EquipoTorneo actualizarPuntos(Long equipoId, int puntosSumar) {
        EquipoTorneo equipo = obtenerEquipo(equipoId);
        equipo.setPuntosTotales(equipo.getPuntosTotales() + puntosSumar);
        return equipoTorneoRepository.save(equipo);
    }

    public void eliminarEquipo(Long equipoId) {
        equipoTorneoRepository.deleteById(equipoId);
    }

    @Transactional
    public void actualizarCapitan(UpdateCapitanDTO dto, Long userIdSolicitante) {
        EquipoTorneo equipo = obtenerEquipo(dto.getEquipoId());

        boolean esCapitan = equipo.getCapitan().getId().equals(userIdSolicitante);
        boolean esAdmin = userService.isAdmin(userIdSolicitante);
        if (!esCapitan && !esAdmin) {
            throw new AccessDeniedException("No tienes permisos para cambiar el capitán");
        }

        User nuevoCapitan = userRepository.findById(dto.getNuevoCapitanId())
                .orElseThrow(() -> new RuntimeException("Usuario nuevo capitán no encontrado"));

        // Debe ser miembro
        MiembroEquipoTorneo miembroNuevo = miembroEquipoTorneoRepository
                .findByEquipo_IdAndUser_Id(equipo.getId(), nuevoCapitan.getId())
                .orElseThrow(() -> new IllegalArgumentException("El nuevo capitán debe ser miembro del equipo"));

        // Desmarcar capitán actual (si lo hay)
        miembroEquipoTorneoRepository.findByEquipo_IdAndEsCapitanTrue(equipo.getId())
                .ifPresent(actual -> {
                    actual.setEsCapitan(false);
                    miembroEquipoTorneoRepository.save(actual);
                });

        // Marcar nuevo capitán
        miembroNuevo.setEsCapitan(true);
        miembroEquipoTorneoRepository.save(miembroNuevo);

        // Sincronizar campo en Equipo
        equipo.setCapitan(nuevoCapitan);
        equipoTorneoRepository.save(equipo);
    }

    public List<RankingEquipoDTO> obtenerRankingEquipos(Long torneoId) {
        List<EquipoTorneo> equipos = equipoTorneoRepository.findByTorneo_IdOrderByPuntosTotalesDesc(torneoId);

        int[] posicion = {1}; // contador mutable
        return equipos.stream()
                .map(equipo -> RankingEquipoDTO.builder()
                        .posicion(posicion[0]++)
                        .nombreEquipo(equipo.getNombre())
                        .nombreCapitan(equipo.getCapitan().getUsername())
                        .puntosTotales(equipo.getPuntosTotales())
                        .build())
                .toList();
    }

    public List<RankingEquipoDTO> obtenerRankingGlobal() {
        return mapearRanking(equipoTorneoRepository.findAllByOrderByPuntosTotalesDesc());
    }

    public List<RankingEquipoDTO> obtenerRankingAnual(int year) {
        Calendar calInicio = Calendar.getInstance();
        calInicio.set(year, Calendar.JANUARY, 1, 0, 0, 0);
        calInicio.set(Calendar.MILLISECOND, 0);

        Calendar calFin = Calendar.getInstance();
        calFin.set(year, Calendar.DECEMBER, 31, 23, 59, 59);
        calFin.set(Calendar.MILLISECOND, 999);

        Date inicio = calInicio.getTime();
        Date fin = calFin.getTime();

        return mapearRanking(equipoTorneoRepository
                .findByTorneo_FechaInicioBetweenOrderByPuntosTotalesDesc(inicio, fin));
    }

    public List<RankingEquipoDTO> obtenerRankingMensual(int year, int mes) {
        Calendar calInicio = Calendar.getInstance();
        calInicio.set(year, mes - 1, 1, 0, 0, 0);
        calInicio.set(Calendar.MILLISECOND, 0);

        Calendar calFin = (Calendar) calInicio.clone();
        int lastDay = calInicio.getActualMaximum(Calendar.DAY_OF_MONTH);
        calFin.set(Calendar.DAY_OF_MONTH, lastDay);
        calFin.set(Calendar.HOUR_OF_DAY, 23);
        calFin.set(Calendar.MINUTE, 59);
        calFin.set(Calendar.SECOND, 59);
        calFin.set(Calendar.MILLISECOND, 999);

        Date inicio = calInicio.getTime();
        Date fin = calFin.getTime();

        return mapearRanking(equipoTorneoRepository
                .findByTorneo_FechaInicioBetweenOrderByPuntosTotalesDesc(inicio, fin));
    }

    private List<RankingEquipoDTO> mapearRanking(List<EquipoTorneo> equipos) {
        int[] posicion = {1};
        return equipos.stream()
                .map(e -> RankingEquipoDTO.builder()
                        .posicion(posicion[0]++)
                        .nombreEquipo(e.getNombre())
                        .nombreCapitan(e.getCapitan().getUsername())
                        .puntosTotales(e.getPuntosTotales())
                        .build())
                .toList();
    }

    public EquipoEstadisticasDTO obtenerEstadisticasEquipo(Long equipoId) {
        EquipoTorneo equipo = obtenerEquipo(equipoId);

        List<EquipoTorneo> historial = equipoTorneoRepository.findByNombreAndTorneoIsNotNullOrderByTorneo_FechaInicioDesc(
                equipo.getNombre()
        );

        if (historial.isEmpty()) {
            return EquipoEstadisticasDTO.builder()
                    .equipoId(equipo.getId())
                    .nombreEquipo(equipo.getNombre())
                    .torneosJugados(0)
                    .torneosGanados(0)
                    .posicionPromedio(0)
                    .mejorPosicion(0)
                    .puntosTotales(equipo.getPuntosTotales())
                    .fechaUltimoTorneo(null)
                    .build();
        }

        int torneosJugados = historial.size();
        int torneosGanados = (int) historial.stream()
                .filter(e -> e.getPuntosTotales() >= historial.stream()
                        .filter(eq -> eq.getTorneo().equals(e.getTorneo()))
                        .mapToInt(EquipoTorneo::getPuntosTotales)
                        .max().orElse(0))
                .count();

        // Simulamos posición si no se guarda directamente: mayor puntaje → posición más alta
        Map<Long, List<EquipoTorneo>> porTorneo = historial.stream()
                .collect(Collectors.groupingBy(e -> e.getTorneo().getId()));

        List<Integer> posiciones = porTorneo.values().stream()
                .map(lista -> {
                    lista.sort(Comparator.comparingInt(EquipoTorneo::getPuntosTotales).reversed());
                    for (int i = 0; i < lista.size(); i++) {
                        if (lista.get(i).getNombre().equals(equipo.getNombre())) {
                            return i + 1;
                        }
                    }
                    return lista.size(); // debería nunca pasar
                })
                .toList();

        double promedio = posiciones.stream().mapToInt(Integer::intValue).average().orElse(0);
        int mejorPosicion = posiciones.stream().mapToInt(Integer::intValue).min().orElse(0);
        Date fechaUltimo = historial.get(0).getTorneo().getFechaInicio();

        return EquipoEstadisticasDTO.builder()
                .equipoId(equipo.getId())
                .nombreEquipo(equipo.getNombre())
                .torneosJugados(torneosJugados)
                .torneosGanados(torneosGanados)
                .posicionPromedio(promedio)
                .mejorPosicion(mejorPosicion)
                .puntosTotales(equipo.getPuntosTotales())
                .fechaUltimoTorneo(fechaUltimo)
                .build();
    }

    public List<HistorialEquipoDTO> obtenerHistorialEquipo(Long equipoId) {
        EquipoTorneo equipo = obtenerEquipo(equipoId);

        // Buscar todos los torneos donde haya un equipo con ese nombre
        List<EquipoTorneo> historial = equipoTorneoRepository.findByNombreAndTorneoIsNotNullOrderByTorneo_FechaInicioDesc(
                equipo.getNombre()
        );

        // Agrupamos por torneo para calcular posición relativa
        Map<Long, List<EquipoTorneo>> porTorneo = historial.stream()
                .collect(Collectors.groupingBy(e -> e.getTorneo().getId()));

        List<HistorialEquipoDTO> historialDTOs = new ArrayList<>();

        for (EquipoTorneo e : historial) {
            List<EquipoTorneo> participantes = porTorneo.get(e.getTorneo().getId());
            participantes.sort(Comparator.comparingInt(EquipoTorneo::getPuntosTotales).reversed());

            int posicion = 1;
            for (EquipoTorneo participante : participantes) {
                if (participante.getNombre().equals(equipo.getNombre())) break;
                posicion++;
            }

            historialDTOs.add(HistorialEquipoDTO.builder()
                    .torneoId(e.getTorneo().getId())
                    .nombreTorneo(e.getTorneo().getNombre())
                    .fechaInicio(e.getTorneo().getFechaInicio())
                    .posicion(posicion)
                    .puntosObtenidos(e.getPuntosTotales())
                    .ganado(posicion == 1)
                    .build());
        }

        return historialDTOs;
    }

    public void asignarLogrosEquipoGanador(Torneo torneo) {
        // 1. Obtener equipo ganador
        List<EquipoTorneo> equipos = equipoTorneoRepository.findByTorneo(torneo);
        EquipoTorneo ganador = equipos.stream()
                .max(Comparator.comparingInt(EquipoTorneo::getPuntosTotales))
                .orElse(null);

        if (ganador == null) {
            log.warn("No se encontró equipo ganador para el torneo '{}'", torneo.getNombre());
            return;
        }

        List<MiembroEquipoTorneo> miembros = miembroEquipoTorneoRepository.findByEquipo(ganador);

        // 2. 🏆 Logro: Campeón por Equipos
        for (MiembroEquipoTorneo miembro : miembros) {
            logroService.otorgarLogroSiNoTiene(miembro.getUser().getId(), "Campeón por Equipos");
        }

        // 3. 👨‍👩‍👧‍👦 Logro: Equipo o familia?
        long torneosGanadosConEseNombre = equipoTorneoRepository
                .findByNombreAndTorneoIsNotNullOrderByTorneo_FechaInicioDesc(ganador.getNombre()).stream()
                .filter(e -> {
                    List<EquipoTorneo> todos = equipoTorneoRepository.findByTorneo(e.getTorneo());
                    int max = todos.stream().mapToInt(EquipoTorneo::getPuntosTotales).max().orElse(0);
                    return e.getPuntosTotales() >= max;
                })
                .count();

        if (torneosGanadosConEseNombre >= 3) {
            for (MiembroEquipoTorneo miembro : miembros) {
                logroService.otorgarLogroSiNoTiene(miembro.getUser().getId(), "Equipo o familia?");
            }
        }

        // 4. 🌪 Logro: Arrasador en Equipo
        for (MiembroEquipoTorneo miembro : miembros) {
            long ganados = equipoTorneoRepository.findByMiembro(miembro.getUser()).stream()
                    .filter(e -> {
                        List<EquipoTorneo> todos = equipoTorneoRepository.findByTorneo(e.getTorneo());
                        int max = todos.stream().mapToInt(EquipoTorneo::getPuntosTotales).max().orElse(0);
                        return e.getPuntosTotales() >= max;
                    })
                    .count();

            if (ganados >= 3) {
                logroService.otorgarLogroSiNoTiene(miembro.getUser().getId(), "Arrasador en Equipo");
            }
        }

        // 5. 🧠 Logro: Capitán Estratégico
        User capitan = ganador.getCapitan();
        logroService.otorgarLogroSiNoTiene(capitan.getId(), "Capitán Estratégico");

        // 6. 🤝 Logro: Todos a una
        int rondaFinal = torneo.getMesas().stream()
                .mapToInt(TorneoMesa::getRonda)
                .max()
                .orElse(1);

        List<Long> idsFinalistas = torneo.getMesas().stream()
                .filter(m -> m.getRonda() == rondaFinal)
                .flatMap(m -> m.getMesa().getJugadores().stream())
                .map(UserMesa::getUser)
                .map(User::getId)
                .toList();

        boolean todosEstan = miembros.stream()
                .map(m -> m.getUser().getId())
                .allMatch(idsFinalistas::contains);

        if (todosEstan) {
            for (MiembroEquipoTorneo miembro : miembros) {
                logroService.otorgarLogroSiNoTiene(miembro.getUser().getId(), "Todos a una");
            }
        }

        log.info("Logros de equipo asignados al ganador '{}'", ganador.getNombre());
    }
}