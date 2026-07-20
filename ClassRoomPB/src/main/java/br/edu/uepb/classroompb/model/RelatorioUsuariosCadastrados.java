package br.edu.uepb.classroompb.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RelatorioUsuariosCadastrados {
  private final List<UsuarioCadastradoResumo> usuarios;

  public RelatorioUsuariosCadastrados(List<UsuarioCadastradoResumo> usuarios) {
    List<UsuarioCadastradoResumo> base = usuarios == null ? new ArrayList<>() : usuarios;
    this.usuarios = Collections.unmodifiableList(new ArrayList<>(base));
  }

  public List<UsuarioCadastradoResumo> getUsuarios() {
    return usuarios;
  }

  public boolean isVazio() {
    return usuarios.isEmpty();
  }

  public int getTotalUsuarios() {
    return usuarios.size();
  }

  public int getTotalAlunos() {
    return contarPerfil("ALUNO");
  }

  public int getTotalProfessores() {
    return contarPerfil("PROFESSOR");
  }

  public int getTotalCoordenadores() {
    return contarPerfil("COORDENADOR");
  }

  public int getTotalAdministradores() {
    return contarPerfil("ADMINISTRADOR");
  }

  public int getTotalUsuariosComCursoVinculado() {
    int total = 0;
    for (UsuarioCadastradoResumo usuario : usuarios) {
      if (usuario.possuiCursoVinculado()) {
        total++;
      }
    }
    return total;
  }

  public int getTotalUsuariosSemCursoVinculado() {
    return getTotalUsuarios() - getTotalUsuariosComCursoVinculado();
  }

  public int contarUsuariosPorCurso(String codigoCurso) {
    if (codigoCurso == null || codigoCurso.isBlank()) {
      return 0;
    }

    int total = 0;
    for (UsuarioCadastradoResumo usuario : usuarios) {
      if (codigoCurso.trim().equalsIgnoreCase(usuario.getCodigoCurso())) {
        total++;
      }
    }
    return total;
  }

  public int contarPerfil(String perfil) {
    if (perfil == null || perfil.isBlank()) {
      return 0;
    }

    int total = 0;
    for (UsuarioCadastradoResumo usuario : usuarios) {
      if (perfil.trim().equalsIgnoreCase(usuario.getPerfil())) {
        total++;
      }
    }
    return total;
  }
}
