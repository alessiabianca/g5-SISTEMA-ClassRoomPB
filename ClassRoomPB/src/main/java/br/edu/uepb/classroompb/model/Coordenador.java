package br.edu.uepb.classroompb.model;

public class Coordenador extends Usuario {
  public Coordenador(String matricula, String nome, String email, String senha) {
    this(matricula, nome, email, senha, null);
  }

  public Coordenador(
      String matricula, String nome, String email, String senha, String codigoCurso) {
    super(matricula, nome, email, senha, "COORDENADOR", codigoCurso);
  }
}
