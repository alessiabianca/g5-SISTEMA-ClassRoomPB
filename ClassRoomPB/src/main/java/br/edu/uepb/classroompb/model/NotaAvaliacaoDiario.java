package br.edu.uepb.classroompb.model;

/** Nota de uma avaliacao no extrato de diario do aluno. */
public class NotaAvaliacaoDiario {
  private final Avaliacao avaliacao;
  private final Double valor;

  public NotaAvaliacaoDiario(Avaliacao avaliacao, Double valor) {
    this.avaliacao = avaliacao;
    this.valor = valor;
  }

  public Avaliacao getAvaliacao() {
    return avaliacao;
  }

  public Double getValor() {
    return valor;
  }

  public boolean isLancada() {
    return valor != null;
  }
}
