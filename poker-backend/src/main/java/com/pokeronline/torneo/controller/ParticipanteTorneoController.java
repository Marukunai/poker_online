package com.pokeronline.torneo.controller;

import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.torneo.dto.ParticipanteTorneoDTO;
import com.pokeronline.torneo.model.ParticipanteTorneo;
import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.service.ParticipanteTorneoService;
import com.pokeronline.torneo.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/torneos")
@RequiredArgsConstructor
public class ParticipanteTorneoController {

    private final ParticipanteTorneoService participanteService;
    private final TorneoService torneoService;
    private final UserRepository userRepository;

    @PostMapping("/{id}/inscribirse")
    public ParticipanteTorneoDTO inscribirse(@PathVariable Long id,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Torneo torneo = torneoService.obtenerTorneoPorId(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));


        ParticipanteTorneo participante = participanteService.inscribirUsuario(torneo, user);

        return ParticipanteTorneoDTO.builder()
                .id(participante.getId())
                .torneoId(torneo.getId())
                .username(user.getUsername())
                .puntos(0)
                .fichasActuales(torneo.getFichasIniciales())
                .eliminado(false)
                .build();
    }

    @GetMapping("/{id}/ranking")
    public List<ParticipanteTorneoDTO> ranking(@PathVariable Long id) {
        Torneo torneo = torneoService.obtenerTorneoPorId(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        List<ParticipanteTorneo> participantes = participanteService.obtenerRanking(torneo);

        return participantes.stream()
                .map(participante -> ParticipanteTorneoDTO.builder()
                        .id(participante.getId())
                        .torneoId(torneo.getId())
                        .username(participante.getUser().getUsername())
                        .puntos(participante.getPuntos())
                        .fichasActuales(participante.getFichasActuales())
                        .eliminado(participante.isEliminado())
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/participantes")
    public List<ParticipanteTorneoDTO> listarParticipantes(@PathVariable Long id) {
        Torneo torneo = torneoService.obtenerTorneoPorId(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        List<ParticipanteTorneo> participantes = participanteService.obtenerParticipantes(torneo);

        return participantes.stream()
                .map(participante -> ParticipanteTorneoDTO.builder()
                        .id(participante.getId())
                        .torneoId(torneo.getId())
                        .username(participante.getUser().getUsername())
                        .puntos(participante.getPuntos())
                        .fichasActuales(participante.getFichasActuales())
                        .eliminado(participante.isEliminado())
                        .build())
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}/participantes")
    public void eliminarParticipante(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Torneo torneo = torneoService.obtenerTorneoPorId(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
        participanteService.marcarEliminado(torneo, user);
    }

    @GetMapping("/{id}/datos")
    public ParticipanteTorneoDTO misDatos(@PathVariable Long id,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Torneo torneo = torneoService.obtenerTorneoPorId(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        ParticipanteTorneo participante = participanteService.obtenerParticipante(torneo, user)
                .orElseThrow(() -> new RuntimeException("No estás inscrito en este torneo"));

        return ParticipanteTorneoDTO.builder()
                .id(participante.getId())
                .torneoId(torneo.getId())
                .username(participante.getUser().getUsername())
                .puntos(participante.getPuntos())
                .fichasActuales(participante.getFichasActuales())
                .eliminado(participante.isEliminado())
                .build();
    }
}