package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.RelatorioUsuariosCadastrados;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.model.UsuarioCadastradoResumo;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RelatorioUsuariosCadastradosService {
  private final UsuarioRepository usuarioRepository;

  public RelatorioUsuariosCadastradosService(UsuarioRepository usuarioRepository) {
    if (usuarioRepository == null) {
      throw new IllegalArgumentException("Repositorio de usuarios obrigatorio.");
    }
    this.usuarioRepository = usuarioRepository;
  }

  public RelatorioUsuariosCadastrados gerarRelatorioGeralUsuariosCadastrados() {
    List<UsuarioCadastradoResumo> resumos = new ArrayList<>();
    for (Usuario usuario : usuarioRepository.buscarTodos()) {
      if (usuario == null) {
        continue;
      }
      resumos.add(
          new UsuarioCadastradoResumo(
              usuario.getMatricula(),
              usuario.getNome(),
              usuario.getEmail(),
              usuario.getPerfil(),
              usuario.getCodigoCurso()));
    }

    resumos.sort(
        Comparator.comparing(UsuarioCadastradoResumo::getPerfil, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(UsuarioCadastradoResumo::getMatricula, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(UsuarioCadastradoResumo::getEmail, String.CASE_INSENSITIVE_ORDER));
    return new RelatorioUsuariosCadastrados(resumos);
  }

  public RelatorioUsuariosCadastrados gerarRelatorioGeralUsuariosCadastrados(
      String perfilUsuarioLogado) throws ValidacaoException {
    validarAcessoAdministrador(perfilUsuarioLogado);
    return gerarRelatorioGeralUsuariosCadastrados();
  }

  private void validarAcessoAdministrador(String perfilUsuarioLogado) throws ValidacaoException {
    if (perfilUsuarioLogado == null || !"ADMINISTRADOR".equalsIgnoreCase(perfilUsuarioLogado)) {
      throw new ValidacaoException(
          "Acesso negado: Apenas administradores podem gerar relatorio geral de usuarios.");
    }
  }
}
