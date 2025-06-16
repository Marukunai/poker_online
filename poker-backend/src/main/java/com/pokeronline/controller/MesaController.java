package com.pokeronline.controller;

import com.pokeronline.model.Mesa;
import com.pokeronline.model.User;
import com.pokeronline.model.UserMesa;
import com.pokeronline.repository.MesaRepository;
import com.pokeronline.repository.UserMesaRepository;
import com.pokeronline.repository.UserRepository;
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

    // ✅ Obtener todas las mesas activas
    @GetMapping
    public ResponseEntity<List<Mesa>> getMesasActivas() {
        return ResponseEntity.ok(mesaRepository.findByActivaTrue());
    }

    // ✅ Crear una nueva mesa
    @PostMapping
    public ResponseEntity<Mesa> crearMesa(@RequestParam String nombre, @RequestParam(defaultValue = "6") int maxJugadores) {
        Mesa nuevaMesa = Mesa.builder()
                .nombre(nombre)
                .activa(true)
                .maxJugadores(maxJugadores)
                .build();
        return ResponseEntity.ok(mesaRepository.save(nuevaMesa));
    }

    // ✅ Unirse a una mesa
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
                .fichasEnMesa(100) // cantidad inicial, por ejemplo
                .enJuego(true)
                .build();

        userMesaRepository.save(userMesa);
        return ResponseEntity.ok("Unido correctamente a la mesa");
    }
}
