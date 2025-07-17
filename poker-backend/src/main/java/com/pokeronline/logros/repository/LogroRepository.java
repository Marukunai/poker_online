package com.pokeronline.logros.repository;

import com.pokeronline.logros.model.Logro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogroRepository extends JpaRepository<Logro, Long> {
    Logro findByNombre(String nombre);
}
