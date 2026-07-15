package br.edu.uepb.classroompb.model;

public class Historico {
  private String matriculaAluno;
  private String codigoDisciplina;
  private String periodo;
  private double mediaFinal;
  private double percentualFrequencia;
  private StatusAcademico status;

  public Historico(
      String matriculaAluno,
      String codigoDisciplina,
      String periodo,
      double mediaFinal,
      double percentualFrequencia,
      StatusAcademico status) {
    this.matriculaAluno = matriculaAluno;
    this.codigoDisciplina = codigoDisciplina;
    this.periodo = periodo;
    this.mediaFinal = mediaFinal;
    this.percentualFrequencia = percentualFrequencia;
    this.status = status;
  }

  public String getMatriculaAluno() {
    return matriculaAluno;
  }

  public void setMatriculaAluno(String matriculaAluno) {
    this.matriculaAluno = matriculaAluno;
  }

  public String getCodigoDisciplina() {
    return codigoDisciplina;
  }

  public void setCodigoDisciplina(String codigoDisciplina) {
    this.codigoDisciplina = codigoDisciplina;
  }

  public String getPeriodo() {
    return periodo;
  }

  public void setPeriodo(String periodo) {
    this.periodo = periodo;
  }

  public double getMediaFinal() {
    return mediaFinal;
  }

  public void setMediaFinal(double mediaFinal) {
    this.mediaFinal = mediaFinal;
  }

  public double getPercentualFrequencia() {
    return percentualFrequencia;
  }

  public void setPercentualFrequencia(double percentualFrequencia) {
    this.percentualFrequencia = percentualFrequencia;
  }

  public StatusAcademico getStatus() {
    return status;
  }

  public void setStatus(StatusAcademico status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return matriculaAluno
        + ";"
        + codigoDisciplina
        + ";"
        + periodo
        + ";"
        + mediaFinal
        + ";"
        + percentualFrequencia
        + ";"
        + status.name();
  }
}
