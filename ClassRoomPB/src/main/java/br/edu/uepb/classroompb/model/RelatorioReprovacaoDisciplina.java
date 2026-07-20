package br.edu.uepb.classroompb.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RelatorioReprovacaoDisciplina {
  private final List<ReprovacaoDisciplina> indicadores;

  public RelatorioReprovacaoDisciplina(List<ReprovacaoDisciplina> indicadores) {
    List<ReprovacaoDisciplina> base = indicadores == null ? new ArrayList<>() : indicadores;
    this.indicadores = Collections.unmodifiableList(new ArrayList<>(base));
  }

  public List<ReprovacaoDisciplina> getIndicadores() {
    return indicadores;
  }

  public boolean isVazio() {
    return indicadores.isEmpty();
  }

  public int getTotalDisciplinas() {
    return indicadores.size();
  }

  public int getTotalRegistrosAnalisados() {
    int total = 0;
    for (ReprovacaoDisciplina indicador : indicadores) {
      total += indicador.getTotalRegistros();
    }
    return total;
  }

  public int getTotalAprovados() {
    int total = 0;
    for (ReprovacaoDisciplina indicador : indicadores) {
      total += indicador.getTotalAprovados();
    }
    return total;
  }

  public int getTotalRecuperacao() {
    int total = 0;
    for (ReprovacaoDisciplina indicador : indicadores) {
      total += indicador.getTotalRecuperacao();
    }
    return total;
  }

  public int getTotalReprovados() {
    int total = 0;
    for (ReprovacaoDisciplina indicador : indicadores) {
      total += indicador.getTotalReprovados();
    }
    return total;
  }

  public int getTotalReprovadosPorNota() {
    int total = 0;
    for (ReprovacaoDisciplina indicador : indicadores) {
      total += indicador.getTotalReprovadosPorNota();
    }
    return total;
  }

  public int getTotalReprovadosPorFalta() {
    int total = 0;
    for (ReprovacaoDisciplina indicador : indicadores) {
      total += indicador.getTotalReprovadosPorFalta();
    }
    return total;
  }

  public double getTaxaReprovacaoGeralPercentual() {
    int totalRegistros = getTotalRegistrosAnalisados();
    if (totalRegistros <= 0) {
      return 0.0;
    }
    return (getTotalReprovados() * 100.0) / totalRegistros;
  }
}
