package com.pokeronline.controller;

import com.pokeronline.dto.HistorialManoDTO;
import com.pokeronline.dto.PerfilCompletoDTO;
import com.pokeronline.dto.UserDTO;
import com.pokeronline.estadisticas.dto.TorneoHistorialDTO;
import com.pokeronline.moderacion.dto.SancionDTO;
import com.pokeronline.estadisticas.service.EstadisticasService;
import com.pokeronline.exception.UnauthorizedException;
import com.pokeronline.logros.dto.LogroUsuarioDTO;
import com.pokeronline.logros.service.LogroUsuarioService;
import com.pokeronline.model.HistorialMano;
import com.pokeronline.model.User;
import com.pokeronline.repository.HistorialManoRepository;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.service.UserService;
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

    private final EstadisticasService estadisticasService;
    private final LogroUsuarioService logroUsuarioService;
    private final HistorialManoRepository historialManoRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final com.pokeronline.moderacion.service.SancionService sancionService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) throw new UnauthorizedException("Debes iniciar sesión");

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();

        var sancionesDto = sancionService.obtenerSancionesUsuario(user.getId());

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
                .nivelBot(user.isEsIA() ? user.getNivelBot() : null)
                .estiloBot(user.isEsIA() ? user.getEstiloBot() : null)
                .sanciones(sancionesDto)
                .bloqueado(user.isBloqueado())
                .chatBloqueado(user.isChatBloqueado())
                .build();

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/updateUsername")
    public ResponseEntity<?> actualizarNombre(@RequestParam Long userId, @RequestParam String nuevoUsername) {
        userService.actualizarPerfil(userId, nuevoUsername);
        return ResponseEntity.ok("Nombre actualizado correctamente");
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

    @GetMapping("/public-profile/{userId}")
    public ResponseEntity<UserDTO> getPublicProfile(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserDTO dto = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .fichas(user.getFichas())
                .partidasGanadas(user.getPartidasGanadas())
                .manosJugadas(user.getManosJugadas())
                .manosGanadas(user.getManosGanadas())
                .fichasGanadasHistoricas(user.getFichasGanadasHistoricas())
                .vecesAllIn(user.getVecesAllIn())
                .vecesHizoBluff(user.getVecesHizoBluff())
                .nivelBot(user.isEsIA() ? user.getNivelBot() : null)
                .estiloBot(user.isEsIA() ? user.getEstiloBot() : null)
                .build();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{userId}/resumen-completo")
    public ResponseEntity<PerfilCompletoDTO> resumenCompleto(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // DTO público
        UserDTO dto = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .fichas(user.getFichas())
                .partidasGanadas(user.getPartidasGanadas())
                .manosJugadas(user.getManosJugadas())
                .manosGanadas(user.getManosGanadas())
                .fichasGanadasHistoricas(user.getFichasGanadasHistoricas())
                .vecesAllIn(user.getVecesAllIn())
                .vecesHizoBluff(user.getVecesHizoBluff())
                .nivelBot(user.isEsIA() ? user.getNivelBot() : null)
                .estiloBot(user.isEsIA() ? user.getEstiloBot() : null)
                .build();

        // Logros
        List<LogroUsuarioDTO> logros = logroUsuarioService.obtenerLogrosUsuario(userId);

        // Torneos
        List<TorneoHistorialDTO> historialTorneos = estadisticasService.obtenerHistorialTorneos(userId);

        // Últimas 5 manos
        List<HistorialManoDTO> ultimasPartidas = historialManoRepository.findTop5ByJugadorOrderByFechaDesc(user)
                .stream()
                .map(mano -> HistorialManoDTO.builder()
                        .fecha(mano.getFecha())
                        .fichasGanadas(mano.getFichasGanadas())
                        .cartasGanadoras(mano.getCartasGanadoras())
                        .cartasJugador(mano.getCartasJugador())
                        .contraJugadores(mano.getContraJugadores())
                        .tipoManoGanadora(mano.getTipoManoGanadora())
                        .faseFinal(String.valueOf(mano.getFaseFinal()))
                        .build()
                ).toList();

        PerfilCompletoDTO perfil = PerfilCompletoDTO.builder()
                .user(dto)
                .logros(logros)
                .historialTorneos(historialTorneos)
                .ultimasPartidas(ultimasPartidas)
                .build();

        return ResponseEntity.ok(perfil);
    }
}