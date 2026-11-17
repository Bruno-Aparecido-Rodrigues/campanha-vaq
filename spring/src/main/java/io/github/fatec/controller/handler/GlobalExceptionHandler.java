package io.github.fatec.controller.handler;

import io.github.fatec.controller.CampanhaController;
import io.github.fatec.controller.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

// Traduz as exceções lançadas pelo domain em códigos HTTP com { "mensagem": "..." }.
// Antes isso ficava dentro do CampanhaController; agora o controller só cuida dos endpoints.
@RestControllerAdvice(assignableTypes = CampanhaController.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse dadoInvalido(IllegalArgumentException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse jsonInvalido(HttpMessageNotReadableException ex) {
        return new ErrorResponse("Dados em formato inválido. Confira a meta (número) e a data (aaaa-mm-dd).");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse semUsuario(MissingRequestHeaderException ex) {
        return new ErrorResponse("Usuário não identificado. Acesse pelo gateway (porta 8080) estando logado.");
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse naoEhDono(SecurityException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse naoEncontrada(NoSuchElementException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    // Regra de negócio violada, inclusive transição de status inválida na máquina de estado
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse regraDeNegocio(IllegalStateException ex) {
        return new ErrorResponse(ex.getMessage());
    }
}
