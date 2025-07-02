package com.pokeronline.torneo.service;

import com.pokeronline.model.User;
import com.pokeronline.torneo.model.ParticipanteTorneo;
import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.repository.ParticipanteTorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipanteTorneoService {

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
            return participanteTorneoRepository.save(nuevo);
        });
    }

    public List<ParticipanteTorneo> obtenerParticipantes(Torneo torneo) {
        return participanteTorneoRepository.findByTorneo(torneo);
    }

    public List<ParticipanteTorneo> obtenerRanking(Torneo torneo) {
        return participanteTorneoRepository.findByTorneo(torneo).stream()
                .sorted(
                        // 1. Sort by 'eliminado'
                        Comparator.comparing(ParticipanteTorneo::isEliminado)
                                // 2. Then, sort by 'puntos'
                                .thenComparing(ParticipanteTorneo::getPuntos, Comparator.reverseOrder())
                                // 3. Finally, sort by 'fichasActuales'
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
}