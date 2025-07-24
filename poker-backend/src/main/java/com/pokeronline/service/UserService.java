package com.pokeronline.service;

import com.pokeronline.model.User;
import com.pokeronline.moderacion.model.MotivoSancion;
import com.pokeronline.moderacion.model.TipoSancion;
import com.pokeronline.moderacion.service.ModeracionService;
import com.pokeronline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ModeracionService moderacionService;
    private final UserRepository userRepository;

    private static final Set<String> PALABRAS_PROHIBIDAS = Set.of(
            "idiota", "imbecil", "tonto", "estupido", "capullo", "inutil", "gilipollas", "payaso", "anormal",
            "mierda", "joder", "puta", "puto", "coño", "zorra", "polla", "culiao", "cabron", "maricon", "nazi",
            "negro", "sidoso", "mongolo", "retardado", "cancer", "malparido",
            "fuck", "bitch", "asshole", "dick", "faggot", "cunt", "shit", "motherfucker", "whore", "slut"
    );

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public void actualizarPerfil(Long userId, String nuevoUsername) {
        User user = userRepository.findById(userId).orElseThrow();

        if (esNombreInapropiado(nuevoUsername)) {
            moderacionService.registrarSancion(
                    userId,
                    MotivoSancion.NOMBRE_INAPROPIADO,
                    TipoSancion.ADVERTENCIA,
                    "Intento de cambio a nombre de usuario inapropiado",
                    null,
                    null
            );
            throw new RuntimeException("Nombre de usuario inapropiado. Has recibido una advertencia.");
        }

        user.setUsername(nuevoUsername);
        userRepository.save(user);
    }


    private boolean esNombreInapropiado(String username) {
        String texto = username.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
        return PALABRAS_PROHIBIDAS.stream().anyMatch(texto::contains);
    }

    public Optional<User> buscarPorEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existePorId(Long id) {
        return userRepository.existsById(id);
    }

    public Long getUserIdFromUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + userDetails.getUsername()));
    }

    public boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRole().name().equals("ADMIN"))
                .orElse(false);
    }
}