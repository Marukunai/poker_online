package com.pokeronline.torneo.service;

import com.pokeronline.model.Mesa;
import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.model.TorneoMesa;
import com.pokeronline.torneo.repository.TorneoMesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TorneoMesaService {

    private final TorneoMesaRepository torneoMesaRepository;

    public TorneoMesa vincularMesaATorneo(Torneo torneo, Mesa mesa) {
        TorneoMesa existente = torneoMesaRepository.findByTorneoAndMesa(torneo, mesa).orElse(null);
        if (existente != null) {
            return existente;
        }

        TorneoMesa nuevo = TorneoMesa.builder()
                .torneo(torneo)
                .mesa(mesa)
                .build();

        return torneoMesaRepository.save(nuevo);
    }

    public List<TorneoMesa> obtenerMesasDelTorneo(Torneo torneo) {
        return torneoMesaRepository.findByTorneo(torneo);
    }

    public void desvincularMesa(Mesa mesa) {
        torneoMesaRepository.findByMesa(mesa).ifPresent(torneoMesaRepository::delete);
    }
}