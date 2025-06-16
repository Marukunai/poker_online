package com.pokeronline.controller;

import com.pokeronline.dto.JugadorEnMesaDTO;
import com.pokeronline.model.Mesa;
import com.pokeronline.model.User;
import com.pokeronline.model.UserMesa;
import com.pokeronline.repository.MesaRepository;
import com.pokeronline.repository.UserMesaRepository;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.service.BarajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
public class MesaController {

    private final MesaRepository mesaRepository;
    private final UserRepository userRepository;
    private final UserMesaRepository userMesaRepository;
    private final BarajaService barajaService;

    @GetMapping
    public ResponseEntity<List<Mesa>> getMesasActivas() {
        return ResponseEntity.ok(mesaRepository.findByActivaTrue());
    }

    @PostMapping
    public ResponseEntity<Mesa> crearMesa(@RequestParam String nombre, @RequestParam(defaultValue = "6") int maxJugadores) {
        Mesa nuevaMesa = Mesa.builder()
                .nombre(nombre)
                .activa(true)
                .maxJugadores(maxJugadores)
                .build();
        return ResponseEntity.ok(mesaRepository.save(nuevaMesa));
    }

    @PostMapping("/{mesaId}/unirse")
    public ResponseEntity<?> unirseAMesa(@PathVariable Long mesaId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body("No autenticado");

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        if (userMesaRepository.findByUserAndMesa(user, mesa).isPresent()) {
            return ResponseEntity.badRequest().body("Ya estás en esta mesa");
        }

        long jugadoresActuales = userMesaRepository.findByMesa(mesa).size();
        if (jugadoresActuales >= mesa.getMaxJugadores()) {
            return ResponseEntity.badRequest().body("La mesa está llena");
        }

        UserMesa userMesa = UserMesa.builder()
                .user(user)
                .mesa(mesa)
                .fichasEnMesa(100)
                .enJuego(true)
                .build();

        userMesaRepository.save(userMesa);
        return ResponseEntity.ok("Unido correctamente a la mesa");
    }

    @GetMapping("/{mesaId}/jugadores")
    public ResponseEntity<?> listarJugadoresDeMesa(@PathVariable Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        List<UserMesa> relaciones = userMesaRepository.findByMesa(mesa);

        List<JugadorEnMesaDTO> jugadores = relaciones.stream().map(um ->
                new JugadorEnMesaDTO(
                        um.getUser().getId(),
                        um.getUser().getUsername(),
                        um.getUser().getAvatarUrl(),
                        um.getFichasEnMesa()
                )
        ).toList();

        return ResponseEntity.ok(jugadores);
    }

    @PostMapping("/{mesaId}/repartir")
    public ResponseEntity<?> repartirCartas(@PathVariable Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        barajaService.repartirCartas(mesa);
        mesaRepository.save(mesa);

        return ResponseEntity.ok("Cartas repartidas correctamente");
    }
}