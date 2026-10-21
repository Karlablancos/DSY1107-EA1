package cl.duoc.dsy1107.backend.mensajeria;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nombres de RabbitMQ leídos desde application.yml (prefijo "mensajeria").
 *
 * Así ningún nombre de exchange o routing key queda escrito "a mano"
 * en las clases de negocio: si mañana cambia un nombre, se cambia
 * solo en el yml.
 *
 *   mensajeria.exchange                    -> exchange()
 *   mensajeria.routing-keys.gasto-creado   -> routingKeys().gastoCreado()
 *   mensajeria.routing-keys.gasto-resuelto -> routingKeys().gastoResuelto()
 */
@ConfigurationProperties(prefix = "mensajeria")
public record MensajeriaProperties(String exchange, RoutingKeys routingKeys) {

    public record RoutingKeys(String gastoCreado, String gastoResuelto) {}
}