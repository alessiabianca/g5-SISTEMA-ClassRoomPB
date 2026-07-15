package br.edu.uepb.classroompb.model;

import java.io.Serializable;

public abstract class Usuario implements Serializable {
  private static final long serialVersionUID = 1L;

  private String matricula;
  private String nome;
  private String email;
  private String senha; // Adicionado conforme os detalhes da Task 1704
  private String perfil; // Ex: "PROFESSOR", "ALUNO", "COORDENADOR", "ADMINISTRADOR"

  public Usuario(String matricula, String nome, String email, String senha, String perfil) {
    this.matricula = matricula;
    this.nome = nome;
    this.email = email;
    this.senha = senha;
    this.perfil = perfil;
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

  @Override
  public String toString() {
    return matricula + ";" + nome + ";" + email + ";" + senha + ";" + perfil;
  }
}
