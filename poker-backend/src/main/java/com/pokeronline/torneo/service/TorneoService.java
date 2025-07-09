package com.pokeronline.torneo.service;

import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.torneo.dto.CrearTorneoDTO;
import com.pokeronline.torneo.model.ParticipanteTorneo;
import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.model.TorneoEstado;
import com.pokeronline.torneo.repository.TorneoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TorneoService {

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
                .estado(TorneoEstado.PENDIENTE)
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

        int[] porcentajes = {50, 30, 20}; // 1.º 50%, 2.º 30%, 3.º 20%
        for (int i = 0; i < Math.min(ranking.size(), 3); i++) {
            ParticipanteTorneo participante = ranking.get(i);
            int premio = totalPremio * porcentajes[i] / 100;
            User user = participante.getUser();
            user.setFichas(user.getFichas() + premio);
            userRepository.save(user); // Asegúrate de tener esto inyectado
        }

        torneo.setEstado(TorneoEstado.FINALIZADO);
        torneoRepository.save(torneo);
    }
}