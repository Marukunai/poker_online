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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public String register(RegisterDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
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
}