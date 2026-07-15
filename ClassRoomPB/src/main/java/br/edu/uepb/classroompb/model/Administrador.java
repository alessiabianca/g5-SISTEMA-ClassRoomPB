package br.edu.uepb.classroompb.model;

public class Administrador extends Usuario {
  public Administrador(String matricula, String nome, String email, String senha) {
    super(matricula, nome, email, senha, "ADMINISTRADOR");
  }
}
