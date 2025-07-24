package com.pokeronline.repository;

import com.pokeronline.model.RegistroAbandono;
import com.pokeronline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface RegistroAbandonoRepository extends JpaRepository<RegistroAbandono, Long> {

    List<RegistroAbandono> findByUser(User user);

    @Query("SELECT COUNT(r) FROM RegistroAbandono r WHERE r.user = :user AND r.fecha >= :desde")
    long contarAbandonosRecientes(User user, Date desde);
}