package com.pokeronline.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", ex.getMessage(),
                        "status", 404,
                        "timestamp", LocalDateTime.now()
                ));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", ex.getMessage(),
                        "status", 401,
                        "timestamp", LocalDateTime.now()
                ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", ex.getMessage(),
                        "status", 400,
                        "timestamp", LocalDateTime.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Error interno del servidor",
                        "message", ex.getMessage(),
                        "status", 500,
                        "timestamp", LocalDateTime.now()
                ));
    }

    @ExceptionHandler(AlreadyInactiveException.class)
    public ResponseEntity<?> handleAlreadyInactive(AlreadyInactiveException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409
                .body(Map.of(
                        "error", ex.getMessage(),
                        "status", 409,
                        "timestamp", LocalDateTime.now()
                ));
    }

    @ExceptionHandler(AlreadyHasAchievementException.class)
    public ResponseEntity<?> handleAlreadyHasAchievement(AlreadyHasAchievementException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", ex.getMessage(),
                        "status", 410,
                        "timestamp", LocalDateTime.now()
                ));
    }
    @ExceptionHandler(ActiveSanctionExistsException.class)
    public ResponseEntity<?> handleActiveSanctionExists(ActiveSanctionExistsException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", ex.getMessage(),
                        "status", 411,
                        "timestamp", LocalDateTime.now()
                ));
    }
}