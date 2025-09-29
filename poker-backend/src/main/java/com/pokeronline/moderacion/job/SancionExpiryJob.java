package com.pokeronline.moderacion.job;

import com.pokeronline.moderacion.service.SancionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SancionExpiryJob {

    private final SancionService sancionService;

    @Scheduled(fixedDelayString = "${jobs.sanciones.delay:60000}")
    public void desactivarSancionesCaducadas() {
        long t0 = System.currentTimeMillis();
        try {
            int n = sancionService.desactivarCaducadasConDetalle();
            if (n > 0) {
                log.info("Sanciones caducadas desactivadas: {} ({} ms)", n, System.currentTimeMillis() - t0);
            } else {
                log.info("No hay sanciones caducadas para desactivar en este ciclo. ({} ms)", System.currentTimeMillis() - t0);
            }
        } catch (Exception e) {
            log.error("Error al desactivar sanciones caducadas", e);
        }
    }
}