package com.pokeronline.exception;

public class ActiveSanctionExistsException extends RuntimeException {
    public ActiveSanctionExistsException(String message) { super(message); }
}