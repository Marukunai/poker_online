package com.pokeronline.service;

import com.pokeronline.dto.LoginDTO;
import com.pokeronline.dto.RegisterDTO;
import com.pokeronline.model.Role;
import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    private static final Set<String> PALABRAS_PROHIBIDAS = Set.of(
            "idiota", "imbecil", "tonto", "estupido", "capullo", "inutil", "gilipollas", "payaso", "anormal",
            "mierda", "joder", "puta", "puto", "coño", "zorra", "polla", "culiao", "cabron", "maricon", "nazi",
            "negro", "sidoso", "mongolo", "retardado", "cancer", "malparido",
            "fuck", "bitch", "asshole", "dick", "faggot", "cunt", "shit", "motherfucker", "whore", "slut"
    );

    public String register(RegisterDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        if (esNombreInapropiado(dto.getUsername())) {
            throw new RuntimeException("El nombre de usuario contiene lenguaje inapropiado");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fichas(1000)
                .avatarUrl(null)
                .role(Role.USER)
                .esIA(false)
                .nivelBot(null)
                .estiloBot(null)
                .build();

        userRepository.save(user);
        return jwtUtils.generateToken(user);
    }

    public String login(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return jwtUtils.generateToken(user);
    }

    private boolean esNombreInapropiado(String username) {
        String normalizado = username.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
        return PALABRAS_PROHIBIDAS.stream().anyMatch(normalizado::contains);
    }
}