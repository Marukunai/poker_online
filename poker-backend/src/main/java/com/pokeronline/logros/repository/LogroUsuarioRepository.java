package com.pokeronline.logros.repository;

import com.pokeronline.logros.model.Logro;
import com.pokeronline.logros.model.LogroUsuario;
import com.pokeronline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LogroUsuarioRepository extends JpaRepository<LogroUsuario, Long> {
    List<LogroUsuario> findByUser(User user);
    boolean existsByUserAndLogro(User user, Logro logro);
    Optional<LogroUsuario> findByUserAndLogro(User user, Logro logro);
    void deleteByUserAndLogro(User user, Logro logro);
}