package com.pokeronline.torneo.service;

import com.pokeronline.logros.service.LogroService;
import com.pokeronline.model.User;
import com.pokeronline.model.UserMesa;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.torneo.dto.CrearTorneoDTO;
import com.pokeronline.torneo.model.ParticipanteTorneo;
import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.model.TorneoEstado;
import com.pokeronline.torneo.model.TorneoMesa;
import com.pokeronline.torneo.repository.ParticipanteTorneoRepository;
import com.pokeronline.torneo.repository.TorneoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TorneoService {

    private final ParticipanteTorneoRepository participanteTorneoRepository;
    private final LogroService logroService;
    private final ParticipanteTorneoService participanteTorneoService;
    private final UserRepository userRepository;
    private final TorneoRepository torneoRepository;

    public List<Torneo> listarTorneos() {
        return torneoRepository.findAll();
    }

    public Optional<Torneo> obtenerTorneoPorId(Long id) {
        return torneoRepository.findById(id);
    }

    public Optional<Torneo> obtenerTorneoPorNombre(String nombre) { return torneoRepository.findByNombre(nombre); }

    @Transactional
    public Torneo crearTorneo(CrearTorneoDTO dto) {
        Torneo torneo = Torneo.builder()
                .nombre(dto.getNombre())
                .buyIn(dto.getBuyIn())
                .maxParticipantes(dto.getMaxParticipantes())
                .fichasIniciales(dto.getFichasIniciales())
                .premioTotal(dto.getPremioTotal())
                .eliminacionDirecta(dto.isEliminacionDirecta())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .estado(TorneoEstado.PENDIENTE)
                .nivelCiegasActual(0)
                .timestampInicioNivel(null)
                .build();
        return torneoRepository.save(torneo);
    }

    @Transactional
    public void actualizarEstadoTorneo(Torneo torneo, TorneoEstado nuevoEstado) {
        torneo.setEstado(nuevoEstado);
        torneoRepository.save(torneo);
    }

    public void eliminarTorneo(Long id) {
        torneoRepository.deleteById(id);
    }

    @Transactional
    public void guardarTorneo(Torneo torneo) {
        torneoRepository.save(torneo);
    }

    public List<Torneo> listarTorneosEnCurso() {
        return torneoRepository.findByEstado(TorneoEstado.EN_CURSO);
    }

    public List<Torneo> listarTorneosPendientes() {
        return torneoRepository.findByEstado(TorneoEstado.PENDIENTE);
    }

    @Transactional
    public void finalizarTorneo(Torneo torneo) {
        List<ParticipanteTorneo> ranking = participanteTorneoService.obtenerRanking(torneo);
        int totalPremio = torneo.getPremioTotal();

        int[] porcentajes = {50, 30, 20}; // Solo top 3 premian
        for (int i = 0; i < ranking.size(); i++) {
            ParticipanteTorneo participante = ranking.get(i);
            participante.setPosicion(i + 1);

            User user = participante.getUser();

            // Logro: Top 3
            if (i < 3) {
                logroService.otorgarLogroSiNoTiene(user.getId(), "Top 3");
            }

            // Logro: Campeón
            if (i == 0) {
                logroService.otorgarLogroSiNoTiene(user.getId(), "Campeón");

                long torneosGanados = participanteTorneoRepository.countByUserAndPosicion(user, 1);
                if (torneosGanados >= 10) {
                    logroService.otorgarLogroSiNoTiene(user.getId(), "Jugador Legendario");
                }
            }


            if (i < porcentajes.length) {
                int premio = totalPremio * porcentajes[i] / 100;
                user.setFichas(user.getFichas() + premio);
                userRepository.save(user);

                log.info("Premio: {} fichas para {} (posición {})", premio, user.getUsername(), i + 1);
            }

            participanteTorneoService.guardarParticipante(participante);
        }

        // Detectar ronda final (mayor ronda entre todas las mesas del torneo)
        int rondaFinal = torneo.getMesas().stream()
                .mapToInt(TorneoMesa::getRonda)
                .max()
                .orElse(1);

        for (ParticipanteTorneo participante : ranking) {
            User user = participante.getUser();

            boolean estaEnMesaFinal = torneo.getMesas().stream()
                    .filter(tm -> tm.getRonda() == rondaFinal)
                    .map(TorneoMesa::getMesa)
                    .anyMatch(m -> m.getJugadores().stream()
                            .map(UserMesa::getUser)
                            .anyMatch(j -> j.getId().equals(user.getId()))
                    );

            if (estaEnMesaFinal) {
                logroService.otorgarLogroSiNoTiene(user.getId(), "Finalista");
            }
        }

        int clasificados = Math.max(1, ranking.size() / 2); // Por ejemplo, top 50% entran al bracket
        for (int i = 0; i < clasificados; i++) {
            User user = ranking.get(i).getUser();
            logroService.otorgarLogroSiNoTiene(user.getId(), "Clasificado Pro");
        }

        torneo.setEstado(TorneoEstado.FINALIZADO);
        torneoRepository.save(torneo);
        if (!torneo.isEliminacionDirecta()) {
            participanteTorneoService.otorgarPuntosPorRendimiento(torneo);
        }

        log.info("Torneo '{}' finalizado. Se asignaron posiciones a {} jugadores.", torneo.getNombre(), ranking.size());
    }
}