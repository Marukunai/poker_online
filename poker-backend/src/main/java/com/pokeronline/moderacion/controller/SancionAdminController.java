package com.pokeronline.moderacion.controller;

import com.pokeronline.moderacion.model.MotivoSancion;
import com.pokeronline.moderacion.model.TipoSancion;
import com.pokeronline.moderacion.service.ModeracionAdminService;
import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sanciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SancionAdminController {

    private final ModeracionAdminService moderacionAdminService;
    private final UserRepository userRepository;

    @PostMapping("/aplicar")
    public ResponseEntity<?> aplicarSancionManual(@RequestParam Long userId,
                                                  @RequestParam MotivoSancion motivo,
                                                  @RequestParam TipoSancion tipo,
                                                  @RequestParam(required = false) String descripcion,
                                                  @RequestParam(defaultValue = "0") int diasDuracion) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        moderacionAdminService.aplicarSancionManual(userId, tipo, motivo,
                descripcion != null ? descripcion : "Sanción aplicada manualmente por un moderador.",
                diasDuracion);

        return ResponseEntity.ok("Sanción aplicada correctamente al usuario " + user.getUsername());
    }
}