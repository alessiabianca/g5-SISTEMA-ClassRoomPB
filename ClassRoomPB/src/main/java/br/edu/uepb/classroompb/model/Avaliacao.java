package br.edu.uepb.classroompb.model;

public class Avaliacao {
  private String id;
  private String codigoDiario;
  private String descricao;
  private int etapa;
  private double peso;
  private double notaMaxima;

  // Construtor completo para a avaliação
  public Avaliacao(
      String id, String codigoDiario, String descricao, int etapa, double peso, double notaMaxima) {
    this.id = id;
    this.codigoDiario = codigoDiario;
    this.descricao = descricao;
    this.etapa = etapa;
    this.peso = peso;
    this.notaMaxima = notaMaxima;
  }

  // Getters e Setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCodigoDiario() {
    return codigoDiario;
  }

  public void setCodigoDiario(String codigoDiario) {
    this.codigoDiario = codigoDiario;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public int getEtapa() {
    return etapa;
  }

  public void setEtapa(int etapa) {
    this.etapa = etapa;
  }

  public double getPeso() {
    return peso;
  }

  public void setPeso(double peso) {
    this.peso = peso;
  }

  public double getNotaMaxima() {
    return notaMaxima;
  }

  public void setNotaMaxima(double notaMaxima) {
    this.notaMaxima = notaMaxima;
  }

  @Override
  public String toString() {
    return id + ";" + codigoDiario + ";" + descricao + ";" + etapa + ";" + peso + ";" + notaMaxima;
  }
}
