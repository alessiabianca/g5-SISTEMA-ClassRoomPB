package br.edu.uepb.classroompb.model;

public class OcupacaoVagasTurma {
  private final String codigoDisciplina;
  private final String periodo;
  private final String matriculaProfessor;
  private final String horario;
  private final String sala;
  private final int tetoVagas;
  private final int vagasOcupadas;
  private final int vagasDisponiveis;
  private final int alunosEmEspera;
  private final int ocupacaoExcedente;
  private final double densidadePercentual;

  public OcupacaoVagasTurma(
      String codigoDisciplina,
      String periodo,
      String matriculaProfessor,
      String horario,
      String sala,
      int tetoVagas,
      int vagasOcupadas,
      int alunosEmEspera) {
    this.codigoDisciplina = codigoDisciplina;
    this.periodo = periodo;
    this.matriculaProfessor = matriculaProfessor;
    this.horario = horario;
    this.sala = sala;
    this.tetoVagas = Math.max(0, tetoVagas);
    this.vagasOcupadas = Math.max(0, vagasOcupadas);
    this.alunosEmEspera = Math.max(0, alunosEmEspera);
    this.vagasDisponiveis = Math.max(0, this.tetoVagas - this.vagasOcupadas);
    this.ocupacaoExcedente = Math.max(0, this.vagasOcupadas - this.tetoVagas);
    this.densidadePercentual = calcularDensidadePercentual(this.vagasOcupadas, this.tetoVagas);
  }

  public String getCodigoDisciplina() {
    return codigoDisciplina;
  }

  public String getPeriodo() {
    return periodo;
  }

  public String getMatriculaProfessor() {
    return matriculaProfessor;
  }

  public String getHorario() {
    return horario;
  }

  public String getSala() {
    return sala;
  }

  public int getTetoVagas() {
    return tetoVagas;
  }

  public int getVagasOcupadas() {
    return vagasOcupadas;
  }

  public int getVagasDisponiveis() {
    return vagasDisponiveis;
  }

  public int getAlunosEmEspera() {
    return alunosEmEspera;
  }

  public int getOcupacaoExcedente() {
    return ocupacaoExcedente;
  }

  public double getDensidadePercentual() {
    return densidadePercentual;
  }

  public boolean isLotada() {
    return tetoVagas > 0 && vagasOcupadas >= tetoVagas;
  }

  public boolean isAcimaDoTeto() {
    return ocupacaoExcedente > 0;
  }

  public boolean isComListaEspera() {
    return alunosEmEspera > 0;
  }

  private double calcularDensidadePercentual(int vagasOcupadas, int tetoVagas) {
    if (tetoVagas <= 0) {
      return 0.0;
    }
    return (vagasOcupadas * 100.0) / tetoVagas;
  }
}
