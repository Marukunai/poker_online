package com.pokeronline.service;

import com.pokeronline.bot.DificultadBot;
import com.pokeronline.bot.EstiloBot;
import com.pokeronline.model.*;
import com.pokeronline.repository.MesaRepository;
import com.pokeronline.repository.UserMesaRepository;
import com.pokeronline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MesaPrivadaService {

    private final MesaRepository mesaRepository;
    private final UserRepository userRepository;
    private final UserMesaRepository userMesaRepository;

    public Mesa crearMesaPrivada(String nombre, int maxJugadores, String codigoAcceso, boolean fichasTemporales, int smallBlind, int bigBlind) {
        if (maxJugadores > 8) throw new IllegalArgumentException("No pueden jugar más de 8 personas.");

        Mesa mesa = Mesa.builder()
                .nombre(nombre)
                .privada(true)
                .codigoAcceso(codigoAcceso)
                .fichasTemporales(fichasTemporales)
                .smallBlind(smallBlind)
                .bigBlind(bigBlind)
                .activa(true)
                .fase(Fase.PRE_FLOP)
                .pot(0)
                .maxJugadores(maxJugadores)
                .build();

        return mesaRepository.save(mesa);
    }

    public String unirseAMesaPrivada(String email, String codigoAcceso, int fichasSolicitadas) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Mesa mesa = mesaRepository.findByCodigoAcceso(codigoAcceso)
                .orElseThrow(() -> new RuntimeException("Mesa privada no encontrada"));

        // Comprobar si ya está en esta mesa
        Optional<UserMesa> relacionExistente = userMesaRepository.findByUserAndMesa(user, mesa);
        if (relacionExistente.isPresent()) {
            UserMesa um = relacionExistente.get();
            if (um.isConectado()) {
                return "Ya estás en esta mesa";
            } else {
                um.setConectado(true);
                um.setLastSeen(null);
                userMesaRepository.save(um);
                return "Te has reconectado a la mesa";
            }
        }

        // Desconectarlo de otras mesas activas antes de entrar
        List<UserMesa> activas = userMesaRepository.findByUser(user).stream()
                .filter(um -> um.getMesa().isActiva() && um.isConectado())
                .toList();

        for (UserMesa um : activas) {
            um.setConectado(false);
            um.setLastSeen(new Date());
            userMesaRepository.save(um);
        }

        if (!mesa.getPrivada()) throw new RuntimeException("La mesa no es privada");

        if (mesa.isFichasTemporales() && fichasSolicitadas > 10_000_000)
            throw new RuntimeException("Límite máximo de fichas temporales excedido (10M).");

        int fichas = mesa.isFichasTemporales() ? fichasSolicitadas : Math.min(user.getFichas(), 1000);

        UserMesa userMesa = UserMesa.builder()
                .user(user)
                .mesa(mesa)
                .fichasEnMesa(fichas)
                .fichasIniciales(fichas)
                .fichasTemporalesAsignadas(mesa.isFichasTemporales() ? fichas : null)
                .conectado(true)
                .enJuego(true)
                .build();

        userMesaRepository.save(userMesa);
        return "Unido correctamente a la mesa privada";
    }

    public void addBotAMesaPrivada(String codigoAcceso, int fichasIniciales, DificultadBot dificultad, EstiloBot estiloBot) {
        Mesa mesa = mesaRepository.findByCodigoAcceso(codigoAcceso)
                .orElseThrow(() -> new RuntimeException("Mesa privada no encontrada"));

        long numeroBots = userMesaRepository.findByMesa(mesa).stream()
                .filter(j -> j.getUser().isEsIA())
                .count();

        String nombreBot = "CPU-" + (numeroBots + 1);
        String emailBot = "cpu" + UUID.randomUUID() + "@bot.com";

        User bot = User.builder()
                .email(emailBot)
                .username(nombreBot)
                .fichas(0)
                .esIA(true)
                .nivelBot(dificultad != null ? dificultad : DificultadBot.FACIL)
                .estiloBot(estiloBot != null ? estiloBot : EstiloBot.DEFAULT)
                .build();

        if (!bot.isEsIA() && bot.getNivelBot() != null) {
            throw new IllegalArgumentException("Solo los bots pueden tener un nivel de dificultad.");
        }

        UserMesa userMesa = UserMesa.builder()
                .user(bot)
                .mesa(mesa)
                .fichasEnMesa(fichasIniciales)
                .fichasIniciales(fichasIniciales)
                .fichasTemporalesAsignadas(fichasIniciales)
                .conectado(true)
                .enJuego(true)
                .build();

        userRepository.save(bot);
        userMesaRepository.save(userMesa);
    }
}