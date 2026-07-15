package br.edu.uepb.classroompb.model;

public class Coordenador extends Usuario {
  public Coordenador(String matricula, String nome, String email, String senha) {
    super(matricula, nome, email, senha, "COORDENADOR");
  }
}
