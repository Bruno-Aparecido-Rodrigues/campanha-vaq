package io.github.fatec.controller.adapter;

import io.github.fatec.controller.request.CampanhaRequest;
import io.github.fatec.controller.request.CampanhaUpdateRequest;
import io.github.fatec.controller.response.CampanhaResponse;
import io.github.fatec.entity.Campanha;

//Converte request -> Campanha e Campanha -> response.
public class CampanhaControllerAdapter {
    private CampanhaControllerAdapter() {
    }

    // Nova campanha: quem define id, data de criação, status e arrecadado zero é o domain (Campanha.nova).
    public static Campanha cast(CampanhaRequest request, String usuarioId, String usuarioNome) {
        return Campanha.nova(
                limpar(request.titulo()),
                limpar(request.descricao()),
                request.meta(),
                request.dataLimite(),
                usuarioId,
                usuarioNome);
    }

    //Edição: só leva os campos editáveis os outros ficam nulos porque o service busca os valores verdadeiros no banco
    public static Campanha cast(CampanhaUpdateRequest request) {
        return new Campanha(
                request.id(),
                limpar(request.titulo()),
                limpar(request.descricao()),
                request.meta(),
                null,
                null,
                request.dataLimite(),
                null,
                null,
                null,
                null,
                true);
    }

    public static CampanhaResponse toResponse(Campanha campanha) {
        return new CampanhaResponse(
                campanha.id(),
                campanha.titulo(),
                campanha.descricao(),
                campanha.meta(),
                campanha.valorArrecadado(),
                campanha.totalDoacoes(),
                campanha.dataLimite(),
                campanha.dataCriacao(),
                campanha.criadorId(),
                campanha.criadorNome(),
                campanha.status());
    }

    // Tira espaços sobrando no começo e no fim do texto.
    private static String limpar(String texto) {
        return texto == null ? null : texto.trim();
    }
}