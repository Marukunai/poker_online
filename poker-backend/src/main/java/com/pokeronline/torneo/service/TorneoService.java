package com.pokeronline.torneo.service;

import com.pokeronline.torneo.dto.CrearTorneoDTO;
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

    private final TorneoRepository torneoRepository;

    public List<Torneo> listarTorneos() {
        return torneoRepository.findAll();
    }

    public Optional<Torneo> obtenerTorneoPorId(Long id) {
        return torneoRepository.findById(id);
    }

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
}