package cl.duoc.dsy1107.notificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología de RabbitMQ del CONSUMIDOR (notificaciones-ms).
 *
 * Esta clase SOLO declara infraestructura de mensajería; no contiene lógica
 * de negocio. Todos los nombres vienen de application.yml (MensajeriaProperties).
 *
 * RUTA NORMAL
 *   [gastos.exchange : topic] --"gasto.#"--> (gastos.notificaciones.queue)
 *
 * RUTA DE ERROR (mensaje rechazado con basicNack requeue=false)
 *   (gastos.notificaciones.queue) --x-dead-letter-exchange-->
 *   [gastos.dlx : direct] --"gastos.notificaciones.muerto"--> (gastos.notificaciones.dlq)
 *
 * Declarar es idempotente: si un exchange o cola ya existe con los mismos
 * argumentos, RabbitMQ lo reutiliza. Por eso este servicio puede arrancar
 * antes o después que el backend.
 */
@Configuration
public class RabbitConfig {

    // ========================= RUTA NORMAL =========================

    /** 1. Exchange donde publica el backend (mismo nombre y mismos argumentos). */
    @Bean
    TopicExchange gastosExchange(MensajeriaProperties props) {
        return new TopicExchange(props.exchange(), true, false); // durable, no auto-delete
    }

    /**
     * 2. Cola principal, con su "dirección de devolución":
     *    si un mensaje se rechaza sin requeue, RabbitMQ lo mueve solo al DLX
     *    usando la routing key indicada.
     *
     *    Sin TTL a propósito: un mensaje no debe irse a la DLQ solo por
     *    esperar, sino únicamente cuando el consumidor lo rechaza.
     */
    @Bean
    Queue colaNotificaciones(MensajeriaProperties props) {
        return QueueBuilder.durable(props.cola())
            .deadLetterExchange(props.dlx())               // x-dead-letter-exchange
            .deadLetterRoutingKey(props.dlqRoutingKey())   // x-dead-letter-routing-key
            .build();
    }

    /** 3. Binding: todo lo que empiece con "gasto." llega a la cola principal. */
    @Bean
    Binding bindingNotificaciones(Queue colaNotificaciones, TopicExchange gastosExchange,
                                  MensajeriaProperties props) {
        return BindingBuilder.bind(colaNotificaciones)
            .to(gastosExchange)
            .with(props.bindingKey()); // "gasto.#"
    }

    // ========================= RUTA DE ERROR (DLX / DLQ) =========================

    /** 4. Dead Letter Exchange: recibe los mensajes rechazados. */
    @Bean
    DirectExchange deadLetterExchange(MensajeriaProperties props) {
        return new DirectExchange(props.dlx(), true, false);
    }

    /** 5. Dead Letter Queue: guarda los mensajes que no se pudieron procesar. */
    @Bean
    Queue deadLetterQueue(MensajeriaProperties props) {
        return QueueBuilder.durable(props.dlq()).build();
    }

    /** 6. Binding: une el DLX con la DLQ. */
    @Bean
    Binding bindingDeadLetter(Queue deadLetterQueue, DirectExchange deadLetterExchange,
                              MensajeriaProperties props) {
        return BindingBuilder.bind(deadLetterQueue)
            .to(deadLetterExchange)
            .with(props.dlqRoutingKey());
    }

    // ========================= CONVERSIÓN =========================

    /** Los mensajes llegan como JSON y se convierten solos al record GastoEvento. */
    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}