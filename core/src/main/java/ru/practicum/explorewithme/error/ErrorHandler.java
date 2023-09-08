package ru.practicum.explorewithme.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.explorewithme.error.model.ErrorResponse;
import ru.practicum.explorewithme.exception.ConditionsNotMetException;
import ru.practicum.explorewithme.exception.IncorrectRequestException;
import ru.practicum.explorewithme.exception.LimitReachedException;
import ru.practicum.explorewithme.exception.NotFoundException;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler({MissingServletRequestParameterException.class,
            ConstraintViolationException.class,
            NumberFormatException.class,
            IncorrectRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(final RuntimeException exception) {
        log.warn(exception.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Incorrectly made request.")
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {
        log.warn(exception.getFieldError().getDefaultMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Incorrectly made request.")
                .message(exception.getFieldError().getDefaultMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler({DataIntegrityViolationException.class,
            LimitReachedException.class,
            ConditionsNotMetException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(final RuntimeException exception) {
        log.warn(exception.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.toString())
                .reason("For the requested operation the conditions are not met.")
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException exception) {
        log.warn(exception.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.toString())
                .reason("The required object was not found.")
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable exception) {
        log.warn(exception.toString());
        return ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .reason("Something went wrong")
                .message(exception.toString())
                .timestamp(LocalDateTime.now())
                .build();
    }
}