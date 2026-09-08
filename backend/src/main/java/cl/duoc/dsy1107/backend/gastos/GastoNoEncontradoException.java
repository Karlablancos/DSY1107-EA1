package cl.duoc.dsy1107.backend.gastos;

public class GastoNoEncontradoException extends RuntimeException {

    public GastoNoEncontradoException(Long id) {
        super("Gasto no encontrado: " + id);
    }
}