package org.generation.italy.fantafootball.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice(assignableTypes = TradeController.class)
public class TradeControllerAdvice {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException exception) {
        String reason = exception.getReason() == null ? "Trade operation failed" : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode())
                .body(Map.of("errorCode", exception.getStatusCode().toString(), "message", reason));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException exception) {
        String message = exception.getMessage() == null ? "Access denied" : exception.getMessage();
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("errorCode", "access_denied", "message", message));
    }
}
