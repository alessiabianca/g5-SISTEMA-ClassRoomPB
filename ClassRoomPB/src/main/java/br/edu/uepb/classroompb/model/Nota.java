package br.edu.uepb.classroompb.model;

/**
 * US34 — Representa o registro de notas de um aluno em uma disciplina/período. Suporta até 3
 * avaliações. Valores negativos (-1) indicam avaliação não realizada.
 */
public class Nota {
  private String matriculaAluno;
  private String codigoDisciplina;
  private String periodo;
  private double nota1;
  private double nota2;
  private double nota3;

  public Nota(
      String matriculaAluno,
      String codigoDisciplina,
      String periodo,
      double nota1,
      double nota2,
      double nota3) {
    this.matriculaAluno = matriculaAluno;
    this.codigoDisciplina = codigoDisciplina;
    this.periodo = periodo;
    this.nota1 = nota1;
    this.nota2 = nota2;
    this.nota3 = nota3;
  }

  public Nota(
      String matriculaAluno, String codigoDisciplina, String periodo, double nota1, double nota2) {
    this(matriculaAluno, codigoDisciplina, periodo, nota1, nota2, -1);
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

  public double getNota1() {
    return nota1;
  }

  public void setNota1(double nota1) {
    this.nota1 = nota1;
  }

  public double getNota2() {
    return nota2;
  }

  public void setNota2(double nota2) {
    this.nota2 = nota2;
  }

  public double getNota3() {
    return nota3;
  }

  public void setNota3(double nota3) {
    this.nota3 = nota3;
  }

  public double getNotaEtapa1() {
    return nota1;
  }

  public double getNotaEtapa2() {
    return nota2;
  }

  /**
   * Converte o objeto para o formato de persistência em arquivo plano (CSV com ";"). Segue o mesmo
   * padrão adotado pelos demais modelos do projeto.
   */
  @Override
  public String toString() {
    return matriculaAluno
        + ";"
        + codigoDisciplina
        + ";"
        + periodo
        + ";"
        + nota1
        + ";"
        + nota2
        + ";"
        + nota3;
  }
}
