package com.pokeronline.controller;

import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.service.EspectadorMesaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mesa/espectadores")
@RequiredArgsConstructor
public class EspectadorMesaController {

    private final EspectadorMesaService espectadorMesaService;
    private final UserRepository userRepository;

    @PostMapping("/{mesaId}/unirse")
    public ResponseEntity<?> unirseComoEspectador(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long mesaId) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        espectadorMesaService.unirComoEspectador(mesaId, user);
        return ResponseEntity.ok("Te has unido como espectador");
    }

    @PostMapping("/{mesaId}/salir")
    public ResponseEntity<?> salirComoEspectador(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long mesaId) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        espectadorMesaService.salirDeEspectador(mesaId, user);
        return ResponseEntity.ok("Has salido del modo espectador");
    }

    @GetMapping("/{mesaId}")
    public ResponseEntity<?> listarEspectadores(@PathVariable Long mesaId) {
        return ResponseEntity.ok(espectadorMesaService.listarEspectadores(mesaId));
    }
}