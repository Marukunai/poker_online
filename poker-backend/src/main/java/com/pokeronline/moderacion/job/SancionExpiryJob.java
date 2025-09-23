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

    private final SancionService sancionService;

    @Scheduled(fixedDelayString = "${jobs.sanciones.delay:60000}")
    @Transactional
    public void desactivarSancionesCaducadas() {
        int n = sancionService.desactivarCaducadasConDetalle();
        if (n > 0) {
            log.info("Sanciones caducadas desactivadas: {}", n);
        } else {
            log.info("No hay sanciones caducadas para desactivar en este ciclo.");
        }
    }
}