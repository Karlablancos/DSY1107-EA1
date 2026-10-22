package cl.duoc.dsy1107.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body para crear o eliminar un binding (exchange -> cola).
 *
 * Ejemplo:
 *   { "exchange": "pagos.exchange", "cola": "pagos.queue", "routingKey": "pago.#" }
 *
 * La routing key puede ir vacía ("") para exchanges fanout, pero no puede faltar.
 * Admite los comodines de topic: '*' (una palabra) y '#' (cero o más palabras).
 */
public record BindingRequest(

    @NotBlank(message = "El exchange es obligatorio")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$",
             message = "El nombre del exchange solo admite letras, números, '.', '_' y '-'")
    String exchange,

    @NotBlank(message = "La cola es obligatoria")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$",
             message = "El nombre de la cola solo admite letras, números, '.', '_' y '-'")
    String cola,

    @NotNull(message = "La routing key es obligatoria (puede ser \"\" para fanout)")
    @Size(max = 255, message = "La routing key no puede superar 255 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9._*#-]*$",
             message = "La routing key solo admite letras, números, '.', '_', '-', '*' y '#'")
    String routingKey
) {}