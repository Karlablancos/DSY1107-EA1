package cl.duoc.dsy1107.backend.gastos;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gastos")
public class GastoController {

    private final GastoService service;

    public GastoController(GastoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Gasto> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Gasto crear(@Valid @RequestBody GastoNuevo datos,
                       @RequestHeader(value = "X-Usuario-Id", defaultValue = "anonimo") String usuarioId) {
        return service.crear(datos, usuarioId);
    }

    @PutMapping("/{id}")
    public Gasto actualizar(@PathVariable Long id,
                            @Valid @RequestBody GastoActualizado datos) {
        return service.actualizar(id, datos);
    }

    @PatchMapping("/{id}/resolucion")
    public void resolver(@PathVariable Long id,
                         @Valid @RequestBody GastoResolucion resolucion) {
        service.resolver(id, resolucion);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}