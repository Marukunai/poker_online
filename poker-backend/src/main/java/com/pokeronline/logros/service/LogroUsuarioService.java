package com.pokeronline.logros.service;

import com.pokeronline.logros.dto.LogroUsuarioDTO;
import com.pokeronline.logros.model.Logro;
import com.pokeronline.logros.model.LogroUsuario;
import com.pokeronline.logros.repository.LogroRepository;
import com.pokeronline.logros.repository.LogroUsuarioRepository;
import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogroUsuarioService {

    private final LogroRepository logroRepository;
    private final LogroUsuarioRepository logroUsuarioRepository;
    private final UserRepository userRepository;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public List<LogroUsuarioDTO> obtenerLogrosUsuario(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        List<Logro> todos = logroRepository.findAll();
        List<LogroUsuario> obtenidos = logroUsuarioRepository.findByUser(user);

        Map<Long, LogroUsuario> mapaObtenidos = obtenidos.stream()
                .collect(Collectors.toMap(lu -> lu.getLogro().getId(), lu -> lu));

        return todos.stream().map(logro -> {
            LogroUsuario lu = mapaObtenidos.get(logro.getId());
            return LogroUsuarioDTO.builder()
                    .id(logro.getId())
                    .nombre(logro.getNombre())
                    .descripcion(logro.getDescripcion())
                    .iconoLogro(logro.getIconoLogro())
                    .obtenido(lu != null)
                    .fechaObtencion(lu != null ? sdf.format(lu.getFechaObtencion()) : null)
                    .build();
        }).toList();
    }

    public boolean tieneLogro(User user, Logro logro) {
        return logroUsuarioRepository.existsByUserAndLogro(user, logro);
    }

    public void asignarLogroSiNoTiene(User user, Logro logro) {
        if (!tieneLogro(user, logro)) {
            LogroUsuario logroUsuario = LogroUsuario.builder()
                    .user(user)
                    .logro(logro)
                    .fechaObtencion(new Date())
                    .build();
            logroUsuarioRepository.save(logroUsuario);
        }
    }

    public void asignarLogroPorIdSiNoTiene(Long userId, Long logroId) {
        User user = userRepository.findById(userId).orElseThrow();
        Logro logro = logroRepository.findById(logroId).orElseThrow();
        asignarLogroSiNoTiene(user, logro);
    }

    public void eliminarLogroDeUsuario(Long userId, Long logroId) {
        User user = userRepository.findById(userId).orElseThrow();
        Logro logro = logroRepository.findById(logroId).orElseThrow();
        logroUsuarioRepository.deleteByUserAndLogro(user, logro);
    }
}