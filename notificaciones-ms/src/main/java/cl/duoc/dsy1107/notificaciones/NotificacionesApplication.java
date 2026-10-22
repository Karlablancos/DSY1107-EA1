package cl.duoc.dsy1107.notificaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Microservicio CONSUMIDOR de eventos de gasto.
 *
 * Escucha la cola gastos.notificaciones.queue, procesa cada evento con
 * ACK manual y envía a la DLQ los mensajes que no se pueden procesar.
 * No expone API REST ni usa base de datos.
 */
@SpringBootApplication
@ConfigurationPropertiesScan   // activa las clases @ConfigurationProperties (MensajeriaProperties)
public class NotificacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacionesApplication.class, args);
    }
}