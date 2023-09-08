package ru.practicum.explorewithme.exception;

public class LimitReachedException extends RuntimeException {
    public LimitReachedException(String message) {
        super(message);
    }
}
