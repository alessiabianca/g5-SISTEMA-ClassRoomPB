package br.edu.uepb.classroompb.model;

public class Frequencia {

  public enum TipoFrequencia {
    PRESENCA,
    FALTA
  }

  private String dataAula;
  private String matriculaAluno;
  private String codigoDisciplina;
  private String periodo;
  private TipoFrequencia status;

  public Frequencia(
      String dataAula,
      String matriculaAluno,
      String codigoDisciplina,
      String periodo,
      TipoFrequencia status) {
    this.dataAula = dataAula;
    this.matriculaAluno = matriculaAluno;
    this.codigoDisciplina = codigoDisciplina;
    this.periodo = periodo;
    this.status = status;
  }

  public String getDataAula() {
    return dataAula;
  }

  public void setDataAula(String dataAula) {
    this.dataAula = dataAula;
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

  public TipoFrequencia getStatus() {
    return status;
  }

  public void setStatus(TipoFrequencia status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return dataAula
        + ";"
        + matriculaAluno
        + ";"
        + codigoDisciplina
        + ";"
        + periodo
        + ";"
        + status.name();
  }
}
