package com.pokeronline.amigos.job;

import com.pokeronline.amigos.service.InvitacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvitacionExpiryJob {

    private final InvitacionService invitacionService;

    @Scheduled(fixedDelayString = "${jobs.invitaciones.delay:600000}") // Cada 10 min
    public void expirarPendientes() {
        int n = invitacionService.expirarPendientes();
        if (n > 0) log.info("Invitaciones expiradas: {}", n);
    }
}
