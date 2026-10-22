package cl.duoc.dsy1107.notificaciones.dlq;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * CONSUMIDOR de la Dead Letter Queue (dominio: mensajes fallidos).
 *
 * Registra en el log cada mensaje que no se pudo procesar: su contenido,
 * la cola de origen y el motivo por el que "murió" (rejected, expired,
 * maxlen), datos que RabbitMQ agrega en el header "x-death".
 *
 * Recibe el mensaje CRUDO (sin convertir a GastoEvento): un mensaje que murió
 * por datos inválidos podría no ser un JSON válido, y convertirlo fallaría
 * otra vez.
 *
 * Después de registrarlo hace ACK: el mensaje ya quedó documentado en el log,
 * y dejarlo en la cola solo lo haría reaparecer.
 */
@Component
public class DeadLetterListener {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterListener.class);

    @RabbitListener(id = "gastos-dlq", queues = "${mensajeria.dlq}")
    public void registrar(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        String cuerpo = new String(message.getBody(), StandardCharsets.UTF_8);

        // RabbitMQ agrega el header "x-death" al mover un mensaje al DLX.
        Object xDeath = message.getMessageProperties().getHeaders().get("x-death");
        String colaOrigen = "desconocida";
        String motivo = "desconocido";
        if (xDeath instanceof List<?> muertes && !muertes.isEmpty()
                && muertes.get(0) instanceof Map<?, ?> ultima) {
            colaOrigen = String.valueOf(ultima.get("queue"));
            motivo = String.valueOf(ultima.get("reason"));   // rejected | expired | maxlen
        }

        log.error("☠️ DLQ | colaOrigen={} | motivo={} | contenido={}", colaOrigen, motivo, cuerpo);

        // Ya quedó registrado: se confirma para sacarlo de la DLQ.
        channel.basicAck(tag, false);
    }
}