package com.pokeronline.amigos.controller;

import com.pokeronline.amigos.dto.CrearMensajeDTO;
import com.pokeronline.amigos.dto.MensajePrivadoDTO;
import com.pokeronline.amigos.service.ChatPrivadoService;
import com.pokeronline.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatPrivadoController {

    private final ChatPrivadoService chatPrivadoService;
    private final UserService userService;

    // ENVIAR MENSAJE
    @PostMapping("/enviar")
    public MensajePrivadoDTO enviar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CrearMensajeDTO dto
    ) {
        Long remitenteId = userService.getUserIdFromUserDetails(userDetails);
        return chatPrivadoService.enviarMensaje(remitenteId, dto);
    }

    // CONVERSACIÓN (paginada)
    @GetMapping("/conversacion/{amigoId}")
    public Page<MensajePrivadoDTO> conversacion(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long amigoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "fechaEnvio,desc") String sort
    ) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        String[] sortParts = sort.split(",");
        Sort s = sortParts.length == 2
                ? Sort.by(Sort.Direction.fromString(sortParts[1]), sortParts[0])
                : Sort.by(Sort.Direction.DESC, "fechaEnvio");
        return chatPrivadoService.obtenerConversacion(userId, amigoId, PageRequest.of(page, size, s));
    }

    // MARCAR MENSAJES COMO LEÍDOS
    @PostMapping("/marcar-leidos/{remitenteId}")
    public String marcarLeidos(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long remitenteId
    ) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        chatPrivadoService.marcarComoLeidos(userId, remitenteId);
        return "Mensajes marcados como leídos";
    }

    // CONTAR NO LEÍDOS
    @GetMapping("/no-leidos/count")
    public int contarNoLeidos(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        return chatPrivadoService.contarNoLeidos(userId);
    }

    // ELIMINAR MENSAJE
    @DeleteMapping("/mensaje/{mensajeId}")
    public String eliminarMensaje(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long mensajeId,
            @RequestParam(defaultValue = "false") boolean paraAmbos
    ) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        chatPrivadoService.eliminarMensaje(mensajeId, userId, paraAmbos);
        return "Mensaje eliminado";
    }
}