package com.pokeronline.exception;

public class AlreadyInactiveException extends RuntimeException {
    public AlreadyInactiveException(String message) {
        super(message);
    }
}