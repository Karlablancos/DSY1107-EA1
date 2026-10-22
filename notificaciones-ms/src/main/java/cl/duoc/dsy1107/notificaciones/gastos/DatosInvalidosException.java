package cl.duoc.dsy1107.notificaciones.gastos;

/**
 * Error NO RECUPERABLE: el mensaje trae datos inválidos (sin id, monto
 * negativo, tipo desconocido...). Reintentar no sirve: fallaría siempre igual.
 *
 * Decisión del consumidor: basicNack(requeue=false) -> va DIRECTO a la DLQ.
 */
public class DatosInvalidosException extends RuntimeException {
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}