package com.codewithsrb.BookManagement.exception;

import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.codewithsrb.BookManagement.model.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.*;

/**
 * This class handles exception centrally for the controller calls.
 */
@ControllerAdvice
@Slf4j
public class HandleException extends ResponseEntityExceptionHandler implements ErrorController {

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .reason(exception.getMessage())
                .httpStatus(resolve(statusCode.value()))
                .statusCode(statusCode.value())
                .build(), statusCode);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        String fieldMessage = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));

        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .httpStatus(HttpStatus.resolve(status.value()))
                .statusCode(status.value())
                .reason(fieldMessage)
                .message(exception.getMessage())
                .build(), status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> illegalArgumentException(IllegalArgumentException exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .httpStatus(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .reason(exception.getMessage())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> resourceNotFoundException(ResourceNotFoundException exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .httpStatus(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .reason(exception.getMessage())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(InvalidClaimException.class)
    public ResponseEntity<Object> invalidClaimException(InvalidClaimException exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .httpStatus(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .reason(exception.getMessage())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Object> tokenExpiredException(TokenExpiredException exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .httpStatus(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .reason(exception.getMessage())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> badCredentialsException(BadCredentialsException exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .httpStatus(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .reason(exception.getMessage())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> apiException(ApiException exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .reason(exception.getMessage())
                .httpStatus(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> exception(Exception exception) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .httpStatus(INTERNAL_SERVER_ERROR)
                .statusCode(INTERNAL_SERVER_ERROR.value())
                .reason(exception.getMessage())
                .build(), INTERNAL_SERVER_ERROR);
    }
}