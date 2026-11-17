package io.github.fatec.repository;

import io.github.fatec.entity.Campanha;
import io.github.fatec.entity.enumerable.StatusCampanha;

import java.math.BigDecimal;
import java.util.List;

// Contrato do banco de vaquinhas, quem implementa é o CampanhaRepositoryImpl (módulo spring).
public interface CampanhaRepository {
    Campanha save(Campanha campanha);
    Campanha update(Campanha campanha);
    void delete(Campanha campanhaEncerrada);
    List<Campanha> findAll();
    Campanha findById(String id);
    List<Campanha> findByCriadorId(String criadorId);
    // Soma a doação de forma atômica ($inc) e devolve a campanha já com o novo total.
    Campanha somarDoacao(String campanhaId, BigDecimal valor);
    // Troca o status só se ele ainda for o esperado. Devolve false se outro processo já trocou.
    boolean alterarStatus(String campanhaId, StatusCampanha statusAtual, StatusCampanha novoStatus);
}
