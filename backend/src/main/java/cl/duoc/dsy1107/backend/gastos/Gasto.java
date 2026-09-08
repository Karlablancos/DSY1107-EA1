package cl.duoc.dsy1107.backend.gastos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "gasto")
@Getter
@Setter
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(nullable = false)
    private int monto;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(length = 500)
    private String comentario;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;

    protected Gasto() {}

    public Gasto(String descripcion, int monto, String categoria,
                 LocalDate fecha, String usuarioId, Instant creadoEn) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.categoria = categoria;
        this.fecha = fecha;
        this.usuarioId = usuarioId;
        this.creadoEn = creadoEn;
    }
}