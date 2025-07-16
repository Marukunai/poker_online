package com.pokeronline.admin.service;

import com.pokeronline.admin.dto.*;
import com.pokeronline.model.*;
import com.pokeronline.repository.*;
import com.pokeronline.torneo.model.*;
import com.pokeronline.torneo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final TorneoRepository torneoRepository;
    private final MesaRepository mesaRepository;
    private final BlindLevelRepository blindLevelRepository;
    private final UserRepository userRepository;

    public Torneo actualizarTorneo(Long id, UpdateTorneoDTO dto) {
        Torneo torneo = torneoRepository.findById(id).orElseThrow();
        torneo.setNombre(dto.getNombre());
        torneo.setFechaInicio(dto.getFechaInicio());
        torneo.setFechaFin(dto.getFechaFin());
        torneo.setEliminacionDirecta(dto.isEliminacionDirecta());
        torneo.setFichasIniciales(dto.getFichasIniciales());
        return torneoRepository.save(torneo);
    }

    public Mesa actualizarMesa(Long id, UpdateMesaDTO dto) {
        Mesa mesa = mesaRepository.findById(id).orElseThrow();
        mesa.setNombre(dto.getNombre());
        mesa.setActiva(dto.isActiva());
        mesa.setSmallBlind(dto.getSmallBlind());
        mesa.setBigBlind(dto.getBigBlind());
        mesa.setMaxJugadores(dto.getMaxJugadores());
        return mesaRepository.save(mesa);
    }

    public BlindLevel actualizarBlindLevel(Long id, UpdateBlindLevelDTO dto) {
        BlindLevel level = blindLevelRepository.findById(id).orElseThrow();
        level.setNivel(dto.getNivel());
        level.setSmallBlind(dto.getSmallBlind());
        level.setBigBlind(dto.getBigBlind());
        level.setDuracionSegundos(dto.getDuracionSegundos());
        return blindLevelRepository.save(level);
    }

    public User actualizarUsuario(Long id, UpdateUserDTO dto) {
        User user = userRepository.findById(id).orElseThrow();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setFichas(dto.getFichas());
        return userRepository.save(user);
    }
}