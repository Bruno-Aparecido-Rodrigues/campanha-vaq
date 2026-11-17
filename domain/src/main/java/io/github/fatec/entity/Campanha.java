package io.github.fatec.entity;

import io.github.fatec.entity.enumerable.StatusCampanha;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// Vaquinha: dados do negócio e as mudanças que podem acontecer com ela.
// As mudanças de status passam sempre pela máquina de estado do StatusCampanha.
public record Campanha(
        String id,
        String titulo,
        String descricao,
        BigDecimal meta,
        BigDecimal valorArrecadado,
        Integer totalDoacoes,
        LocalDate dataLimite,
        Instant dataCriacao,
        String criadorId,
        String criadorNome,
        StatusCampanha status,
        boolean ativo
) {

    // Nova vaquinha: o sistema define id, data de criação e status, e ela começa com zero arrecadado.
    public static Campanha nova(String titulo, String descricao, BigDecimal meta, LocalDate dataLimite,
                                String criadorId, String criadorNome) {
        return new Campanha(
                UUID.randomUUID().toString(),
                titulo,
                descricao,
                meta,
                BigDecimal.ZERO,
                0,
                dataLimite,
                Instant.now(),
                criadorId,
                criadorNome,
                StatusCampanha.ABERTA,
                true);
    }

    public boolean metaAtingida() {
        return valorArrecadado.compareTo(meta) >= 0;
    }

    // Edição pelo dono: arrecadado, doações, criador e data de criação não mudam.
    // Se a nova meta já foi alcançada, vai para META_ATINGIDA; se ainda falta, volta para ABERTA.
    public Campanha editar(String novoTitulo, String novaDescricao, BigDecimal novaMeta, LocalDate novaDataLimite) {
        StatusCampanha novoStatus = status.irPara(
                novaMeta.compareTo(valorArrecadado) <= 0 ? StatusCampanha.META_ATINGIDA : StatusCampanha.ABERTA);
        return new Campanha(id, novoTitulo, novaDescricao, novaMeta, valorArrecadado, totalDoacoes,
                novaDataLimite, dataCriacao, criadorId, criadorNome, novoStatus, ativo);
    }

    // Exclusão lógica: a vaquinha vai para o estado final ENCERRADA e fica inativa.
    public Campanha encerrar() {
        return new Campanha(id, titulo, descricao, meta, valorArrecadado, totalDoacoes,
                dataLimite, dataCriacao, criadorId, criadorNome, status.irPara(StatusCampanha.ENCERRADA), false);
    }
}
