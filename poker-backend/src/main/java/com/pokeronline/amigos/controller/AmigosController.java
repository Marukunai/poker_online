package com.pokeronline.amigos.controller;

import com.pokeronline.amigos.dto.*;
import com.pokeronline.amigos.dto.SolicitudAmistadDTO;
import com.pokeronline.amigos.model.EstadoConexion;
import com.pokeronline.amigos.service.AmigosService;
import com.pokeronline.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amigos")
@RequiredArgsConstructor
public class AmigosController {

    private final AmigosService amigosService;
    private final UserService userService;

    // LISTAR AMIGOS
    @GetMapping
    public List<AmigoDTO> listarAmigos(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "TODOS") FiltroAmigos filtro,
            @RequestParam(required = false, defaultValue = "NOMBRE_ASC") OrdenAmigos orden
    ) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        return amigosService.obtenerAmigos(userId, filtro, orden);
    }

    // ENVIAR SOLICITUD DE AMISTAD
    @PostMapping("/solicitudes")
    public SolicitudAmistadDTO enviarSolicitud(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SolicitudAmistadDTO dto
    ) {
        Long remitenteId = userService.getUserIdFromUserDetails(userDetails);
        return amigosService.enviarSolicitud(remitenteId, dto.getDestinatarioId(), dto.getMensaje());
    }

    // ACEPTAR SOLICITUD
    @PostMapping("/solicitudes/{solicitudId}/aceptar")
    public AmistadDTO aceptarSolicitud(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long solicitudId
    ) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        return amigosService.aceptarSolicitud(solicitudId, userId);
    }

    // ELIMINAR AMIGO
    @DeleteMapping("/{amigoId}")
    public String eliminarAmigo(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long amigoId,
            @RequestParam(defaultValue = "false") boolean eliminarHistorialChat
    ) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        amigosService.eliminarAmigo(userId, amigoId, eliminarHistorialChat);
        return "Amigo eliminado correctamente";
    }
}