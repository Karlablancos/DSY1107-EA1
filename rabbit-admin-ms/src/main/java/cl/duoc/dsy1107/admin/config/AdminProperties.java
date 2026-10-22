package cl.duoc.dsy1107.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Reglas del administrador leídas desde application.yml (prefijo "admin").
 *
 *   admin.prefijo-reservado -> prefijoReservado()  nombres que RabbitMQ reserva ("amq.")
 *   admin.protegidos        -> protegidos()        topología del sistema que no se puede borrar
 */
@ConfigurationProperties(prefix = "admin")
public record AdminProperties(String prefijoReservado, List<String> protegidos) {

    /** true si el nombre pertenece a la topología del sistema. */
    public boolean esProtegido(String nombre) {
        return protegidos != null && protegidos.contains(nombre);
    }

    /** true si el nombre usa el prefijo reservado por RabbitMQ. */
    public boolean usaPrefijoReservado(String nombre) {
        return nombre != null && nombre.startsWith(prefijoReservado);
    }
}