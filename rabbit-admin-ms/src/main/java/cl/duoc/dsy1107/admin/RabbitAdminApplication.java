package cl.duoc.dsy1107.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Microservicio ADMINISTRADOR de RabbitMQ.
 *
 * Expone una API REST para crear y eliminar colas, exchanges y bindings
 * en tiempo de ejecución, sin reiniciar los demás microservicios.
 * Toda la entrada se valida antes de llegar al broker.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class RabbitAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(RabbitAdminApplication.class, args);
    }
}