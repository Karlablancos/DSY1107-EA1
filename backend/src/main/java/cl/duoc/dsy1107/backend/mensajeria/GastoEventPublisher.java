package cl.duoc.dsy1107.backend.mensajeria;

import cl.duoc.dsy1107.backend.gastos.Gasto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * PRODUCTOR de eventos de gasto.
 *
 * Es la única clase del backend que conoce RabbitMQ. Los servicios de negocio
 * (GastoService) solo llaman a gastoCreado() o gastoResuelto(): no saben de
 * exchanges, routing keys ni RabbitTemplate.
 *
 * Los nombres (exchange y routing keys) salen de application.yml a través de
 * MensajeriaProperties: aquí no hay ningún nombre escrito a mano.
 */
@Component
public class GastoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(GastoEventPublisher.class);

    private final RabbitTemplate rabbit;
    private final MensajeriaProperties props;

    public GastoEventPublisher(RabbitTemplate rabbit, MensajeriaProperties props) {
        this.rabbit = rabbit;
        this.props = props;
    }

    /** Publica con routing key "gasto.creado". */
    public void gastoCreado(Gasto gasto) {
        publicar(props.routingKeys().gastoCreado(), armarEvento("CREADO", gasto));
    }

    /** Publica con routing key "gasto.resuelto" (gasto aprobado o rechazado). */
    public void gastoResuelto(Gasto gasto) {
        publicar(props.routingKeys().gastoResuelto(), armarEvento("RESUELTO", gasto));
    }

    /**
     * Envía el evento al exchange.
     *
     * Si RabbitMQ no está disponible, NO se propaga el error: el gasto ya quedó
     * guardado en la base de datos y la mensajería no debe romper la lógica
     * existente. El fallo queda registrado en el log para revisarlo.
     */
    private void publicar(String routingKey, GastoEvento evento) {
        try {
            rabbit.convertAndSend(props.exchange(), routingKey, evento);
            log.info("Evento publicado [{}] gastoId={}", routingKey, evento.gastoId());
        } catch (AmqpException e) {
            log.error("No se pudo publicar [{}] gastoId={}: {}",
                routingKey, evento.gastoId(), e.getMessage());
        }
    }

    /** Arma el "sobre" con solo los datos que necesita el consumidor. */
    private GastoEvento armarEvento(String tipo, Gasto gasto) {
        return new GastoEvento(
            tipo,
            gasto.getId(),
            gasto.getDescripcion(),
            gasto.getMonto(),
            gasto.getCategoria(),
            gasto.getEstado(),
            gasto.getUsuarioId(),
            Instant.now()
        );
    }
}