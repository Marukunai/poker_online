package com.pokeronline.chat.repository;

import com.pokeronline.chat.model.MensajeChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeChatRepository extends JpaRepository<MensajeChat, Long> {
    List<MensajeChat> findByMesaIdOrderByTimestampAsc(Long mesaId);
}