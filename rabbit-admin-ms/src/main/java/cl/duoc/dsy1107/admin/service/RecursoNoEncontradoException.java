package cl.duoc.dsy1107.admin.service;

/**
 * Se pidió operar sobre una cola que no existe en RabbitMQ.
 * Se responde con HTTP 404 Not Found.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}