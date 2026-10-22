package cl.duoc.dsy1107.notificaciones.gastos;

/**
 * Error RECUPERABLE: falla pasajera de un servicio externo (por ejemplo, el
 * servidor de correo no responde). Puede funcionar si se intenta de nuevo.
 *
 * Decisión del consumidor:
 *   - primera entrega -> basicNack(requeue=true)  (reintentar una vez)
 *   - ya era reintento -> basicNack(requeue=false) (a la DLQ)
 */
public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}