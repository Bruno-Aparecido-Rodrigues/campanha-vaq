package io.github.fatec.service;

import io.github.fatec.entity.Campanha;
import io.github.fatec.entity.enumerable.StatusCampanha;
import io.github.fatec.event.CampanhaPublicador;
import io.github.fatec.repository.CampanhaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Regras de negócio da vaquinha: validar, criar, editar, excluir e somar doações.
// É Java puro (sem Spring): depende só das interfaces CampanhaRepository e CampanhaPublicador.
// O Spring cria este objeto no ServiceConfig (módulo spring).
public class CampanhaService {

    private static final int TITULO_MAX = 80;

    private final CampanhaRepository repository;
    private final CampanhaPublicador publicador;

    public CampanhaService(CampanhaRepository repository, CampanhaPublicador publicador) {
        this.repository = repository;
        this.publicador = publicador;
    }

    // ---------------- Consultas ----------------

    public List<Campanha> listar() {
        return repository.findAll();
    }

    public Campanha buscar(String id) {
        return repository.findById(id);
    }

    public List<Campanha> minhas(String usuarioId) {
        return repository.findByCriadorId(usuarioId);
    }

    // ---------------- CRUD (cada operação publica uma mensagem) ----------------

    public Campanha criar(Campanha nova) {
        validar(nova);
        Campanha salva = repository.save(nova);
        publicador.criada(salva);
        return salva;
    }

    public Campanha atualizar(Campanha dados, String usuarioId) {
        Campanha atual = buscarDoDono(dados.id(), usuarioId);

        if (!atual.status().permiteEdicao()) {
            throw new IllegalStateException("Campanhas encerradas não podem ser editadas");
        }
        validar(dados);
        if (dados.meta().compareTo(atual.valorArrecadado()) < 0) {
            throw new IllegalStateException(
                    "A meta não pode ser menor que o valor já arrecadado (R$ " + atual.valorArrecadado() + ")");
        }

        // O novo status (ABERTA ou META_ATINGIDA) é decidido pela própria Campanha, via máquina de estado
        Campanha alterada = atual.editar(dados.titulo(), dados.descricao(), dados.meta(), dados.dataLimite());

        Campanha salva = repository.update(alterada);
        publicador.atualizada(salva);
        return salva;
    }

    public void excluir(String id, String usuarioId) {
        Campanha atual = buscarDoDono(id, usuarioId);

        if (atual.totalDoacoes() > 0) {
            throw new IllegalStateException("Campanhas que já receberam doações não podem ser excluídas");
        }

        Campanha encerrada = atual.encerrar();   // status -> ENCERRADA, ativo = false
        repository.delete(encerrada);
        publicador.excluida(encerrada);
    }

    // Chamado quando chega a mensagem doacao.realizada.
    public void registrarDoacao(String campanhaId, BigDecimal valor) {
        Campanha atualizada = repository.somarDoacao(campanhaId, valor);

        // Regra de negócio: ao alcançar a meta, a vaquinha passa de ABERTA para META_ATINGIDA
        if (atualizada.status() == StatusCampanha.ABERTA && atualizada.metaAtingida()) {
            StatusCampanha novoStatus = atualizada.status().irPara(StatusCampanha.META_ATINGIDA);
            repository.alterarStatus(campanhaId, StatusCampanha.ABERTA, novoStatus);
        }
    }

    // ---------------- Regras auxiliares ----------------

    // Busca a campanha e garante que quem está pedindo é o dono dela.
    private Campanha buscarDoDono(String id, String usuarioId) {
        Campanha campanha = repository.findById(id);
        if (!campanha.criadorId().equals(usuarioId)) {
            throw new SecurityException("Somente quem criou a vaquinha pode alterá-la");
        }
        return campanha;
    }

    private void validar(Campanha c) {
        if (c.titulo() == null || c.titulo().trim().length() < 3) {
            throw new IllegalArgumentException("O título deve ter pelo menos 3 caracteres");
        }
        if (c.titulo().trim().length() > TITULO_MAX) {
            throw new IllegalArgumentException("O título pode ter no máximo " + TITULO_MAX + " caracteres");
        }
        if (c.descricao() == null || c.descricao().trim().length() < 10) {
            throw new IllegalArgumentException("Conte a história da vaquinha na descrição (mínimo de 10 caracteres)");
        }
        if (c.meta() == null || c.meta().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A meta deve ser maior que zero");
        }
        if (c.dataLimite() == null || c.dataLimite().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data limite não pode estar no passado");
        }
    }
}
