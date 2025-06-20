package com.pokeronline.controller;

import com.pokeronline.dto.*;
import com.pokeronline.model.*;
import com.pokeronline.repository.AccionPartidaRepository;
import com.pokeronline.repository.MesaRepository;
import com.pokeronline.repository.UserMesaRepository;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.service.BarajaService;
import com.pokeronline.service.EvaluadorManoService;
import com.pokeronline.service.MesaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
public class MesaController {

    private final AccionPartidaRepository accionPartidaRepository;
    private final EvaluadorManoService evaluadorManoService;
    private final MesaRepository mesaRepository;
    private final UserRepository userRepository;
    private final UserMesaRepository userMesaRepository;
    private final BarajaService barajaService;
    private final MesaService mesaService;

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

        // Si ya está en otra(s) mesa(s) activa(s), lo desconectamos
        List<UserMesa> mesasActivas = userMesaRepository.findByUser(user).stream()
                .filter(um -> um.getMesa().isActiva() && um.isConectado())
                .toList();

        boolean fueDesconectadoDeOtraMesa = false;

        for (UserMesa um : mesasActivas) {
            um.setConectado(false);
            um.setLastSeen(new Date());
            userMesaRepository.save(um);
            fueDesconectadoDeOtraMesa = true;
        }

        // Comprobamos que haya espacio en la nueva mesa
        long jugadoresActuales = userMesaRepository.findByMesa(mesa).size();
        if (jugadoresActuales >= mesa.getMaxJugadores()) {
            return ResponseEntity.badRequest().body("La mesa está llena");
        }

        Optional<UserMesa> relacionExistente = userMesaRepository.findByUserAndMesa(user, mesa);
        if (relacionExistente.isPresent()) {
            UserMesa um = relacionExistente.get();
            if (um.isConectado()) {
                return ResponseEntity.badRequest().body("Ya estás en esta mesa");
            } else {
                // Reactivar jugador si estaba desconectado
                um.setConectado(true);
                um.setLastSeen(null); // opcional, limpiar última desconexión
                userMesaRepository.save(um);
                return ResponseEntity.ok("Te has reconectado a la mesa");
            }
        }


        // Creamos la nueva relación
        UserMesa userMesa = UserMesa.builder()
                .user(user)
                .mesa(mesa)
                .fichasEnMesa(100)
                .enJuego(true)
                .conectado(true)
                .build();

        userMesaRepository.save(userMesa);

        String mensaje = fueDesconectadoDeOtraMesa
                ? "Te desconectamos de otra(s) mesa(s) activa(s) y te unimos correctamente a esta mesa."
                : "Unido correctamente a la mesa.";

        return ResponseEntity.ok(mensaje);
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

    @GetMapping("/{mesaId}/ganador")
    public ResponseEntity<?> obtenerGanador(@PathVariable Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        List<UserMesa> jugadores = userMesaRepository.findByMesa(mesa);

        ManoEvaluada ganador = evaluadorManoService.determinarGanador(jugadores, mesa);

        return ResponseEntity.ok(ganador);
    }

    @PostMapping("/{mesaId}/resolver-showdown")
    public ResponseEntity<?> resolverShowdown(@PathVariable Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        if (mesa.getFase() != Fase.SHOWDOWN) {
            return ResponseEntity.badRequest().body("La partida aún no ha llegado a la fase de SHOWDOWN");
        }

        // Nuevo resultado
        ResultadoShowdownInterno resultado = mesaService.resolverShowdown(mesa);

        List<UserMesa> relaciones = userMesaRepository.findByMesa(mesa);
        List<JugadorEnMesaDTO> jugadoresDTO = relaciones.stream()
                .map(um -> new JugadorEnMesaDTO(
                        um.getUser().getId(),
                        um.getUser().getUsername(),
                        um.getUser().getAvatarUrl(),
                        um.getFichasEnMesa()
                )).toList();

        List<JugadorEnMesaDTO> ganadoresDTO = relaciones.stream()
                .filter(um -> resultado.getGanadores().contains(um.getUser()))
                .map(um -> new JugadorEnMesaDTO(
                        um.getUser().getId(),
                        um.getUser().getUsername(),
                        um.getUser().getAvatarUrl(),
                        um.getFichasEnMesa()
                )).toList();

        ResultadoShowdownDTO response = new ResultadoShowdownDTO(
                ganadoresDTO,
                jugadoresDTO,
                resultado.getTipoManoGanadora() != null ? resultado.getTipoManoGanadora().name() : null,
                resultado.getCartasGanadoras()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{mesaId}/nueva-mano")
    public ResponseEntity<?> nuevaMano(@PathVariable Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        mesaService.iniciarNuevaMano(mesa);
        return ResponseEntity.ok("Nueva mano iniciada");
    }

    @PostMapping("/{mesaId}/finalizar-mano")
    public ResponseEntity<?> finalizarMano(@PathVariable Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        String resultado = mesaService.finalizarMano(mesa);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/{mesaId}/reconectar")
    public ResponseEntity<?> reconectar(@PathVariable Long mesaId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body("No autenticado");

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        UserMesa userMesa = userMesaRepository.findByUserAndMesa(user, mesa)
                .orElseThrow(() -> new RuntimeException("No estás en esta mesa"));

        userMesa.setConectado(true);
        userMesaRepository.save(userMesa);

        return ResponseEntity.ok("Reconectado con éxito");
    }

    @PostMapping("/{mesaId}/abandonar")
    public ResponseEntity<?> abandonar(@PathVariable Long mesaId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body("No autenticado");

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        UserMesa userMesa = userMesaRepository.findByUserAndMesa(user, mesa)
                .orElseThrow(() -> new RuntimeException("No estás en esta mesa"));

        userMesa.setConectado(false);
        userMesa.setLastSeen(new Date());
        userMesaRepository.save(userMesa);

        return ResponseEntity.ok("Abandonaste la mesa");
    }

    @PostMapping("/{mesaId}/keepalive")
    public ResponseEntity<?> keepAlive(@PathVariable Long mesaId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body("No autenticado");

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        UserMesa userMesa = userMesaRepository.findByUserAndMesa(user, mesa)
                .orElseThrow(() -> new RuntimeException("No estás en esta mesa"));

        userMesa.setLastSeen(new Date());
        userMesaRepository.save(userMesa);

        return ResponseEntity.ok("OK");
    }

    @GetMapping("/{mesaId}/estado")
    public ResponseEntity<?> estadoMesa(@PathVariable Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        List<String> comunitarias = switch (mesa.getFase()) {
            case PRE_FLOP -> List.of();
            case FLOP -> List.of(mesa.getFlop1(), mesa.getFlop2(), mesa.getFlop3());
            case TURN -> List.of(mesa.getFlop1(), mesa.getFlop2(), mesa.getFlop3(), mesa.getTurn());
            case RIVER, SHOWDOWN -> List.of(mesa.getFlop1(), mesa.getFlop2(), mesa.getFlop3(), mesa.getTurn(), mesa.getRiver());
        };

        List<UserMesa> relaciones = userMesaRepository.findByMesa(mesa);
        List<JugadorMesaCompletoDTO> jugadores = relaciones.stream().map(um ->
                new JugadorMesaCompletoDTO(
                        um.getUser().getId(),
                        um.getUser().getUsername(),
                        um.getUser().getAvatarUrl(),
                        um.getFichasEnMesa(),
                        um.getCarta1(),
                        um.getCarta2()
                )
        ).toList();

        MesaEstadoDTO dto = new MesaEstadoDTO(
                mesa.getId(),
                mesa.getNombre(),
                mesa.isActiva(),
                mesa.getFase(),
                mesa.getPot(),
                comunitarias,
                jugadores
        );

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{mesaId}/acciones")
    public ResponseEntity<?> verAccionesDePartida(@PathVariable Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        List<AccionPartida> acciones = accionPartidaRepository.findByMesaOrderByTimestampAsc(mesa);
        return ResponseEntity.ok(acciones);
    }

    @PostMapping("/{mesaId}/salir")
    public ResponseEntity<?> salirDeMesa(@PathVariable Long mesaId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body("No autenticado");

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        UserMesa userMesa = userMesaRepository.findByUserAndMesa(user, mesa)
                .orElseThrow(() -> new RuntimeException("No estás en esta mesa"));

        userMesaRepository.delete(userMesa);

        return ResponseEntity.ok("Saliste definitivamente de la mesa");
    }
}