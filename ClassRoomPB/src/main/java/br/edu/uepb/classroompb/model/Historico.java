package br.edu.uepb.classroompb.model;

public class Historico {
  private String matriculaAluno;
  private String periodo;
  private String codigoDisciplina;
  private String matriculaProfessor;
  private double mediaFinal;
  private double percentualFrequencia;
  private StatusAcademico status;

  public Historico(
      String matriculaAluno,
      String periodo,
      String codigoDisciplina,
      String matriculaProfessor,
      double mediaFinal,
      double percentualFrequencia,
      StatusAcademico status) {
    this.matriculaAluno = validarCampo(matriculaAluno, "matricula do aluno");
    this.periodo = validarCampo(periodo, "periodo");
    this.codigoDisciplina = validarCampo(codigoDisciplina, "codigo da disciplina");
    this.matriculaProfessor = validarCampo(matriculaProfessor, "matricula do professor");
    this.mediaFinal = mediaFinal;
    this.percentualFrequencia = percentualFrequencia;
    if (status == null) {
      throw new IllegalArgumentException("Situacao academica obrigatoria.");
    }
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

  public String getMatriculaProfessor() {
    return matriculaProfessor;
  }

  public void setMatriculaProfessor(String matriculaProfessor) {
    this.matriculaProfessor = matriculaProfessor;
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
    return periodo
        + ";"
        + codigoDisciplina
        + ";"
        + matriculaProfessor
        + ";"
        + mediaFinal
        + ";"
        + percentualFrequencia
        + ";"
        + status.name()
        + ";"
        + matriculaAluno;
  }

  public static Historico fromString(String linha) {
    String[] partes = linha.split(";");
    if (partes.length == 7) {
      return new Historico(
          partes[6],
          partes[0],
          partes[1],
          partes[2],
          Double.parseDouble(partes[3]),
          Double.parseDouble(partes[4]),
          StatusAcademico.valueOf(partes[5]));
    }

    if (partes.length == 6) {
      return new Historico(
          partes[0],
          partes[2],
          partes[1],
          "NAO_INFORMADO",
          Double.parseDouble(partes[3]),
          Double.parseDouble(partes[4]),
          StatusAcademico.valueOf(partes[5]));
    }

    throw new IllegalArgumentException("Registro de historico invalido: " + linha);
  }

  private static String validarCampo(String valor, String nomeCampo) {
    if (valor == null || valor.isBlank() || valor.contains(";")) {
      throw new IllegalArgumentException("Campo obrigatorio invalido: " + nomeCampo);
    }
    return valor;
  }
}
