package cl.duoc.dsy1107.backend.web;

import cl.duoc.dsy1107.backend.gastos.GastoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorDeErrores {

    record Error(String mensaje) {}

    @ExceptionHandler(GastoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Error gastoNoEncontrado(GastoNoEncontradoException ex) {
        return new Error(ex.getMessage());
    }
}