package com.pokeronline.logros.controller;

import com.pokeronline.logros.dto.LogroUsuarioDTO;
import com.pokeronline.logros.service.LogroUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logros/usuario")
@RequiredArgsConstructor
public class LogroUsuarioController {

    private final LogroUsuarioService logroUsuarioService;

    @GetMapping("/{userId}")
    public List<LogroUsuarioDTO> logrosDeUsuario(@PathVariable Long userId) {
        return logroUsuarioService.obtenerLogrosUsuario(userId);
    }

    @PostMapping("/{userId}/asignar/{logroId}")
    public ResponseEntity<?> asignarLogro(@PathVariable Long userId, @PathVariable Long logroId) {
        logroUsuarioService.asignarLogroPorIdSiNoTiene(userId, logroId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Logro otorgado correctamente",
                        "userId", userId,
                        "logroId", logroId
                )
        );
    }

    @DeleteMapping("/{userId}/eliminar/{logroId}")
    public ResponseEntity<?> eliminarLogro(@PathVariable Long userId, @PathVariable Long logroId) {
        logroUsuarioService.eliminarLogroDeUsuario(userId, logroId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Logro eliminado correctamente",
                        "userId", userId,
                        "logroId", logroId
                )
        );
    }
}