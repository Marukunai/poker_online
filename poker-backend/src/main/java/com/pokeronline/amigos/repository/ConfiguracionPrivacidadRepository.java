package com.pokeronline.amigos.repository;

import com.pokeronline.amigos.model.ConfiguracionPrivacidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConfiguracionPrivacidadRepository extends JpaRepository<ConfiguracionPrivacidad, Long> {

    @Query("""
           select c
           from ConfiguracionPrivacidad c
           where c.usuario.id = :userId
           """)
    Optional<ConfiguracionPrivacidad> findByUserId(Long userId);
}