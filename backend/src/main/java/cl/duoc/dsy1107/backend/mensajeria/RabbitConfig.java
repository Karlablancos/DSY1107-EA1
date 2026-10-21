package cl.duoc.dsy1107.backend.mensajeria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ del lado PRODUCTOR (backend de gastos).
 *
 * Esta clase SOLO configura: no contiene lógica de negocio.
 * Regla de diseño: quien publica declara el exchange; quien consume
 * (notificaciones-ms) declara sus colas, la DLX y la DLQ.
 *
 * Topología:
 *   backend --(gasto.creado / gasto.resuelto)--> [gastos.exchange : topic]
 */
@Configuration
@EnableConfigurationProperties(MensajeriaProperties.class)
public class RabbitConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitConfig.class);

    /**
     * Exchange de tipo TOPIC: permite que un consumidor se suscriba con
     * comodines, por ejemplo "gasto.#" para recibir todos los eventos de gasto.
     * durable = true      -> sobrevive a un reinicio de RabbitMQ
     * autoDelete = false  -> no se borra aunque se queden sin colas enlazadas
     */
    @Bean
    TopicExchange gastosExchange(MensajeriaProperties props) {
        return new TopicExchange(props.exchange(), true, false);
    }

    /**
     * Los eventos viajan como JSON (legible en el panel de RabbitMQ)
     * y no como objetos Java serializados.
     */
    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /**
     * El "cartero" que publica los mensajes.
     * mandatory = true: si el exchange no tiene ninguna cola enlazada para esa
     * routing key, RabbitMQ DEVUELVE el mensaje y queda registrado en el log,
     * en vez de perderse en silencio.
     */
    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setMandatory(true);
        template.setReturnsCallback(devuelto -> log.warn(
            "Mensaje NO enrutado: exchange={} routingKey={} motivo={}",
            devuelto.getExchange(), devuelto.getRoutingKey(), devuelto.getReplyText()));
        return template;
    }
}