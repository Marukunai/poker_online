package com.pokeronline.exception;

public class AlreadyHasAchievementException extends RuntimeException {
    public AlreadyHasAchievementException(String message) { super(message); }
}