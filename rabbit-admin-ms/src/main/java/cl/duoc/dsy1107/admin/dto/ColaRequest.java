package cl.duoc.dsy1107.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Body para crear una cola.
 *
 * Ejemplo:
 *   { "nombre": "pagos.queue", "durable": true,
 *     "deadLetterExchange": "gastos.dlx", "ttlMs": 60000 }
 *
 * Solo "nombre" es obligatorio. durable vale true si no se envía.
 */
public record ColaRequest(

    @NotBlank(message = "El nombre de la cola es obligatorio")
    @Size(max = 255, message = "El nombre de la cola no puede superar 255 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$",
             message = "El nombre de la cola solo admite letras, números, '.', '_' y '-'")
    String nombre,

    Boolean durable,

    // Opcional: si viene, debe ser un nombre válido de exchange
    @Size(max = 255, message = "El dead letter exchange no puede superar 255 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9._-]*$",
             message = "El dead letter exchange solo admite letras, números, '.', '_' y '-'")
    String deadLetterExchange,

    // Opcional: tiempo de vida de los mensajes en milisegundos
    @Positive(message = "El TTL debe ser mayor a 0 milisegundos")
    Integer ttlMs
) {}