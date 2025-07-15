package com.pokeronline.torneo.websocket;

import com.pokeronline.torneo.model.Torneo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TorneoEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publicarInicioTorneo(Torneo torneo) {
        try {
            messagingTemplate.convertAndSend("/topic/torneo/" + torneo.getId() + "/inicio", "Torneo iniciado");
        } catch (MessagingException e) {
            log.warn("No se pudo enviar evento de inicio de torneo: {}", e);
        }
    }

    public void publicarAvanceRonda(Long torneoId, int nuevaRonda) {
        try {
            messagingTemplate.convertAndSend("/topic/torneo/" + torneoId + "/ronda", nuevaRonda);
        } catch (MessagingException e) {
            log.warn("No se pudo enviar evento de avance de ronda: {}", e);
        }
    }

    public void publicarCambioRanking(Long torneoId) {
        try {
            messagingTemplate.convertAndSend("/topic/torneo/" + torneoId + "/ranking", "Ranking actualizado");
        } catch (MessagingException e) {
            log.warn("No se pudo enviar evento de ranking: {}", e);
        }
    }

    public void publicarCambioCiegas(Long torneoId, String nivel) {
        try {
            messagingTemplate.convertAndSend("/topic/torneo/" + torneoId + "/ciegas", nivel);
        } catch (MessagingException e) {
            log.warn("No se pudo enviar evento de ciegas: {}", e);
        }
    }

    public void publicarFinalizacionTorneo(Torneo torneo) {
        try {
            messagingTemplate.convertAndSend("/topic/torneo/" + torneo.getId() + "/finalizado", "Torneo finalizado");
        } catch (MessagingException e) {
            log.warn("No se pudo enviar evento de finalización de torneo: {}", e);
        }
    }
}