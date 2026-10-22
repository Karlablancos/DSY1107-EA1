package cl.duoc.dsy1107.admin.service;

import cl.duoc.dsy1107.admin.config.AdminProperties;
import cl.duoc.dsy1107.admin.dto.BindingRequest;
import cl.duoc.dsy1107.admin.dto.ColaRequest;
import cl.duoc.dsy1107.admin.dto.ExchangeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.stereotype.Service;

/**
 * Servicio DEDICADO a administrar RabbitMQ (colas, exchanges y bindings).
 *
 * Toda la lógica de administración vive aquí; el controller solo recibe la
 * petición HTTP y delega. Usa AmqpAdmin, la API de Spring para declarar y
 * eliminar elementos del broker en tiempo de ejecución.
 *
 * Reglas que aplica además de la validación de los DTO:
 *   - No permite crear nada con el prefijo reservado "amq.".
 *   - No permite eliminar la topología del sistema (admin.protegidos en el yml).
 *   - Verifica que la cola exista antes de eliminarla o de enlazarla.
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final AmqpAdmin admin;
    private final AdminProperties reglas;

    public RabbitAdminService(AmqpAdmin admin, AdminProperties reglas) {
        this.admin = admin;
        this.reglas = reglas;
    }

    // =============================== COLAS ===============================

    public String crearCola(ColaRequest req) {
        validarPrefijo(req.nombre());
        boolean durable = req.durable() == null || req.durable(); // por defecto: durable

        QueueBuilder builder = durable
            ? QueueBuilder.durable(req.nombre())
            : QueueBuilder.nonDurable(req.nombre());

        if (tieneTexto(req.deadLetterExchange())) {
            builder.deadLetterExchange(req.deadLetterExchange());
        }
        if (req.ttlMs() != null) {
            builder.ttl(req.ttlMs());
        }

        Queue cola = builder.build();
        admin.declareQueue(cola);
        log.info("Cola creada: {} (durable={}, dlx={}, ttlMs={})",
            req.nombre(), durable, req.deadLetterExchange(), req.ttlMs());
        return req.nombre();
    }

    public void eliminarCola(String nombre) {
        validarNoProtegido(nombre);
        exigirColaExistente(nombre);
        admin.deleteQueue(nombre);
        log.info("Cola eliminada: {}", nombre);
    }

    // ============================= EXCHANGES =============================

    public String crearExchange(ExchangeRequest req) {
        validarPrefijo(req.nombre());
        boolean durable = req.durable() == null || req.durable();

        Exchange exchange = new ExchangeBuilder(req.nombre(), req.tipo())
            .durable(durable)
            .build();

        admin.declareExchange(exchange);
        log.info("Exchange creado: {} (tipo={}, durable={})", req.nombre(), req.tipo(), durable);
        return req.nombre();
    }

    public void eliminarExchange(String nombre) {
        validarPrefijo(nombre);       // los "amq.*" son del broker: no se tocan
        validarNoProtegido(nombre);
        admin.deleteExchange(nombre);
        log.info("Exchange eliminado: {}", nombre);
    }

    // ============================== BINDINGS =============================

    public void crearBinding(BindingRequest req) {
        exigirColaExistente(req.cola());
        admin.declareBinding(aBinding(req));
        log.info("Binding creado: {} --[{}]--> {}", req.exchange(), req.routingKey(), req.cola());
    }

    public void eliminarBinding(BindingRequest req) {
        if (reglas.esProtegido(req.exchange()) && reglas.esProtegido(req.cola())) {
            throw new OperacionNoPermitidaException(
                "El binding " + req.exchange() + " -> " + req.cola()
                    + " es parte de la topología del sistema y no se puede eliminar");
        }
        admin.removeBinding(aBinding(req));
        log.info("Binding eliminado: {} --[{}]--> {}", req.exchange(), req.routingKey(), req.cola());
    }

    // ============================== REGLAS ===============================

    private void validarPrefijo(String nombre) {
        if (reglas.usaPrefijoReservado(nombre)) {
            throw new OperacionNoPermitidaException(
                "El prefijo '" + reglas.prefijoReservado() + "' está reservado por RabbitMQ: " + nombre);
        }
    }

    private void validarNoProtegido(String nombre) {
        if (reglas.esProtegido(nombre)) {
            throw new OperacionNoPermitidaException(
                "'" + nombre + "' es parte de la topología del sistema y no se puede eliminar");
        }
    }

    private void exigirColaExistente(String nombre) {
        if (admin.getQueueInfo(nombre) == null) {
            throw new RecursoNoEncontradoException("La cola '" + nombre + "' no existe");
        }
    }

    private Binding aBinding(BindingRequest req) {
        return new Binding(req.cola(), Binding.DestinationType.QUEUE,
            req.exchange(), req.routingKey(), null);
    }

    private boolean tieneTexto(String s) {
        return s != null && !s.isBlank();
    }
}