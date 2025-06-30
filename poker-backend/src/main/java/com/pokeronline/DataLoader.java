package com.pokeronline;

import com.pokeronline.model.*;
import com.pokeronline.repository.MesaRepository;
import com.pokeronline.repository.UserMesaRepository;
import com.pokeronline.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader {

    private final UserRepository userRepository;
    private final MesaRepository mesaRepository;
    private final UserMesaRepository userMesaRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        if (userRepository.findByEmail("alice@email.com").isPresent()) {
            System.out.println("Los datos ya existen. No se volverán a insertar.");
            return;
        }

        // Crear usuarios
        User alice = crearUsuario("alice", "alice@email.com", 5000);
        User bob = crearUsuario("bob", "bob@email.com", 8000);
        User charlie = crearUsuario("charlie", "charlie@email.com", 15000);
        User dan = crearUsuario("dan", "dan@email.com", 3000);
        User eve = crearUsuario("eve", "eve@email.com", 10000);
        User frank = crearUsuario("frank", "frank@email.com", 20000);

        userRepository.saveAll(List.of(alice, bob, charlie, dan, eve, frank));

        // Crear mesas
        Mesa mesaBasica = crearMesa("Mesa Básica", 5, 10, 6);
        Mesa mesaIntermedia = crearMesa("Mesa Intermedia", 25, 50, 6);
        Mesa mesaAvanzada = crearMesa("Mesa High Rollers", 100, 200, 8);

        mesaRepository.saveAll(List.of(mesaBasica, mesaIntermedia, mesaAvanzada));

        // Añadir jugadores a las mesas
        userMesaRepository.saveAll(Arrays.asList(
                vincularUsuarioMesa(alice, mesaBasica, 1000),
                vincularUsuarioMesa(bob, mesaBasica, 1000),
                vincularUsuarioMesa(charlie, mesaBasica, 1000),

                vincularUsuarioMesa(dan, mesaIntermedia, 3000),
                vincularUsuarioMesa(eve, mesaIntermedia, 3000),

                vincularUsuarioMesa(frank, mesaAvanzada, 5000)
        ));

        System.out.println("Usuarios, mesas y relaciones cargados exitosamente.");
    }

    private User crearUsuario(String username, String email, int fichas) {
        return User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("123"))
                .avatarUrl(null)
                .fichas(fichas)
                .role(Role.USER)
                .partidasGanadas(0)
                .esIA(false)
                .build();
    }

    private Mesa crearMesa(String nombre, int sb, int bb, int max) {
        return Mesa.builder()
                .nombre(nombre)
                .activa(true)
                .smallBlind(sb)
                .bigBlind(bb)
                .fase(Fase.PRE_FLOP)
                .pot(0)
                .maxJugadores(max)
                .build();
    }

    private UserMesa vincularUsuarioMesa(User user, Mesa mesa, int fichas) {
        return UserMesa.builder()
                .user(user)
                .mesa(mesa)
                .fichasEnMesa(fichas)
                .fichasDisponibles(fichas)
                .fichasIniciales(fichas)
                .enJuego(true)
                .conectado(true)
                .lastSeen(new Date())
                .build();
    }
}