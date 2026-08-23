package com.ayshriv.salescrm.common.config;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.resources.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Safety net for malformed JSON request bodies (HTTP 400).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MappingJacksonValue> handleMalformedJson(HttpMessageNotReadableException ex) {
        ApiStatus status = Resources.setStatus(Constants.ERROR, "Malformed JSON request body.", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Resources.formatedResponse(status));
    }

    /**
     * Safety net for HTTP method not supported (HTTP 405).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<MappingJacksonValue> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ApiStatus status = Resources.setStatus(Constants.ERROR, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(Resources.formatedResponse(status));
    }

    /**
     * Safety net for unmapped endpoints / 404 route not found (HTTP 404).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<MappingJacksonValue> handleNotFound(NoResourceFoundException ex) {
        ApiStatus status = Resources.setStatus(Constants.ERROR, "Requested endpoint or resource not found.", null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Resources.formatedResponse(status));
    }

    /**
     * Safety net for security authorization access denied (HTTP 403).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MappingJacksonValue> handleAccessDenied(AccessDeniedException ex) {
        ApiStatus status = Resources.setStatus(Constants.UNAUTHORIZED, "Access denied.", null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Resources.formatedResponse(status));
    }

    /**
     * Safety net for security authentication failures (HTTP 401).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<MappingJacksonValue> handleAuthenticationException(AuthenticationException ex) {
        ApiStatus status = Resources.setStatus(Constants.UNAUTHORIZED, Constants.INVALID_TOKEN, null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Resources.formatedResponse(status));
    }

    /**
     * Last-resort fallback safety net for unexpected runtime exceptions (HTTP 500).
     * Note: Primary business error handling lives inside service layer try/catch blocks returning ApiStatus.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MappingJacksonValue> handleGenericException(Exception ex) {
        ApiStatus status = Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Resources.formatedResponse(status));
    }
}
