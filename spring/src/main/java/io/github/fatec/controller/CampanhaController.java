package io.github.fatec.controller;

import io.github.fatec.controller.adapter.CampanhaControllerAdapter;
import io.github.fatec.controller.request.CampanhaRequest;
import io.github.fatec.controller.request.CampanhaUpdateRequest;
import io.github.fatec.controller.response.CampanhaResponse;
import io.github.fatec.service.CampanhaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Endpoints /campanha (CRUD). Só recebe o HTTP e chama o CampanhaService;
// a tradução das exceções em códigos HTTP fica no GlobalExceptionHandler.
@RestController
@RequestMapping("/campanha")
public class CampanhaController {

    // Cabeçalhos que o gateway acrescenta depois de validar o cookie do usuário
    private static final String USER_ID = "X-User-Id";
    private static final String USER_NOME = "X-User-Nome";

    private final CampanhaService service;

    public CampanhaController(CampanhaService service) {
        this.service = service;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/save")
    public CampanhaResponse save(
            @RequestBody CampanhaRequest request,
            @RequestHeader(USER_ID) String usuarioId,
            @RequestHeader(USER_NOME) String usuarioNome) {
        String nome = URLDecoder.decode(usuarioNome, StandardCharsets.UTF_8);
        return CampanhaControllerAdapter.toResponse(
                service.criar(CampanhaControllerAdapter.cast(request, usuarioId, nome)));
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/update")
    public CampanhaResponse update(
            @RequestBody CampanhaUpdateRequest request,
            @RequestHeader(USER_ID) String usuarioId) {
        return CampanhaControllerAdapter.toResponse(
                service.atualizar(CampanhaControllerAdapter.cast(request), usuarioId));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable String id, @RequestHeader(USER_ID) String usuarioId) {
        service.excluir(id, usuarioId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/all")
    public List<CampanhaResponse> findAll() {
        return service.listar().stream()
                .map(CampanhaControllerAdapter::toResponse)
                .toList();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/minhas")
    public List<CampanhaResponse> minhas(@RequestHeader(USER_ID) String usuarioId) {
        return service.minhas(usuarioId).stream()
                .map(CampanhaControllerAdapter::toResponse)
                .toList();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public CampanhaResponse findById(@PathVariable String id) {
        return CampanhaControllerAdapter.toResponse(service.buscar(id));
    }
}
