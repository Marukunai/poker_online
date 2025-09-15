package com.pokeronline.torneo.service;

import com.pokeronline.exception.ResourceNotFoundException;
import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.torneo.model.EsperaTorneo;
import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.repository.EsperaTorneoRepository;
import com.pokeronline.torneo.repository.TorneoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EsperaTorneoService {

    private final EsperaTorneoRepository esperaTorneoRepository;
    private final TorneoRepository torneoRepository;
    private final UserRepository userRepository;

    public EsperaTorneo registrarPresencia(Long torneoId, Long userId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return esperaTorneoRepository.findByTorneoAndUser(torneo, user)
                .orElseGet(() -> esperaTorneoRepository.save(
                        EsperaTorneo.builder()
                                .torneo(torneo)
                                .user(user)
                                .timestampIngreso(new Date())
                                .build()
                ));
    }

    public List<EsperaTorneo> obtenerEsperando(Long torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
        return esperaTorneoRepository.findByTorneo(torneo);
    }

    @Transactional
    public void limpiarEsperaTorneo(Long torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
        esperaTorneoRepository.deleteByTorneo(torneo);
    }
}