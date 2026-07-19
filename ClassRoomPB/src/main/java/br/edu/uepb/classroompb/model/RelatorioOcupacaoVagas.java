package br.edu.uepb.classroompb.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RelatorioOcupacaoVagas {
  private final List<OcupacaoVagasTurma> ocupacoes;

  public RelatorioOcupacaoVagas(List<OcupacaoVagasTurma> ocupacoes) {
    List<OcupacaoVagasTurma> base = ocupacoes == null ? new ArrayList<>() : ocupacoes;
    this.ocupacoes = Collections.unmodifiableList(new ArrayList<>(base));
  }

  public List<OcupacaoVagasTurma> getOcupacoes() {
    return ocupacoes;
  }

  public boolean isVazio() {
    return ocupacoes.isEmpty();
  }

  public int getTotalTurmas() {
    return ocupacoes.size();
  }

  public int getTetoTotalVagas() {
    int total = 0;
    for (OcupacaoVagasTurma ocupacao : ocupacoes) {
      total += ocupacao.getTetoVagas();
    }
    return total;
  }

  public int getTotalVagasOcupadas() {
    int total = 0;
    for (OcupacaoVagasTurma ocupacao : ocupacoes) {
      total += ocupacao.getVagasOcupadas();
    }
    return total;
  }

  public int getTotalVagasDisponiveis() {
    int total = 0;
    for (OcupacaoVagasTurma ocupacao : ocupacoes) {
      total += ocupacao.getVagasDisponiveis();
    }
    return total;
  }

  public int getTotalAlunosEmEspera() {
    int total = 0;
    for (OcupacaoVagasTurma ocupacao : ocupacoes) {
      total += ocupacao.getAlunosEmEspera();
    }
    return total;
  }

  public int getTotalOcupacaoExcedente() {
    int total = 0;
    for (OcupacaoVagasTurma ocupacao : ocupacoes) {
      total += ocupacao.getOcupacaoExcedente();
    }
    return total;
  }

  public int getTurmasLotadas() {
    int total = 0;
    for (OcupacaoVagasTurma ocupacao : ocupacoes) {
      if (ocupacao.isLotada()) {
        total++;
      }
    }
    return total;
  }

  public int getTurmasComListaEspera() {
    int total = 0;
    for (OcupacaoVagasTurma ocupacao : ocupacoes) {
      if (ocupacao.isComListaEspera()) {
        total++;
      }
    }
    return total;
  }

  public int getTurmasAcimaDoTeto() {
    int total = 0;
    for (OcupacaoVagasTurma ocupacao : ocupacoes) {
      if (ocupacao.isAcimaDoTeto()) {
        total++;
      }
    }
    return total;
  }

  public double getDensidadeGeralPercentual() {
    int tetoTotal = getTetoTotalVagas();
    if (tetoTotal <= 0) {
      return 0.0;
    }
    return (getTotalVagasOcupadas() * 100.0) / tetoTotal;
  }

  public double getDensidadeMediaPercentual() {
    if (ocupacoes.isEmpty()) {
      return 0.0;
    }

    double soma = 0.0;
    for (OcupacaoVagasTurma ocupacao : ocupacoes) {
      soma += ocupacao.getDensidadePercentual();
    }
    return soma / ocupacoes.size();
  }
}
