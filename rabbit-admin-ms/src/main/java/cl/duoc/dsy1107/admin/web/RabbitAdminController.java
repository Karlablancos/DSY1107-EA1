package cl.duoc.dsy1107.admin.web;

import cl.duoc.dsy1107.admin.dto.BindingRequest;
import cl.duoc.dsy1107.admin.dto.ColaRequest;
import cl.duoc.dsy1107.admin.dto.ExchangeRequest;
import cl.duoc.dsy1107.admin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API REST del administrador de RabbitMQ.
 *
 * El controller NO tiene lógica de administración: valida la entrada (@Valid)
 * y delega en RabbitAdminService. Los errores los traduce ManejadorDeErrores.
 *
 *   POST   /api/admin/colas               crear cola
 *   DELETE /api/admin/colas/{nombre}      eliminar cola
 *   POST   /api/admin/exchanges           crear exchange
 *   DELETE /api/admin/exchanges/{nombre}  eliminar exchange
 *   POST   /api/admin/bindings            crear binding
 *   DELETE /api/admin/bindings            eliminar binding (body con exchange, cola y routingKey)
 */
@RestController
@RequestMapping("/api/admin")
public class RabbitAdminController {

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    // ------------------------------- COLAS -------------------------------

    @PostMapping("/colas")
    public ResponseEntity<Map<String, String>> crearCola(@Valid @RequestBody ColaRequest req) {
        String nombre = service.crearCola(req);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("mensaje", "Cola creada", "nombre", nombre));
    }

    @DeleteMapping("/colas/{nombre}")
    public ResponseEntity<Map<String, String>> eliminarCola(@PathVariable String nombre) {
        service.eliminarCola(nombre);
        return ResponseEntity.ok(Map.of("mensaje", "Cola eliminada", "nombre", nombre));
    }

    // ----------------------------- EXCHANGES -----------------------------

    @PostMapping("/exchanges")
    public ResponseEntity<Map<String, String>> crearExchange(@Valid @RequestBody ExchangeRequest req) {
        String nombre = service.crearExchange(req);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("mensaje", "Exchange creado", "nombre", nombre, "tipo", req.tipo()));
    }

    @DeleteMapping("/exchanges/{nombre}")
    public ResponseEntity<Map<String, String>> eliminarExchange(@PathVariable String nombre) {
        service.eliminarExchange(nombre);
        return ResponseEntity.ok(Map.of("mensaje", "Exchange eliminado", "nombre", nombre));
    }

    // ------------------------------ BINDINGS -----------------------------

    @PostMapping("/bindings")
    public ResponseEntity<Map<String, String>> crearBinding(@Valid @RequestBody BindingRequest req) {
        service.crearBinding(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "mensaje", "Binding creado",
            "exchange", req.exchange(), "cola", req.cola(), "routingKey", req.routingKey()));
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Map<String, String>> eliminarBinding(@Valid @RequestBody BindingRequest req) {
        service.eliminarBinding(req);
        return ResponseEntity.ok(Map.of(
            "mensaje", "Binding eliminado",
            "exchange", req.exchange(), "cola", req.cola(), "routingKey", req.routingKey()));
    }
}