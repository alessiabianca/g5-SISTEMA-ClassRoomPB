package br.edu.uepb.classroompb.model;

public class ReprovacaoDisciplina {
  private final String codigoDisciplina;
  private final int totalAprovados;
  private final int totalRecuperacao;
  private final int totalReprovadosPorNota;
  private final int totalReprovadosPorFalta;
  private final int totalRegistros;

  public ReprovacaoDisciplina(
      String codigoDisciplina,
      int totalAprovados,
      int totalRecuperacao,
      int totalReprovadosPorNota,
      int totalReprovadosPorFalta) {
    if (codigoDisciplina == null || codigoDisciplina.isBlank()) {
      throw new IllegalArgumentException("Codigo da disciplina obrigatorio.");
    }
    this.codigoDisciplina = codigoDisciplina;
    this.totalAprovados = Math.max(0, totalAprovados);
    this.totalRecuperacao = Math.max(0, totalRecuperacao);
    this.totalReprovadosPorNota = Math.max(0, totalReprovadosPorNota);
    this.totalReprovadosPorFalta = Math.max(0, totalReprovadosPorFalta);
    this.totalRegistros =
        this.totalAprovados
            + this.totalRecuperacao
            + this.totalReprovadosPorNota
            + this.totalReprovadosPorFalta;
  }

  public String getCodigoDisciplina() {
    return codigoDisciplina;
  }

  public int getTotalAprovados() {
    return totalAprovados;
  }

  public int getTotalRecuperacao() {
    return totalRecuperacao;
  }

  public int getTotalReprovadosPorNota() {
    return totalReprovadosPorNota;
  }

  public int getTotalReprovadosPorFalta() {
    return totalReprovadosPorFalta;
  }

  public int getTotalReprovados() {
    return totalReprovadosPorNota + totalReprovadosPorFalta;
  }

  public int getTotalRegistros() {
    return totalRegistros;
  }

  public double getTaxaReprovacaoPercentual() {
    return calcularPercentual(getTotalReprovados());
  }

  public double getTaxaReprovacaoPorNotaPercentual() {
    return calcularPercentual(totalReprovadosPorNota);
  }

  public double getTaxaReprovacaoPorFaltaPercentual() {
    return calcularPercentual(totalReprovadosPorFalta);
  }

  public boolean isSemReprovacoes() {
    return getTotalReprovados() == 0;
  }

  private double calcularPercentual(int quantidade) {
    if (totalRegistros <= 0) {
      return 0.0;
    }
    return (quantidade * 100.0) / totalRegistros;
  }
}
