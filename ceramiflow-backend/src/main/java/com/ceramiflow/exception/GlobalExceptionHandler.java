package com.ceramiflow.exception;

import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.time.*;
import java.util.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<?> notFound(NotFoundException e) {
        return body(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({ BusinessException.class, IllegalStateException.class, IllegalArgumentException.class })
    ResponseEntity<?> business(RuntimeException e) {
        return body(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler({ OptimisticLockException.class, OptimisticLockingFailureException.class })
    ResponseEntity<?> lock(Exception e) {
        return body(HttpStatus.CONFLICT, "Mẻ gốm vừa được cập nhật bởi một thao tác khác. Vui lòng tải lại và thử lại.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException e) {
        var errors = new LinkedHashMap<String, String>();
        e.getBindingResult().getFieldErrors().forEach(x -> errors.putIfAbsent(x.getField(), x.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("timestamp", LocalDateTime.now(), "status", 400, "error",
                "Validation failed", "fields", errors));
    }

    // Browser EventSource connections are intentionally long-lived. Refreshing or
    // leaving the page can close the socket while Tomcat is writing an SSE event.
    // This is a normal client disconnect, not an application error.
    @ExceptionHandler({ ClientAbortException.class, AsyncRequestNotUsableException.class })
    void clientDisconnected(Exception e) {
        log.debug("Client/SSE đã đóng kết nối: {}", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> generic(Exception e) {
        log.error("Unexpected server error", e);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred");
    }

    private ResponseEntity<?> body(HttpStatus s, String m) {
        return ResponseEntity.status(s).body(Map.of("timestamp", LocalDateTime.now(), "status", s.value(), "error",
                s.getReasonPhrase(), "message", m));
    }
}
