package com.pokeronline.service;

import com.pokeronline.exception.ResourceNotFoundException;
import com.pokeronline.model.User;
import com.pokeronline.moderacion.model.MotivoSancion;
import com.pokeronline.moderacion.model.TipoSancion;
import com.pokeronline.moderacion.service.ModeracionService;
import com.pokeronline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.pokeronline.util.FiltroPalabrasService;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ModeracionService moderacionService;
    private final UserRepository userRepository;
    private final FiltroPalabrasService filtroPalabrasService;
    private final PasswordEncoder passwordEncoder;

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // (Legacy) queda por compatibilidad, pero mejor usa actualizarPerfilAutenticado
    public void actualizarPerfil(Long userId, String nuevoNombre) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        aplicarReglasNombre(user, nuevoNombre);
        user.setUsername(nuevoNombre.trim());
        userRepository.save(user);
    }

    // ✅ Actualizar perfil del autenticado (username/avatar)
    public void actualizarPerfilAutenticado(String emailAutenticado, com.pokeronline.dto.UpdateProfileDTO dto) {
        User user = userRepository.findByEmail(emailAutenticado.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (dto.getUsername() != null && !dto.getUsername().trim().isEmpty()) {
            String nuevoNombre = dto.getUsername().trim();

            // Unicidad (case-insensitive)
            userRepository.findByUsername(nuevoNombre).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new RuntimeException("El nombre de usuario ya está en uso");
                }
            });

            aplicarReglasNombre(user, nuevoNombre);
            user.setUsername(nuevoNombre);
        }

        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }

        userRepository.save(user);
    }

    private void aplicarReglasNombre(User user, String nuevoNombre) {
        if (filtroPalabrasService.contienePalabraGrave(nuevoNombre)) {
            moderacionService.registrarSancion(
                    user.getId(),
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
    }

    public void cambiarPassword(String emailAutenticado, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(emailAutenticado.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (Objects.equals(newPassword, currentPassword)) {
            throw new RuntimeException("La nueva contraseña no puede ser igual que la anterior");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("La contraseña actual no es correcta");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("La nueva contraseña debe tener al menos 6 caracteres");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public Optional<User> buscarPorEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase());
    }

    public boolean existePorId(Long id) {
        return userRepository.existsById(id);
    }

    public Long getUserIdFromUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername().toLowerCase())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + userDetails.getUsername()));
    }

    public boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRole().name().equals("ADMIN"))
                .orElse(false);
    }
}