package cl.duoc.dsy1107.notificaciones.gastos;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * CONSUMIDOR de eventos de gasto (dominio: gastos).
 *
 * Escucha la cola principal con ACK MANUAL (acknowledge-mode: manual en
 * application.yml). Cada mensaje termina SIEMPRE con una respuesta explícita
 * a RabbitMQ, según este árbol de decisiones:
 *
 *   Resultado                                   Respuesta                    Destino
 *   ------------------------------------------  ---------------------------  ------------------
 *   Procesado OK                                basicAck                     se elimina
 *   DatosInvalidosException (no recuperable)    basicNack(requeue=false)     DLQ
 *   ServicioNoDisponibleException, 1ª entrega   basicNack(requeue=true)      vuelve a la cola
 *   ServicioNoDisponibleException, reintento    basicNack(requeue=false)     DLQ
 *   Error inesperado                            basicNack(requeue=false)     DLQ (evita bucle)
 *
 * El reintento es UNO solo: se usa la marca "redelivered" de RabbitMQ para
 * saber si el mensaje ya había sido entregado antes. Así un error que se
 * repite no entra en un bucle infinito de reencolado.
 *
 * La lógica de negocio vive en NotificacionService; esta clase solo decide
 * qué responderle al broker.
 */
@Component
public class GastoNotificacionListener {

    private static final Logger log = LoggerFactory.getLogger(GastoNotificacionListener.class);

    private final NotificacionService notificaciones;

    public GastoNotificacionListener(NotificacionService notificaciones) {
        this.notificaciones = notificaciones;
    }

    /**
     * @param evento  el cuerpo JSON convertido a GastoEvento
     * @param message el mensaje completo (de aquí salen deliveryTag y redelivered)
     * @param channel el canal AMQP, necesario para hacer ACK / NACK manual
     */
    @RabbitListener(id = "gastos-notificaciones", queues = "${mensajeria.cola}")
    public void recibir(GastoEvento evento, Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();         // "n° de seguimiento"
        boolean esReintento = message.getMessageProperties().isRedelivered(); // ¿ya se entregó antes?
        Long gastoId = (evento != null) ? evento.gastoId() : null;

        log.debug("Mensaje recibido gastoId={} tipo={} reintento={}",
            gastoId, evento != null ? evento.tipo() : null, esReintento);

        try {
            notificaciones.validar(evento);
            notificaciones.notificar(evento);

            // ✅ Todo bien: confirmar. RabbitMQ elimina el mensaje.
            channel.basicAck(tag, false);
            log.info("ACK gastoId={}", gastoId);

        } catch (DatosInvalidosException e) {
            // ☠️ No recuperable: reintentar no sirve. Directo a la DLQ.
            channel.basicNack(tag, false, false);
            log.error("NACK -> DLQ (datos inválidos) gastoId={} motivo={}", gastoId, e.getMessage());

        } catch (ServicioNoDisponibleException e) {
            if (!esReintento) {
                // 🔁 Recuperable y es la primera vez: devolver a la cola para un reintento.
                channel.basicNack(tag, false, true);
                log.warn("NACK + requeue (falla pasajera, se reintenta 1 vez) gastoId={} motivo={}",
                    gastoId, e.getMessage());
            } else {
                // ☠️ Ya se reintentó y volvió a fallar: a la DLQ para no entrar en bucle.
                channel.basicNack(tag, false, false);
                log.error("NACK -> DLQ (falló también el reintento) gastoId={} motivo={}",
                    gastoId, e.getMessage());
            }

        } catch (RuntimeException e) {
            // ⚠️ Error no previsto: no sabemos si es recuperable. Se envía a la DLQ
            // (no se reencola) para no arriesgar un bucle infinito.
            channel.basicNack(tag, false, false);
            log.error("NACK -> DLQ (error inesperado) gastoId={}", gastoId, e);
        }
    }
}