package cl.duoc.dsy1107.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body para crear un exchange.
 *
 * Ejemplo:
 *   { "nombre": "pagos.exchange", "tipo": "topic", "durable": true }
 */
public record ExchangeRequest(

    @NotBlank(message = "El nombre del exchange es obligatorio")
    @Size(max = 255, message = "El nombre del exchange no puede superar 255 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$",
             message = "El nombre del exchange solo admite letras, números, '.', '_' y '-'")
    String nombre,

    @NotBlank(message = "El tipo de exchange es obligatorio")
    @Pattern(regexp = "^(direct|topic|fanout|headers)$",
             message = "El tipo debe ser direct, topic, fanout o headers")
    String tipo,

    Boolean durable
) {}