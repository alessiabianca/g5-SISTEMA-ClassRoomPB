package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Historico;
import br.edu.uepb.classroompb.model.RelatorioReprovacaoDisciplina;
import br.edu.uepb.classroompb.model.ReprovacaoDisciplina;
import br.edu.uepb.classroompb.model.StatusAcademico;
import br.edu.uepb.classroompb.repository.HistoricoRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class RelatorioReprovacaoDisciplinaService {
  private final HistoricoRepository historicoRepository;

  public RelatorioReprovacaoDisciplinaService(HistoricoRepository historicoRepository) {
    if (historicoRepository == null) {
      throw new IllegalArgumentException("Repositorio de historico obrigatorio.");
    }
    this.historicoRepository = historicoRepository;
  }

  public RelatorioReprovacaoDisciplina gerarRelatorioReprovacaoPorDisciplina() {
    return montarRelatorio(historicoRepository.buscarTodos());
  }

  public RelatorioReprovacaoDisciplina gerarRelatorioReprovacaoPorDisciplina(
      String codigoDisciplina) throws ValidacaoException {
    validarCodigoDisciplina(codigoDisciplina);

    List<Historico> filtrados = new ArrayList<>();
    for (Historico historico : historicoRepository.buscarTodos()) {
      if (equalsIgnoreCase(historico.getCodigoDisciplina(), codigoDisciplina.trim())) {
        filtrados.add(historico);
      }
    }

    return montarRelatorio(filtrados);
  }

  public ReprovacaoDisciplina calcularReprovacaoDisciplina(String codigoDisciplina)
      throws ValidacaoException {
    RelatorioReprovacaoDisciplina relatorio =
        gerarRelatorioReprovacaoPorDisciplina(codigoDisciplina);
    if (relatorio.isVazio()) {
      throw new ValidacaoException(
          "Erro: Nao ha historico consolidado para a disciplina informada.");
    }
    return relatorio.getIndicadores().get(0);
  }

  private RelatorioReprovacaoDisciplina montarRelatorio(List<Historico> historicos) {
    Map<String, ContadorReprovacao> agrupado = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    if (historicos != null) {
      for (Historico historico : historicos) {
        if (historico == null || historico.getStatus() == null) {
          continue;
        }

        String codigoDisciplina = normalizarCodigoDisciplina(historico.getCodigoDisciplina());
        ContadorReprovacao contador =
            agrupado.computeIfAbsent(codigoDisciplina, chave -> new ContadorReprovacao());
        contador.contabilizar(historico.getStatus());
      }
    }

    List<ReprovacaoDisciplina> indicadores = new ArrayList<>();
    for (Map.Entry<String, ContadorReprovacao> entrada : agrupado.entrySet()) {
      indicadores.add(entrada.getValue().toIndicador(entrada.getKey()));
    }
    return new RelatorioReprovacaoDisciplina(indicadores);
  }

  private String normalizarCodigoDisciplina(String codigoDisciplina) {
    return codigoDisciplina.trim().toUpperCase(Locale.ROOT);
  }

  private void validarCodigoDisciplina(String codigoDisciplina) throws ValidacaoException {
    if (codigoDisciplina == null || codigoDisciplina.trim().isEmpty()) {
      throw new ValidacaoException("Erro: O codigo da disciplina deve ser informado.");
    }
  }

  private boolean equalsIgnoreCase(String primeiro, String segundo) {
    return primeiro != null && segundo != null && primeiro.equalsIgnoreCase(segundo);
  }

  private static class ContadorReprovacao {
    private int aprovados;
    private int recuperacao;
    private int reprovadosPorNota;
    private int reprovadosPorFalta;

    private void contabilizar(StatusAcademico status) {
      if (status == StatusAcademico.APROVADO) {
        aprovados++;
      } else if (status == StatusAcademico.RECUPERACAO) {
        recuperacao++;
      } else if (status == StatusAcademico.REPROVADO_NOTA) {
        reprovadosPorNota++;
      } else if (status == StatusAcademico.REPROVADO_FALTA) {
        reprovadosPorFalta++;
      }
    }

    private ReprovacaoDisciplina toIndicador(String codigoDisciplina) {
      return new ReprovacaoDisciplina(
          codigoDisciplina, aprovados, recuperacao, reprovadosPorNota, reprovadosPorFalta);
    }
  }
}
