package br.edu.uepb.classroompb.model;

public class UsuarioCadastradoResumo {
  private final String matricula;
  private final String nome;
  private final String email;
  private final String perfil;
  private final String codigoCurso;

  public UsuarioCadastradoResumo(
      String matricula, String nome, String email, String perfil, String codigoCurso) {
    this.matricula = normalizarCampo(matricula);
    this.nome = normalizarCampo(nome);
    this.email = normalizarCampo(email);
    this.perfil = normalizarCampo(perfil);
    this.codigoCurso = normalizarCurso(codigoCurso);
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

  public String getPerfil() {
    return perfil;
  }

  public String getCodigoCurso() {
    return codigoCurso;
  }

  public boolean possuiCursoVinculado() {
    return !codigoCurso.equals("NAO_VINCULADO");
  }

  private String normalizarCampo(String valor) {
    if (valor == null || valor.isBlank()) {
      return "NAO_INFORMADO";
    }
    return valor.trim();
  }

  private String normalizarCurso(String valor) {
    if (valor == null || valor.isBlank()) {
      return "NAO_VINCULADO";
    }
    return valor.trim();
  }
}
