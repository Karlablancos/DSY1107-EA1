package cl.duoc.dsy1107.backend.gastos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

public record GastoActualizado(
    @NotBlank String descripcion,
    @PositiveOrZero int monto,
    @NotBlank String categoria,
    @NotNull LocalDate fecha
) {}