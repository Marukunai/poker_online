package com.pokeronline.controller;

import com.pokeronline.model.*;
import com.pokeronline.repository.MesaRepository;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;
    private final MesaRepository mesaRepository;
    private final UserRepository userRepository;

    @PostMapping("/iniciar/{mesaId}")
    public ResponseEntity<?> iniciarTurnos(@PathVariable Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        turnoService.inicializarTurnos(mesa);
        return ResponseEntity.ok("Turnos iniciados correctamente");
    }

    @GetMapping("/actual/{mesaId}")
    public ResponseEntity<Turno> turnoActual(@PathVariable Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        return ResponseEntity.ok(turnoService.getTurnoActual(mesa));
    }

    @PostMapping("/avanzar/{mesaId}")
    public ResponseEntity<?> avanzar(@PathVariable Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        turnoService.avanzarTurno(mesa);
        return ResponseEntity.ok("Turno avanzado correctamente");
    }

    @PostMapping("/accion/{mesaId}")
    public ResponseEntity<?> realizarAccion(
            @PathVariable Long mesaId,
            @RequestParam Accion accion,
            @RequestParam(defaultValue = "0") int cantidad,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        turnoService.realizarAccion(mesa, user, accion, cantidad);

        return ResponseEntity.ok("Acción realizada correctamente");
    }

    @PostMapping("/fase/{mesaId}/siguiente")
    public ResponseEntity<?> siguienteFase(@PathVariable Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        turnoService.avanzarFase(mesa);
        return ResponseEntity.ok("Avanzado a la siguiente fase: " + mesa.getFase());
    }
}