package com.cooperativa.erp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para controladores REST.
 * Captura excepciones específicas y devuelve respuestas JSON estandarizadas.
 */
@ControllerAdvice // Indica que esta clase asesora a los controladores
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    /**
     * Maneja IllegalArgumentException (y sus subclases).
     * Devuelve un código 400 Bad Request.
     * @param ex La excepción capturada.
     * @param request La solicitud web actual.
     * @return ResponseEntity con el cuerpo del error y el estado HTTP.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        log.warn("Error de solicitud inválida: {}", ex.getMessage()); // Logueamos como warning

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage()); // Usamos el mensaje de nuestra excepción
        body.put("path", request.getDescription(false).replace("uri=", "")); // Obtiene la URI

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Manejador genérico para otras excepciones no controladas explícitamente.
     * Devuelve un código 500 Internal Server Error.
     * Es importante loguear estas como ERROR para investigarlas.
     * @param ex La excepción capturada.
     * @param request La solicitud web actual.
     * @return ResponseEntity con el cuerpo del error y el estado HTTP.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(
            Exception ex, WebRequest request) {

        log.error("Error interno inesperado: ", ex); // Logueamos como error con stack trace

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "Ocurrió un error inesperado en el servidor."); // Mensaje genérico al cliente
        body.put("path", request.getDescription(false).replace("uri=", ""));

        // NO incluir ex.getMessage() aquí para no exponer detalles internos en errores 500.
        // Los detalles están en el log del servidor.

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

