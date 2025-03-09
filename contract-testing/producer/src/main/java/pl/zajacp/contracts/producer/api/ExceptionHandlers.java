package pl.zajacp.contracts.producer.api;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ExceptionHandlers {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<ValidationError>> handleConstraintViolationException(ConstraintViolationException ex) {
        var message = ex.getConstraintViolations().stream()
                .map(cv -> new ValidationError(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    public record ValidationError(String path, String message) {
    }
}
