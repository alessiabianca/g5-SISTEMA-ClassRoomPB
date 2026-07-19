package br.edu.uepb.classroompb.model;

public class Aluno extends Usuario {
  public Aluno(String matricula, String nome, String email, String senha) {
    this(matricula, nome, email, senha, null);
  }

  public Aluno(String matricula, String nome, String email, String senha, String codigoCurso) {
    super(matricula, nome, email, senha, "ALUNO", codigoCurso);
  }
}
