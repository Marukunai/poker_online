package com.pokeronline.torneo.scheduler;

import com.pokeronline.model.Mesa;
import com.pokeronline.torneo.model.ParticipanteTorneo;
import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.model.TorneoEstado;
import com.pokeronline.torneo.model.TorneoMesa;
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

    private final TorneoService torneoService;
    private final ParticipanteTorneoService participanteService;
    private final TorneoMesaRepository torneoMesaRepository;

    @Scheduled(fixedRate = 60000) // Cada minuto
    @Transactional
    public void gestionarTorneos() {
        revisarTorneosPendientes();
        revisarTorneosEnCurso();
    }

    private void revisarTorneosPendientes() {
        List<Torneo> pendientes = torneoService.listarTorneosPendientes();
        Date ahora = new Date();

        for (Torneo torneo : pendientes) {
            if (torneo.getFechaInicio().before(ahora)) {
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

        log.info("Torneo '{}' iniciado con {} participantes distribuidos en {} mesas.", torneo.getNombre(), participantes.size(), mesas.size());
    }

    public void revisarTorneosEnCurso() {
        List<Torneo> torneosEnCurso = torneoService.listarTorneosEnCurso();
        for (Torneo torneo : torneosEnCurso) {
            List<ParticipanteTorneo> activos = participanteService.obtenerParticipantes(torneo)
                    .stream()
                    .filter(p -> !p.isEliminado())
                    .toList();

            revisarMesasFinalizadas(torneo, activos);

            if (activos.size() <= 1) {
                torneoService.finalizarTorneo(torneo);
                System.out.println("Torneo " + torneo.getNombre() + " finalizado automáticamente.");
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
            participanteService.guardarParticipante(ganador);

            log.info("Avanzó el jugador {} a la ronda {} del torneo '{}'.", ganador.getUser().getUsername(), nuevaRonda, torneo.getNombre());
        }
    }
}