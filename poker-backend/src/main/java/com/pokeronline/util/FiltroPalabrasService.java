package com.pokeronline.util;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Pattern;

@Service
public class FiltroPalabrasService {

    private static final Set<String> PALABRAS_PROHIBIDAS_GRAVES = Set.of(
            "puta", "puto", "zorra", "polla", "coño", "mierda", "joder", "maricon", "faggot",
            "bitch", "dick", "cunt", "motherfucker", "pene", "anal", "culo", "verga"
    );

    private static final Set<String> PALABRAS_PROHIBIDAS_LEVES = Set.of(
            "idiota", "imbecil", "tonto", "capullo", "inutil", "gilipollas", "payaso", "anormal", "retardado",
            "mongolo", "retrasado", "estupido", "prick", "twat", "moron", "slut", "ass",
            "tetas", "teta", "sin tetas"
    );

    // ====== Checks simples ======

    public boolean contienePalabraGrave(String texto) {
        if (texto == null) return false;
        String normalizado = normalizarTexto(texto);
        return PALABRAS_PROHIBIDAS_GRAVES.stream().anyMatch(normalizado::contains);
    }

    public boolean contienePalabraLeve(String texto) {
        if (texto == null) return false;
        String normalizado = normalizarTexto(texto);
        return PALABRAS_PROHIBIDAS_LEVES.stream().anyMatch(normalizado::contains);
    }

    // ====== Sanitizado / filtrado ======

    /**
     * Reemplaza palabras prohibidas (graves y leves) por asteriscos,
     * respetando mayúsculas/minúsculas y procurando no “romper” palabras vecinas.
     */
    public String sanitizar(String texto) {
        if (texto == null || texto.isBlank()) return texto;

        String result = texto;

        // Unir ambas listas para simplificar
        for (String palabra : unionProhibidas()) {
            // Borde de palabra “suave”: no letra a izquierda/derecha
            String regex = "(?iu)(?<!\\p{L})" + Pattern.quote(palabra) + "(?!\\p{L})";
            String reemplazo = asteriscos(palabra.length());
            result = result.replaceAll(regex, reemplazo);
        }
        return result;
    }

    /** Alias semántico usado por otros servicios (si llaman a filtrarContenido). */
    public String filtrarContenido(String texto) {
        return sanitizar(texto);
    }

    // ====== Helpers ======

    private String normalizarTexto(String texto) {
        // Pasa a minúsculas y quita lo que no sea letra/dígito (con soporte básico de acentos comunes)
        return texto.toLowerCase().replaceAll("[^a-z0-9áéíóúüñ]", "");
    }

    private String asteriscos(int len) {
        if (len <= 0) return "***";
        return "*".repeat(Math.max(3, len)); // al menos 3 asteriscos
    }

    private Set<String> unionProhibidas() {
        return Set.copyOf(
                new java.util.HashSet<>() {{
                    addAll(PALABRAS_PROHIBIDAS_GRAVES);
                    addAll(PALABRAS_PROHIBIDAS_LEVES);
                }}
        );
    }
}