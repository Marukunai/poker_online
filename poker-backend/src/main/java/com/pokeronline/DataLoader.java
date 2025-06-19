package com.pokeronline;

import com.pokeronline.model.*;
import com.pokeronline.repository.MesaRepository;
import com.pokeronline.repository.UserMesaRepository;
import com.pokeronline.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

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
        User u1 = User.builder()
                .username("alice")
                .email("alice@email.com")
                .password(passwordEncoder.encode("123"))
                .avatarUrl(null)
                .fichas(1000)
                .role(Role.USER)
                .partidasGanadas(0)
                .build();

        User u2 = User.builder()
                .username("bob")
                .email("bob@email.com")
                .password(passwordEncoder.encode("123"))
                .avatarUrl(null)
                .fichas(1000)
                .role(Role.USER)
                .partidasGanadas(0)
                .build();

        User u3 = User.builder()
                .username("charlie")
                .email("charlie@email.com")
                .password(passwordEncoder.encode("123"))
                .avatarUrl(null)
                .fichas(1000)
                .role(Role.USER)
                .partidasGanadas(0)
                .build();

        userRepository.saveAll(List.of(u1, u2, u3));

        // Crear mesa
        Mesa mesa = Mesa.builder()
                .nombre("Mesa de Prueba")
                .activa(true)
                .smallBlind(5)
                .bigBlind(10)
                .fase(Fase.PRE_FLOP)
                .pot(0)
                .maxJugadores(6)
                .build();
        mesaRepository.save(mesa);

        // Vincular jugadores a la mesa
        UserMesa um1 = UserMesa.builder().user(u1).mesa(mesa).fichasEnMesa(500).enJuego(true).conectado(true).build();
        UserMesa um2 = UserMesa.builder().user(u2).mesa(mesa).fichasEnMesa(500).enJuego(true).conectado(true).build();
        UserMesa um3 = UserMesa.builder().user(u3).mesa(mesa).fichasEnMesa(500).enJuego(true).conectado(true).build();

        userMesaRepository.saveAll(List.of(um1, um2, um3));

        System.out.println("Usuarios, mesa y relaciones creados correctamente.");
    }
}