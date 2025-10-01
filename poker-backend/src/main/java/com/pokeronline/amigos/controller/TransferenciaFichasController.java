package com.pokeronline.amigos.controller;

import com.pokeronline.amigos.dto.LimitesTransferenciaDTO;
import com.pokeronline.amigos.dto.TransferenciaFichasDTO;
import com.pokeronline.amigos.dto.TransferirFichasDTO;
import com.pokeronline.amigos.service.TransferenciaFichasService;
import com.pokeronline.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/amigos/transferencias")
@RequiredArgsConstructor
public class TransferenciaFichasController {

    private final TransferenciaFichasService transferenciaFichasService;
    private final UserService userService;

    // OBTENER LÍMITES DEL DÍA
    @GetMapping("/limites")
    public LimitesTransferenciaDTO limites(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        return transferenciaFichasService.obtenerLimites(userId);
    }

    // TRANSFERIR FICHAS
    @PostMapping
    public TransferenciaFichasDTO transferir(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody TransferirFichasDTO dto
    ) {
        Long remitenteId = userService.getUserIdFromUserDetails(userDetails);
        return transferenciaFichasService.transferirFichas(remitenteId, dto);
    }
}