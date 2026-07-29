package br.edu.uepb.classroompb.model;

import java.util.ArrayList;
import java.util.List;

public class Turma {
  private String codigoDisciplina;
  private String periodo;
  private int vagas;
  private int vagasOcupadas;
  public boolean setTurmaEncontrada;

  private List<String> listaEsperaMatriculas = new ArrayList<>();

  // Construtor principal para novas ofertas de turma
  public Turma(String codigoDisciplina, String periodo, int vagas) {
    this.codigoDisciplina = codigoDisciplina;
    this.periodo = periodo;
    this.vagas = vagas;
    this.vagasOcupadas = 0;
  }

  // Construtor completo com vagas ocupadas (utilizado pelo parsing do repositório)
  public Turma(String codigoDisciplina, String periodo, int vagas, int vagasOcupadas) {
    this.codigoDisciplina = codigoDisciplina;
    this.periodo = periodo;
    this.vagas = vagas;
    this.vagasOcupadas = vagasOcupadas;
  }

  public List<String> getListaEsperaMatriculas() {
    return listaEsperaMatriculas;
  }

  public void setListaEsperaMatriculas(List<String> listaEsperaMatriculas) {
    this.listaEsperaMatriculas = listaEsperaMatriculas;
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

  public int getVagas() {
    return vagas;
  }

  public void setVagas(int vagas) {
    this.vagas = vagas;
  }

  public int getVagasOcupadas() {
    return vagasOcupadas;
  }

  public void setVagasOcupadas(int vagasOcupadas) {
    this.vagasOcupadas = vagasOcupadas;
  }

  @Override
  public String toString() {
    return codigoDisciplina
        + ";"
        + periodo
        + ";"
        + vagas
        + ";"
        + vagasOcupadas;
  }
}
