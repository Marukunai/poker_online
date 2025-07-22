package com.pokeronline.torneo.service;

import com.pokeronline.logros.service.LogroService;
import com.pokeronline.model.User;
import com.pokeronline.torneo.dto.EstadisticasTorneoDTO;
import com.pokeronline.torneo.model.ParticipanteTorneo;
import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.model.TorneoEstado;
import com.pokeronline.torneo.repository.ParticipanteTorneoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipanteTorneoService {

    private final LogroService logroService;
    private final ParticipanteTorneoRepository participanteTorneoRepository;

    public ParticipanteTorneo inscribirUsuario(Torneo torneo, User user) {
        if (participanteTorneoRepository.findByTorneoAndUser(torneo, user).isPresent()) throw new RuntimeException("Ya estás unido al torneo");
        return participanteTorneoRepository.findByTorneoAndUser(torneo, user).orElseGet(() -> {
            ParticipanteTorneo nuevo = ParticipanteTorneo.builder()
                    .torneo(torneo)
                    .user(user)
                    .puntos(0)
                    .fichasActuales(torneo.getFichasIniciales())
                    .eliminado(false)
                    .build();

            // Al final de inscribirUsuario(...) en ParticipanteTorneoService
            long totalTorneos = participanteTorneoRepository.countByUser(user);
            logroService.otorgarLogroSiNoTiene(user.getId(), "Primer Torneo");

            if (totalTorneos >= 10) {
                logroService.otorgarLogroSiNoTiene(user.getId(), "Jugador Competitivo Constante");
            }
            if (totalTorneos >= 25) {
                logroService.otorgarLogroSiNoTiene(user.getId(), "Amante de los Torneos");
            }

            return participanteTorneoRepository.save(nuevo);
        });
    }

    public List<ParticipanteTorneo> obtenerParticipantes(Torneo torneo) {
        return participanteTorneoRepository.findByTorneo(torneo);
    }

    public List<ParticipanteTorneo> obtenerRanking(Torneo torneo) {
        return participanteTorneoRepository.findByTorneo(torneo).stream()
                .sorted(
                        Comparator.comparing(ParticipanteTorneo::isEliminado)
                                .thenComparing(ParticipanteTorneo::getPuntos, Comparator.reverseOrder())
                                .thenComparing(ParticipanteTorneo::getFichasActuales, Comparator.reverseOrder())
                )
                .toList();
    }

    public void sumarPuntos(User user, Torneo torneo, int puntosGanados) {
        ParticipanteTorneo participante = participanteTorneoRepository.findByTorneoAndUser(torneo, user)
                .orElseThrow(() -> new RuntimeException("Participante no encontrado"));
        participante.setPuntos(participante.getPuntos() + puntosGanados);
        participanteTorneoRepository.save(participante);
    }

    public Optional<ParticipanteTorneo> obtenerParticipante(Torneo torneo, User user) {
        return participanteTorneoRepository.findByTorneoAndUser(torneo, user);
    }

    public void marcarEliminado(Torneo torneo, User user) {
        participanteTorneoRepository.findByTorneoAndUser(torneo, user).ifPresent(p -> {
            p.setEliminado(true);
            participanteTorneoRepository.save(p);
        });
    }

    @Transactional
    public void eliminarParticipante(ParticipanteTorneo participante) {
        participante.setEliminado(true);
        participante.setMesa(null); // Para evitar que quede asociado a alguna mesa
        participante.setFichasActuales(0);
        participanteTorneoRepository.save(participante);
    }

    public void guardarParticipante(ParticipanteTorneo participante) {
        participanteTorneoRepository.save(participante);
    }

    public void otorgarPuntosPorRendimiento(Torneo torneo) {
        List<ParticipanteTorneo> ranking = obtenerRanking(torneo);
        for (int i = 0; i < ranking.size(); i++) {
            ParticipanteTorneo p = ranking.get(i);
            int puntos = Math.max(0, 100 - i * 10); // Ejemplo: 1.º 100, 2.º 90, 3.º 80, etc.
            p.setPuntos(p.getPuntos() + puntos);
            guardarParticipante(p);
        }
    }

    public EstadisticasTorneoDTO obtenerEstadisticas(User user) {
        List<ParticipanteTorneo> historial = participanteTorneoRepository.findByUser(user);
        if (historial.isEmpty()) {
            return EstadisticasTorneoDTO.builder()
                    .username(user.getUsername())
                    .torneosJugados(0)
                    .torneosGanados(0)
                    .ratioVictorias(0)
                    .vecesPrimeraRonda(0)
                    .puntosTotales(0)
                    .fichasGanadas(0)
                    .mejorPosicion(0)
                    .posicionPromedio(0)
                    .fechaUltimoTorneo("-")
                    .build();
        }

        int ganados = 0;
        int primeraRonda = 0;
        int puntos = 0;
        int fichas = 0;
        int mejor = Integer.MAX_VALUE;
        int sumaPosiciones = 0;
        String ultimaFecha = "";

        for (ParticipanteTorneo p : historial) {
            Torneo torneo = p.getTorneo();
            if (torneo.getEstado() != TorneoEstado.FINALIZADO) continue;

            puntos += p.getPuntos();
            fichas += p.getFichasActuales();
            int pos = p.getPosicion();
            if (pos == 1) ganados++;
            if (pos > 0) {
                mejor = Math.min(mejor, pos);
                sumaPosiciones += pos;
            }
            if (p.isEliminado() && torneo.getMesas().stream().allMatch(m -> m.getRonda() == 1)) {
                primeraRonda++;
            }

            if (torneo.getFechaInicio() != null) {
                if (ultimaFecha.isEmpty() || torneo.getFechaInicio().after(Date.from(Instant.parse(ultimaFecha)))) {
                    ultimaFecha = torneo.getFechaInicio().toString();
                }
            }
        }

        int finalizados = (int) historial.stream()
                .filter(p -> p.getTorneo().getEstado() == TorneoEstado.FINALIZADO).count();

        double ratio = finalizados > 0 ? (double) ganados / finalizados : 0.0;
        double promedio = finalizados > 0 ? (double) sumaPosiciones / finalizados : 0.0;

        return EstadisticasTorneoDTO.builder()
                .username(user.getUsername())
                .torneosJugados(finalizados)
                .torneosGanados(ganados)
                .ratioVictorias(ratio)
                .vecesPrimeraRonda(primeraRonda)
                .puntosTotales(puntos)
                .fichasGanadas(fichas)
                .mejorPosicion(mejor == Integer.MAX_VALUE ? 0 : mejor)
                .posicionPromedio(promedio)
                .fechaUltimoTorneo(ultimaFecha)
                .build();
    }
}