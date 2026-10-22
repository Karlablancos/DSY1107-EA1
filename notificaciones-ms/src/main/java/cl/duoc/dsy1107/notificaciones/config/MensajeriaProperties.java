package cl.duoc.dsy1107.notificaciones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nombres de RabbitMQ leídos desde application.yml (prefijo "mensajeria").
 *
 *   mensajeria.exchange         -> exchange()       exchange donde publica el backend
 *   mensajeria.cola             -> cola()           cola principal de este consumidor
 *   mensajeria.binding-key      -> bindingKey()     patrón topic ("gasto.#")
 *   mensajeria.dlx              -> dlx()            Dead Letter Exchange
 *   mensajeria.dlq              -> dlq()            Dead Letter Queue
 *   mensajeria.dlq-routing-key  -> dlqRoutingKey()  routing key hacia la DLQ
 */
@ConfigurationProperties(prefix = "mensajeria")
public record MensajeriaProperties(
    String exchange,
    String cola,
    String bindingKey,
    String dlx,
    String dlq,
    String dlqRoutingKey
) {}