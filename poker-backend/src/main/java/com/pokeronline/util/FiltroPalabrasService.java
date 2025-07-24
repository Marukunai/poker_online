package com.pokeronline.util;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class FiltroPalabrasService {

    private static final Set<String> PALABRAS_PROHIBIDAS_GRAVES = Set.of(
            "puta", "puto", "zorra", "polla", "coño", "mierda", "joder", "maricon", "faggot",
            "bitch", "dick", "cunt", "motherfucker", "pene", "anal", "culo", "verga"
    );

    private static final Set<String> PALABRAS_PROHIBIDAS_LEVES = Set.of(
            "idiota", "imbecil", "tonto", "capullo", "inutil", "gilipollas", "payaso", "anormal", "retardado",
            "mongolo", "retrasado", "estupido", "prick", "twat", "moron", "slut", "ass", "tetas", "teta", "sin tetas"
    );

    public boolean contienePalabraGrave(String texto) {
        String normalizado = normalizarTexto(texto);
        return PALABRAS_PROHIBIDAS_GRAVES.stream().anyMatch(normalizado::contains);
    }

    public boolean contienePalabraLeve(String texto) {
        String normalizado = normalizarTexto(texto);
        return PALABRAS_PROHIBIDAS_LEVES.stream().anyMatch(normalizado::contains);
    }

    private String normalizarTexto(String texto) {
        return texto.toLowerCase().replaceAll("[^a-zA-Z0-9áéíóúüñ]", "");
    }
}