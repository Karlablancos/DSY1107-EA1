package cl.duoc.dsy1107.admin.service;

/**
 * La petición es válida en formato, pero rompe una regla del administrador:
 * borrar topología protegida o usar el prefijo reservado "amq.".
 * Se responde con HTTP 409 Conflict.
 */
public class OperacionNoPermitidaException extends RuntimeException {
    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}