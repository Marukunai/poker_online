package com.pokeronline.controller;

import com.pokeronline.dto.HistorialManoDTO;
import com.pokeronline.dto.UserDTO;
import com.pokeronline.exception.UnauthorizedException;
import com.pokeronline.model.HistorialMano;
import com.pokeronline.model.User;
import com.pokeronline.repository.HistorialManoRepository;
import com.pokeronline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final HistorialManoRepository historialManoRepository;
    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) throw new UnauthorizedException("Debes iniciar sesión");

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();

        UserDTO dto = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .fichas(user.getFichas())
                .partidasGanadas(user.getPartidasGanadas())
                .manosJugadas(user.getManosJugadas())
                .manosGanadas(user.getManosGanadas())
                .vecesAllIn(user.getVecesAllIn())
                .fichasGanadasHistoricas(user.getFichasGanadasHistoricas())
                .vecesHizoBluff(user.getVecesHizoBluff())
                .build();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<UserDTO>> obtenerRanking() {
        List<User> top = userRepository.findAll(Sort.by(Sort.Direction.DESC, "partidasGanadas"));

        List<UserDTO> ranking = top.stream().map(user -> UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .fichas(user.getFichas())
                .partidasGanadas(user.getPartidasGanadas())
                .manosJugadas(user.getManosJugadas())
                .manosGanadas(user.getManosGanadas())
                .vecesAllIn(user.getVecesAllIn())
                .fichasGanadasHistoricas(user.getFichasGanadasHistoricas())
                .vecesHizoBluff(user.getVecesHizoBluff())
                .build()
        ).toList();

        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/historial")
    public ResponseEntity<?> historialPartidas(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) throw new UnauthorizedException("Debes iniciar sesión");

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<HistorialMano> historial = historialManoRepository.findByJugadorOrderByFechaDesc(user);

        List<HistorialManoDTO> dtoList = historial.stream().map(mano -> HistorialManoDTO.builder()
                .fecha(mano.getFecha())
                .fichasGanadas(mano.getFichasGanadas())
                .cartasGanadoras(mano.getCartasGanadoras())
                .cartasJugador(mano.getCartasJugador())
                .contraJugadores(mano.getContraJugadores())
                .tipoManoGanadora(mano.getTipoManoGanadora())
                .faseFinal(String.valueOf(mano.getFaseFinal()))
                .build()).toList();

        return ResponseEntity.ok(dtoList);
    }
}