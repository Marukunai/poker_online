package com.pokeronline.admin.controller;

import com.pokeronline.admin.dto.*;
import com.pokeronline.admin.service.AdminService;
import com.pokeronline.model.*;
import com.pokeronline.torneo.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @PutMapping("/torneos/{id}")
    public Torneo actualizarTorneo(@PathVariable Long id, @RequestBody UpdateTorneoDTO dto) {
        return adminService.actualizarTorneo(id, dto);
    }

    @PutMapping("/mesas/{id}")
    public Mesa actualizarMesa(@PathVariable Long id, @RequestBody UpdateMesaDTO dto) {
        return adminService.actualizarMesa(id, dto);
    }

    @PutMapping("/blind-levels/{id}")
    public BlindLevel actualizarBlindLevel(@PathVariable Long id, @RequestBody UpdateBlindLevelDTO dto) {
        return adminService.actualizarBlindLevel(id, dto);
    }

    @PutMapping("/users/{id}")
    public User actualizarUsuario(@PathVariable Long id, @RequestBody UpdateUserDTO dto) {
        return adminService.actualizarUsuario(id, dto);
    }
}