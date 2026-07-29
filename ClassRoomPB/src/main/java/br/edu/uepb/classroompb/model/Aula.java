package br.edu.uepb.classroompb.model;

public class Aula {
  private String id;
  private String codigoDiario;
  private String data;
  private String assunto;
  private int quantidadeAulas;

  // Construtor para criação de nova aula
  public Aula(String id, String codigoDiario, String data, String assunto, int quantidadeAulas) {
    this.id = id;
    this.codigoDiario = codigoDiario;
    this.data = data;
    this.assunto = assunto;
    this.quantidadeAulas = quantidadeAulas;
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

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getAssunto() {
    return assunto;
  }

  public void setAssunto(String assunto) {
    this.assunto = assunto;
  }

  public int getQuantidadeAulas() {
    return quantidadeAulas;
  }

  public void setQuantidadeAulas(int quantidadeAulas) {
    this.quantidadeAulas = quantidadeAulas;
  }

  @Override
  public String toString() {
    return id + ";" + codigoDiario + ";" + data + ";" + assunto + ";" + quantidadeAulas;
  }
}