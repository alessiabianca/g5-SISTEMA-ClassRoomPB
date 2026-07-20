package br.edu.uepb.classroompb.model;

import java.io.Serializable;

public abstract class Usuario implements Serializable {
  private static final long serialVersionUID = 1L;

  private String matricula;
  private String nome;
  private String email;
  private String senha;
  private String perfil;
  private String codigoCurso;

  public Usuario(String matricula, String nome, String email, String senha, String perfil) {
    this(matricula, nome, email, senha, perfil, null);
  }

  public Usuario(
      String matricula,
      String nome,
      String email,
      String senha,
      String perfil,
      String codigoCurso) {
    this.matricula = matricula;
    this.nome = nome;
    this.email = email;
    this.senha = senha;
    this.perfil = perfil;
    this.codigoCurso = codigoCurso;
  }

  public String getMatricula() {
    return matricula;
  }

  public String getNome() {
    return nome;
  }

  public String getEmail() {
    return email;
  }

  public String getSenha() {
    return senha;
  }

  public String getPerfil() {
    return perfil;
  }

  public String getCodigoCurso() {
    return codigoCurso;
  }

  public void setCodigoCurso(String codigoCurso) {
    this.codigoCurso = codigoCurso;
  }

  @Override
  public String toString() {
    return matricula + ";" + nome + ";" + email + ";" + senha + ";" + perfil;
  }
}
