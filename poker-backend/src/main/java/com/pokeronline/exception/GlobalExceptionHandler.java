package com.pokeronline.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /* Utilidad para construir respuestas homogéneas */
    private ResponseEntity<?> build(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(Map.of(
                        "error", message,
                        "status", status.value(),
                        "timestamp", LocalDateTime.now()
                ));
    }

    /* 4xx específicos */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleForbidden(ForbiddenException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<?> handleTooManyRequests(TooManyRequestsException ex, WebRequest request) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler(AlreadyInactiveException.class)
    public ResponseEntity<?> handleAlreadyInactive(AlreadyInactiveException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage()); // 409
    }

    @ExceptionHandler(AlreadyHasAchievementException.class)
    public ResponseEntity<?> handleAlreadyHasAchievement(AlreadyHasAchievementException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage()); // 409
    }

    @ExceptionHandler(ActiveSanctionExistsException.class)
    public ResponseEntity<?> handleActiveSanctionExists(ActiveSanctionExistsException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage()); // 409
    }

    /* Comodines útiles */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex, WebRequest request) {
        // Última red de seguridad para errores de negocio no tipificados
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /* 5xx genérico */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Error interno del servidor",
                        "message", ex.getMessage(),
                        "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "timestamp", LocalDateTime.now()
                ));
    }
}