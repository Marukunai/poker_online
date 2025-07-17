package com.pokeronline.logros.controller;

import com.pokeronline.logros.dto.LogroUsuarioDTO;
import com.pokeronline.logros.service.LogroUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public void asignarLogro(@PathVariable Long userId, @PathVariable Long logroId) {
        logroUsuarioService.asignarLogroPorIdSiNoTiene(userId, logroId);
    }

    @DeleteMapping("/{userId}/eliminar/{logroId}")
    public void eliminarLogro(@PathVariable Long userId, @PathVariable Long logroId) {
        logroUsuarioService.eliminarLogroDeUsuario(userId, logroId);
    }
}