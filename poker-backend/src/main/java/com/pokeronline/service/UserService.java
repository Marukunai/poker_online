package com.pokeronline.service;

import com.pokeronline.exception.ResourceNotFoundException;
import com.pokeronline.model.User;
import com.pokeronline.moderacion.model.MotivoSancion;
import com.pokeronline.moderacion.model.TipoSancion;
import com.pokeronline.moderacion.service.ModeracionService;
import com.pokeronline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;
import com.pokeronline.util.FiltroPalabrasService;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ModeracionService moderacionService;
    private final UserRepository userRepository;
    private final FiltroPalabrasService filtroPalabrasService;

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public void actualizarPerfil(Long userId, String nuevoNombre) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (filtroPalabrasService.contienePalabraGrave(nuevoNombre)) {
            moderacionService.registrarSancion(
                    userId,
                    MotivoSancion.NOMBRE_INAPROPIADO,
                    TipoSancion.ADVERTENCIA,
                    "Intento de usar nombre ofensivo: " + nuevoNombre,
                    null,
                    null
            );
            throw new RuntimeException("Nombre ofensivo no permitido.");
        }

        if (filtroPalabrasService.contienePalabraLeve(nuevoNombre)) {
            throw new RuntimeException("Nombre inapropiado. Elige otro.");
        }

        user.setUsername(nuevoNombre);
        userRepository.save(user);
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