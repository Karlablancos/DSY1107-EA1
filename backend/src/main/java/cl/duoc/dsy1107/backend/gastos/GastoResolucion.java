package cl.duoc.dsy1107.backend.gastos;

import jakarta.validation.constraints.NotBlank;

public record GastoResolucion(
    @NotBlank String estado,
    String comentario
) {}