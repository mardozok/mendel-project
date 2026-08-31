package ar.com.bds.handler;

import ar.com.bds.exception.DataBaseOperationException;
import ar.com.bds.exception.InternalGenericException;
import ar.com.bds.exception.TransactionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class TransactionExceptionHandler {

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            TransactionNotFoundException exception) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Invalid transaction request"
        );
    }

    @ExceptionHandler({DataBaseOperationException.class, InternalGenericException.class})
    public ResponseEntity<Map<String, Object>> handleInternal(
            RuntimeException exception) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                exception.getMessage()
        );
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String error,
            String message) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);

        return ResponseEntity.status(status).body(response);
    }
}
