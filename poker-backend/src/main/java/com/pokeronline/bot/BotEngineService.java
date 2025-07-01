package com.pokeronline.bot;

import com.pokeronline.model.*;

public interface BotEngineService {
    int getApuestaMaxima(Mesa mesa);
    void ejecutarAccionBot(Mesa mesa, User bot, Accion accion, int cantidad);
}