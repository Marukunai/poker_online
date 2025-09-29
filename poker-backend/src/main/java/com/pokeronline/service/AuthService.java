package com.pokeronline.service;

import com.pokeronline.dto.AuthResponseDTO;
import com.pokeronline.dto.LoginDTO;
import com.pokeronline.dto.RegisterDTO;
import com.pokeronline.model.Role;
import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.pokeronline.util.FiltroPalabrasService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final FiltroPalabrasService filtroPalabrasService;

    public AuthResponseDTO register(RegisterDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        String username = dto.getUsername().trim();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        if (filtroPalabrasService.contienePalabraGrave(username)) {
            // No intentes sancionar a alguien que aún no existe
            throw new RuntimeException("Nombre de usuario prohibido por contenido ofensivo.");
        }
        if (filtroPalabrasService.contienePalabraLeve(username)) {
            throw new RuntimeException("Nombre de usuario inapropiado. Elige otro.");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(dto.getPassword()))
                .fichas(1000)
                .avatarUrl(null)
                .role(Role.USER)
                .esIA(false)
                .nivelBot(null)
                .estiloBot(null)
                .build();

        userRepository.save(user);
        String token = jwtUtils.generateToken(user);

        return AuthResponseDTO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponseDTO login(LoginDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String token = jwtUtils.generateToken(user);
        return AuthResponseDTO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .build();
    }
}