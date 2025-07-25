package com.pokeronline.torneo.scheduler;

import com.pokeronline.model.Mesa;
import com.pokeronline.torneo.model.*;
import com.pokeronline.torneo.repository.EsperaTorneoRepository;
import com.pokeronline.torneo.repository.TorneoMesaRepository;
import com.pokeronline.torneo.service.ParticipanteTorneoService;
import com.pokeronline.torneo.service.TorneoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class TorneoScheduler {

    private final EsperaTorneoRepository esperaTorneoRepository;
    private final TorneoService torneoService;
    private final ParticipanteTorneoService participanteService;
    private final TorneoMesaRepository torneoMesaRepository;

    @Scheduled(fixedRate = 60000) // Cada minuto
    @Transactional
    public void gestionarTorneos() {
        revisarTorneosPendientes();
        revisarTorneosEnCurso();
        gestionarBlinds();
    }

    private void revisarTorneosPendientes() {
        List<Torneo> pendientes = torneoService.listarTorneosPendientes();
        Date ahora = new Date();

        for (Torneo torneo : pendientes) {
            if (torneo.getFechaInicio().before(ahora)) {

                // Descalificar ausentes
                List<ParticipanteTorneo> participantes = participanteService.obtenerParticipantes(torneo);
                List<Long> idsPresentes = esperaTorneoRepository.findByTorneo(torneo).stream()
                        .map(e -> e.getUser().getId())
                        .toList();

                for (ParticipanteTorneo p : participantes) {
                    if (!idsPresentes.contains(p.getUser().getId())) {
                        participanteService.eliminarParticipante(p);
                        log.warn("Usuario '{}' fue descalificado del torneo '{}' por no presentarse.",
                                p.getUser().getUsername(), torneo.getNombre());
                    }
                }

                // Verificar si se puede iniciar
                long presentes = esperaTorneoRepository.countByTorneo_Id(torneo.getId());
                if (presentes < torneo.getMinParticipantes()) {
                    log.warn("Torneo '{}' no puede iniciar: solo hay {} jugadores presentes (mínimo requerido: {})",
                            torneo.getNombre(), presentes, torneo.getMinParticipantes());
                    continue;
                }

                iniciarTorneo(torneo);
            }
        }
    }

    private void iniciarTorneo(Torneo torneo) {
        List<ParticipanteTorneo> participantes = participanteService.obtenerParticipantes(torneo);
        torneo.setEstado(TorneoEstado.EN_CURSO);
        torneoService.guardarTorneo(torneo);

        int mesaSize = 6;
        int totalMesas = (int) Math.ceil(participantes.size() / (double) mesaSize);

        List<TorneoMesa> mesas = new ArrayList<>();
        for (int i = 0; i < totalMesas; i++) {
            TorneoMesa tm = TorneoMesa.builder().torneo(torneo).ronda(1).build();
            mesas.add(torneoMesaRepository.save(tm));
        }

        for (int i = 0; i < participantes.size(); i++) {
            ParticipanteTorneo p = participantes.get(i);
            TorneoMesa mesa = mesas.get(i % mesas.size());
            p.setMesa(mesa.getMesa());
            participanteService.guardarParticipante(p);
        }

        log.info("Torneo '{}' iniciado con {} participantes distribuidos en {} mesas.",
                torneo.getNombre(), participantes.size(), mesas.size());
    }

    public void revisarTorneosEnCurso() {
        List<Torneo> torneosEnCurso = torneoService.listarTorneosEnCurso();
        Date ahora = new Date();

        for (Torneo torneo : torneosEnCurso) {
            List<ParticipanteTorneo> activos = participanteService.obtenerParticipantes(torneo)
                    .stream()
                    .filter(p -> !p.isEliminado())
                    .toList();

            if (torneo.getFechaFin() != null && torneo.getFechaFin().before(ahora)) {
                torneoService.finalizarTorneo(torneo);
                log.info("Torneo '{}' finalizado automáticamente por fechaFin.", torneo.getNombre());
                continue;
            }

            revisarMesasFinalizadas(torneo, activos);

            if (torneo.isEliminacionDirecta()) {
                if (activos.size() <= 1) {
                    torneoService.finalizarTorneo(torneo);
                    log.info("Torneo '{}' finalizado automáticamente (eliminación directa).", torneo.getNombre());
                }
            } else {
                if (activos.isEmpty()) {
                    torneoService.finalizarTorneo(torneo);
                    log.info("Torneo '{}' finalizado automáticamente (ranking sin jugadores activos).", torneo.getNombre());
                }
            }
        }
    }

    private void revisarMesasFinalizadas(Torneo torneo, List<ParticipanteTorneo> activos) {
        Set<Mesa> mesasActivas = new HashSet<>();
        for (ParticipanteTorneo p : activos) {
            if (p.getMesa() != null) {
                mesasActivas.add(p.getMesa());
            }
        }

        for (Mesa mesa : mesasActivas) {
            long enMesa = activos.stream().filter(p -> mesa.equals(p.getMesa())).count();
            if (enMesa == 1) {
                avanzarParticipanteDeMesa(torneo, mesa);
            }
        }
    }

    private void avanzarParticipanteDeMesa(Torneo torneo, Mesa mesa) {
        ParticipanteTorneo ganador = participanteService.obtenerParticipantes(torneo).stream()
                .filter(p -> mesa.equals(p.getMesa()) && !p.isEliminado())
                .findFirst()
                .orElse(null);

        if (ganador != null) {
            int nuevaRonda = torneoMesaRepository.findByTorneo(torneo).stream()
                    .mapToInt(TorneoMesa::getRonda).max().orElse(1) + 1;

            TorneoMesa nuevaMesa = TorneoMesa.builder().torneo(torneo).ronda(nuevaRonda).build();
            torneoMesaRepository.save(nuevaMesa);

            ganador.setMesa(nuevaMesa.getMesa());
            if (!torneo.isEliminacionDirecta()) {
                ganador.setFichasActuales(torneo.getFichasIniciales());
            }

            participanteService.guardarParticipante(ganador);

            log.info("Avanzó el jugador {} a la ronda {} del torneo '{}'.",
                    ganador.getUser().getUsername(), nuevaRonda, torneo.getNombre());
        }
    }

    private void gestionarBlinds() {
        List<Torneo> torneosEnCurso = torneoService.listarTorneosEnCurso();
        Date ahora = new Date();

        for (Torneo torneo : torneosEnCurso) {
            List<BlindLevel> niveles = torneo.getBlindLevels();
            if (niveles == null || niveles.isEmpty()) continue;

            int actual = torneo.getNivelCiegasActual();
            if (actual >= niveles.size()) continue;

            BlindLevel nivelActual = niveles.get(actual);
            Date inicioNivel = torneo.getTimestampInicioNivel();

            if (inicioNivel == null) {
                torneo.setTimestampInicioNivel(new Date());
                torneoService.guardarTorneo(torneo);
                continue;
            }

            long duracion = nivelActual.getDuracionSegundos() * 1000L;
            if (ahora.getTime() - inicioNivel.getTime() >= duracion) {
                torneo.setNivelCiegasActual(actual + 1);
                torneo.setTimestampInicioNivel(new Date());
                torneoService.guardarTorneo(torneo);

                String resumen = "Nivel " + (actual + 1) + " (SB: " + nivelActual.getSmallBlind()
                        + ", BB: " + nivelActual.getBigBlind() + ")";

                log.info("Torneo '{}' avanzó a {}", torneo.getNombre(), resumen);
            }
        }
    }
}