package cl.duoc.dsy1107.backend.gastos;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class GastoService {

    private final GastoRepository repo;

    public GastoService(GastoRepository repo) {
        this.repo = repo;
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
        return repo.save(gasto);
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
        repo.save(gasto);
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) throw new GastoNoEncontradoException(id);
        repo.deleteById(id);
    }
}