package com.pokeronline.moderacion.job;

import com.pokeronline.moderacion.model.Sancion;
import com.pokeronline.moderacion.repository.SancionRepository;
import com.pokeronline.moderacion.service.SancionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class SancionExpiryJob {

    private final SancionRepository sancionRepository;
    private final SancionService sancionService;

    // Cada minuto
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void desactivarSancionesCaducadas() {
        Date now = new Date();

        var candidatas = sancionService.listarCaducadasPendientes();
        if (candidatas.isEmpty()) {
            log.info("No hay sanciones caducadas para desactivar en este ciclo.");
            return;
        }
        int afectados = sancionService.desactivarCaducadas(now);
        log.info("Sanciones caducadas desactivadas: {} (ids={})",
                afectados,
                candidatas.stream().map(Sancion::getId).toList());
    }
}