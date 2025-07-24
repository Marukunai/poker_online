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
import com.pokeronline.util.FiltroPalabrasService;
import com.pokeronline.moderacion.model.MotivoSancion;
import com.pokeronline.moderacion.model.TipoSancion;
import com.pokeronline.moderacion.service.ModeracionService;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final FiltroPalabrasService filtroPalabrasService;
    private final ModeracionService moderacionService;

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

        if (filtroPalabrasService.contienePalabraGrave(dto.getUsername())) {
            moderacionService.registrarSancion(
                    null,
                    MotivoSancion.NOMBRE_INAPROPIADO,
                    TipoSancion.ADVERTENCIA,
                    "Intento de registrar nombre ofensivo: " + dto.getUsername(),
                    null,
                    null
            );
            throw new RuntimeException("Nombre de usuario prohibido por contenido ofensivo.");
        }

        if (filtroPalabrasService.contienePalabraLeve(dto.getUsername())) {
            throw new RuntimeException("Nombre de usuario inapropiado. Elige otro.");
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