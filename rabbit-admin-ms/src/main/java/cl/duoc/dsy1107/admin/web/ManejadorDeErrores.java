package cl.duoc.dsy1107.admin.web;

import cl.duoc.dsy1107.admin.service.OperacionNoPermitidaException;
import cl.duoc.dsy1107.admin.service.RecursoNoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

/**
 * Traduce cada tipo de error a una respuesta HTTP clara y en español:
 *
 *   400  datos inválidos (validación del DTO) o JSON mal formado
 *   404  la cola no existe
 *   409  regla de negocio (topología protegida, prefijo "amq.")
 *        o RabbitMQ rechazó la operación
 *   503  RabbitMQ no está disponible
 */
@RestControllerAdvice
public class ManejadorDeErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorDeErrores.class);

    /** Formato único de error para toda la API. */
    public record ErrorRespuesta(int estado, String error, List<String> detalles) {}

    // --------------------------- 400: validación ---------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> datosInvalidos(MethodArgumentNotValidException e) {
        List<String> detalles = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .toList();
        return responder(HttpStatus.BAD_REQUEST, "Datos inválidos", detalles);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorRespuesta> datosInvalidos(HandlerMethodValidationException e) {
        List<String> detalles = e.getAllErrors().stream()
            .map(err -> err.getDefaultMessage())
            .toList();
        return responder(HttpStatus.BAD_REQUEST, "Datos inválidos", detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> jsonInvalido(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "JSON inválido",
            List.of("El cuerpo de la petición no es un JSON válido o falta"));
    }

    // ------------------------- 404 / 409: reglas -------------------------

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(RecursoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, "No encontrado", List.of(e.getMessage()));
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<ErrorRespuesta> noPermitido(OperacionNoPermitidaException e) {
        return responder(HttpStatus.CONFLICT, "Operación no permitida", List.of(e.getMessage()));
    }

    // --------------------------- RabbitMQ ---------------------------

    /** El broker no responde (Docker apagado, red caída...). */
    @ExceptionHandler(AmqpConnectException.class)
    public ResponseEntity<ErrorRespuesta> brokerCaido(AmqpConnectException e) {
        log.error("RabbitMQ no disponible: {}", e.getMessage());
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "RabbitMQ no disponible",
            List.of("No se pudo conectar con el broker. Intente más tarde."));
    }

    /** El broker rechazó la operación (por ejemplo, redeclarar con otros argumentos). */
    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<ErrorRespuesta> brokerRechazo(AmqpException e) {
        String causa = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
        log.warn("RabbitMQ rechazó la operación: {}", causa);
        return responder(HttpStatus.CONFLICT, "RabbitMQ rechazó la operación", List.of(String.valueOf(causa)));
    }

    private ResponseEntity<ErrorRespuesta> responder(HttpStatus estado, String error, List<String> detalles) {
        return ResponseEntity.status(estado).body(new ErrorRespuesta(estado.value(), error, detalles));
    }
}