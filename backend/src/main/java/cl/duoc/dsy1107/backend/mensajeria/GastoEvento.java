package cl.duoc.dsy1107.backend.mensajeria;

import java.time.Instant;

/**
 * Evento que el backend publica en RabbitMQ cuando ocurre algo con un gasto.
 * Viaja como JSON, por ejemplo:
 *
 *   {
 *     "tipo": "CREADO",
 *     "gastoId": 15,
 *     "descripcion": "Taxi reunión cliente",
 *     "monto": 12500,
 *     "categoria": "TRANSPORTE",
 *     "estado": "PENDIENTE",
 *     "usuarioId": "abc-123",
 *     "ocurridoEn": "2026-10-13T03:40:00Z"
 *   }
 *
 * Es un contrato entre microservicios: solo lleva lo que el consumidor
 * necesita, NO la entidad JPA completa (así el consumidor no depende de
 * la base de datos del backend).
 */
public record GastoEvento(
    String tipo,          // "CREADO" o "RESUELTO"
    Long gastoId,
    String descripcion,
    int monto,
    String categoria,
    String estado,        // PENDIENTE / APROBADO / RECHAZADO
    String usuarioId,
    Instant ocurridoEn    // cuándo ocurrió el evento
) {}