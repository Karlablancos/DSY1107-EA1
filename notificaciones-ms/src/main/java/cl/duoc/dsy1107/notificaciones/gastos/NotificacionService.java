package cl.duoc.dsy1107.notificaciones.gastos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Lógica de negocio de las notificaciones de gasto.
 *
 * NO conoce RabbitMQ: no sabe de colas, ACK ni NACK. Solo valida el evento y
 * envía la notificación. Comunica los problemas lanzando excepciones; quien
 * decide qué hacer con el mensaje es el listener.
 *
 * El envío real (correo) está SIMULADO con un log. Para demostrar el manejo
 * de errores, una descripción que contenga "SIMULAR-FALLA" provoca una
 * falla pasajera del "servidor de correo".
 */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);
    private static final Set<String> TIPOS_VALIDOS = Set.of("CREADO", "RESUELTO");

    /** Reglas de validación. Si algo falla, el mensaje no tiene arreglo. */
    public void validar(GastoEvento evento) {
        if (evento == null) {
            throw new DatosInvalidosException("El evento llegó vacío");
        }
        if (evento.gastoId() == null) {
            throw new DatosInvalidosException("Falta gastoId");
        }
        if (evento.tipo() == null || !TIPOS_VALIDOS.contains(evento.tipo())) {
            throw new DatosInvalidosException("Tipo de evento desconocido: " + evento.tipo());
        }
        if (evento.monto() <= 0) {
            throw new DatosInvalidosException("Monto inválido: " + evento.monto());
        }
    }

    /** "Envía" la notificación. Puede fallar de forma pasajera. */
    public void notificar(GastoEvento evento) {
        if (evento.descripcion() != null && evento.descripcion().contains("SIMULAR-FALLA")) {
            throw new ServicioNoDisponibleException("Servidor de correo no responde (simulado)");
        }

        switch (evento.tipo()) {
            case "CREADO" -> log.info("📧 Aviso al aprobador: nuevo gasto #{} '{}' por ${} ({})",
                evento.gastoId(), evento.descripcion(), evento.monto(), evento.categoria());
            case "RESUELTO" -> log.info("📧 Aviso al usuario {}: su gasto #{} quedó {}",
                evento.usuarioId(), evento.gastoId(), evento.estado());
            default -> { /* no ocurre: el tipo ya fue validado */ }
        }
    }
}