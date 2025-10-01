package com.pokeronline.amigos.repository;

import com.pokeronline.amigos.model.Amistad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AmistadRepository extends JpaRepository<Amistad, Long> {

    @Query("""
           select a
           from Amistad a
           where a.usuario1.id = :userId or a.usuario2.id = :userId
           """)
    List<Amistad> findByUsuario(Long userId);

    @Query("""
           select a
           from Amistad a
           where (a.usuario1.id = :u1 and a.usuario2.id = :u2)
              or (a.usuario1.id = :u2 and a.usuario2.id = :u1)
           """)
    Optional<Amistad> findByUsuarios(Long u1, Long u2);

    @Query("""
           select case when count(a) > 0 then true else false end
           from Amistad a
           where (a.usuario1.id = :u1 and a.usuario2.id = :u2)
              or (a.usuario1.id = :u2 and a.usuario2.id = :u1)
           """)
    boolean existeAmistad(Long u1, Long u2);

    @Query("""
           select case when a.usuario1.id = :userId then a.usuario2.id else a.usuario1.id end
           from Amistad a
           where a.usuario1.id = :userId or a.usuario2.id = :userId
           """)
    List<Long> findAmigosIds(Long userId);

    /** Ayuda en memoria (evita JPQL complejo) */
    default boolean tienenAmigosEnComun(Long a, Long b) {
        List<Long> amigosA = findAmigosIds(a);
        if (amigosA.isEmpty()) return false;
        List<Long> amigosB = findAmigosIds(b);
        amigosA.retainAll(amigosB);
        return !amigosA.isEmpty();
    }
}