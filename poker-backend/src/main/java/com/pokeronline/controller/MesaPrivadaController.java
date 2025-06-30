package com.pokeronline.controller;

import com.pokeronline.model.Mesa;
import com.pokeronline.service.MesaPrivadaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
public class MesaPrivadaController {

    private final MesaPrivadaService mesaPrivadaService;

    @PostMapping("/crearPrivada")
    public Mesa crearMesaPrivada(@RequestParam String nombre,
                                 @RequestParam int maxJugadores,
                                 @RequestParam String codigoAcceso,
                                 @RequestParam boolean fichasTemporales,
                                 @RequestParam int smallBlind,
                                 @RequestParam int bigBlind) {
        return mesaPrivadaService.crearMesaPrivada(nombre, maxJugadores, codigoAcceso, fichasTemporales, smallBlind, bigBlind);
    }

    @PostMapping("/unirsePrivada")
    public ResponseEntity<String> unirsePrivada(@RequestParam String email,
                                                @RequestParam String codigoAcceso,
                                                @RequestParam int fichasSolicitadas) {
        String mensaje = mesaPrivadaService.unirseAMesaPrivada(email, codigoAcceso, fichasSolicitadas);
        return ResponseEntity.ok(mensaje);
    }

    @PostMapping("/agregar-bot")
    public String agregarBot(@RequestParam String codigoAcceso,
                             @RequestParam int fichasIniciales) {
        mesaPrivadaService.addBotAMesaPrivada(codigoAcceso, fichasIniciales);
        return "Bot agregado a la mesa privada.";
    }
}