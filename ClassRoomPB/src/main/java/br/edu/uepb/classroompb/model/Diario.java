package br.edu.uepb.classroompb.model;

public class Diario {

  public enum SituacaoDiario {
    ABERTO,
    FECHADO
  }

  private String codigo;
  private String codigoDisciplina;
  private String periodo;
  private String descricao;
  private String matriculaProfessor;
  private String horario;
  private String sala;
  private int cargaHoraria;
  private SituacaoDiario situacao;

  // Construtor padrão para criação inicial (inicia como ABERTO)
  public Diario(
      String codigo,
      String codigoDisciplina,
      String periodo,
      String descricao,
      String matriculaProfessor,
      String horario,
      String sala,
      int cargaHoraria) {
    this.codigo = codigo;
    this.codigoDisciplina = codigoDisciplina;
    this.periodo = periodo;
    this.descricao = descricao;
    this.matriculaProfessor = matriculaProfessor;
    this.horario = horario;
    this.sala = sala;
    this.cargaHoraria = cargaHoraria;
    this.situacao = SituacaoDiario.ABERTO;
  }

  // Construtor completo (utilizado pelo parsing do Repositório ao ler do arquivo)
  public Diario(
      String codigo,
      String codigoDisciplina,
      String periodo,
      String descricao,
      String matriculaProfessor,
      String horario,
      String sala,
      int cargaHoraria,
      SituacaoDiario situacao) {
    this.codigo = codigo;
    this.codigoDisciplina = codigoDisciplina;
    this.periodo = periodo;
    this.descricao = descricao;
    this.matriculaProfessor = matriculaProfessor;
    this.horario = horario;
    this.sala = sala;
    this.cargaHoraria = cargaHoraria;
    this.situacao = situacao;
  }

  // Getters e Setters
  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
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

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public String getMatriculaProfessor() {
    return matriculaProfessor;
  }

  public void setMatriculaProfessor(String matriculaProfessor) {
    this.matriculaProfessor = matriculaProfessor;
  }

  public String getHorario() {
    return horario;
  }

  public void setHorario(String horario) {
    this.horario = horario;
  }

  public String getSala() {
    return sala;
  }

  public void setSala(String sala) {
    this.sala = sala;
  }

  public int getCargaHoraria() {
    return cargaHoraria;
  }

  public void setCargaHoraria(int cargaHoraria) {
    this.cargaHoraria = cargaHoraria;
  }

  public SituacaoDiario getSituacao() {
    return situacao;
  }

  public void setSituacao(SituacaoDiario situacao) {
    this.situacao = situacao;
  }

  public boolean isFechado() {
    return this.situacao == SituacaoDiario.FECHADO;
  }

  @Override
  public String toString() {
    return codigo
        + ";"
        + codigoDisciplina
        + ";"
        + periodo
        + ";"
        + descricao
        + ";"
        + matriculaProfessor
        + ";"
        + horario
        + ";"
        + sala
        + ";"
        + cargaHoraria
        + ";"
        + situacao.name();
  }
}
