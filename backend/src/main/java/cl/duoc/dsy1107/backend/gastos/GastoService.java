package cl.duoc.dsy1107.backend.gastos;

import cl.duoc.dsy1107.backend.mensajeria.GastoEventPublisher; // NUEVO
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class GastoService {

    private final GastoRepository repo;
    private final GastoEventPublisher eventos; // NUEVO: productor de eventos

    // NUEVO: Spring inyecta también el publisher
    public GastoService(GastoRepository repo, GastoEventPublisher eventos) {
        this.repo = repo;
        this.eventos = eventos;
    }

    public List<Gasto> listar() {
        return repo.findAll();
    }

    public Gasto crear(GastoNuevo datos, String usuarioId) {
        var gasto = new Gasto(
            datos.descripcion(),
            datos.monto(),
            datos.categoria(),
            datos.fecha(),
            usuarioId,
            Instant.now()
        );
        Gasto guardado = repo.save(gasto);   // 1) primero se guarda en la BD
        eventos.gastoCreado(guardado);       // 2) NUEVO: después se avisa por RabbitMQ
        return guardado;
    }

    public Gasto actualizar(Long id, GastoActualizado datos) {
        var gasto = repo.findById(id)
            .orElseThrow(() -> new GastoNoEncontradoException(id));
        gasto.setDescripcion(datos.descripcion());
        gasto.setMonto(datos.monto());
        gasto.setCategoria(datos.categoria());
        gasto.setFecha(datos.fecha());
        return repo.save(gasto);
    }

    public void resolver(Long id, GastoResolucion resolucion) {
        var gasto = repo.findById(id)
            .orElseThrow(() -> new GastoNoEncontradoException(id));
        gasto.setEstado(resolucion.estado());
        gasto.setComentario(resolucion.comentario());
        eventos.gastoResuelto(repo.save(gasto)); // NUEVO: avisa que fue aprobado/rechazado
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) throw new GastoNoEncontradoException(id);
        repo.deleteById(id);
    }
}