package cl.duoc.dsy1107.notificaciones.gastos;

import java.time.Instant;

/**
 * Evento de gasto recibido desde RabbitMQ (publicado por el backend).
 *
 * Es una COPIA del contrato del productor: cada microservicio tiene la suya
 * para no depender del código del otro. Los nombres de los campos deben
 * coincidir con el JSON que envía el backend.
 */
public record GastoEvento(
    String tipo,          // "CREADO" o "RESUELTO"
    Long gastoId,
    String descripcion,
    int monto,
    String categoria,
    String estado,        // PENDIENTE / APROBADO / RECHAZADO
    String usuarioId,
    Instant ocurridoEn
) {}