package com.pokeronline.service;

import com.pokeronline.dto.UserDTO;
import com.pokeronline.model.EspectadorMesa;
import com.pokeronline.model.Mesa;
import com.pokeronline.model.User;
import com.pokeronline.repository.EspectadorMesaRepository;
import com.pokeronline.repository.MesaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EspectadorMesaService {

    private final EspectadorMesaRepository espectadorMesaRepository;
    private final MesaRepository mesaRepository;

    public void unirComoEspectador(Long mesaId, User user) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        if (espectadorMesaRepository.findByMesaAndUser(mesa, user).isEmpty()) {
            EspectadorMesa espectador = EspectadorMesa.builder()
                    .mesa(mesa)
                    .user(user)
                    .fechaEntrada(new Date())
                    .build();
            espectadorMesaRepository.save(espectador);
        } else throw new RuntimeException("Ya estás unido a la mesa");
    }

    @Transactional
    public void salirDeEspectador(Long mesaId, User user) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        espectadorMesaRepository.deleteByMesaAndUser(mesa, user);
    }

    public List<UserDTO> listarEspectadores(Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        return espectadorMesaRepository.findByMesa(mesa).stream()
                .map(espec -> {
                    User u = espec.getUser();
                    return UserDTO.builder()
                            .id(u.getId())
                            .username(u.getUsername())
                            .email(u.getEmail())
                            .avatarUrl(u.getAvatarUrl())
                            .fichas(u.getFichas())
                            .partidasGanadas(u.getPartidasGanadas())
                            .manosJugadas(u.getManosJugadas())
                            .manosGanadas(u.getManosGanadas())
                            .vecesAllIn(u.getVecesAllIn())
                            .fichasGanadasHistoricas(u.getFichasGanadasHistoricas())
                            .vecesHizoBluff(u.getVecesHizoBluff())
                            .build();
                }).toList();
    }
}