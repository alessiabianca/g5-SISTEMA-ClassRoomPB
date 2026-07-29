package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.factory.UsuarioFactory;
import br.edu.uepb.classroompb.model.*;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.exception.UsuarioJaExisteException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

public class AutenticacaoService {
  private static AutenticacaoService instancia;
  private final UsuarioRepository repository;
  private Usuario usuarioLogado;

  private AutenticacaoService() {
    this.repository = new UsuarioRepository();
  }

  public static AutenticacaoService getInstancia() {
    if (instancia == null) {
      instancia = new AutenticacaoService();
    }
    return instancia;
  }

  public void cadastrarUsuario(
      String tipo, String nome, String matricula, String email, String senha) throws Exception {
    cadastrarUsuarioInterno(tipo, nome, matricula, email, senha, null, false);
  }

  public void cadastrarUsuario(
      String tipo, String nome, String matricula, String email, String senha, String codigoCurso)
      throws Exception {
    cadastrarUsuarioInterno(tipo, nome, matricula, email, senha, codigoCurso, true);
  }

  private void cadastrarUsuarioInterno(
      String tipo,
      String nome,
      String matricula,
      String email,
      String senha,
      String codigoCurso,
      boolean exigirCurso)
      throws Exception {
    if (tipo == null
        || tipo.trim().isEmpty()
        || nome == null
        || nome.trim().isEmpty()
        || matricula == null
        || matricula.trim().isEmpty()
        || email == null
        || email.trim().isEmpty()
        || senha == null
        || senha.trim().isEmpty()) {
      throw new Exception("Erro: Todos os campos são obrigatórios e não podem estar em branco.");
    }

    if (repository.existe(matricula)) {
      throw new UsuarioJaExisteException(
          "Erro: Usuário com a matrícula '" + matricula + "' já está cadastrado.");
    }
    if (repository.existe(email)) {
      throw new UsuarioJaExisteException(
          "Erro: Usuário com o e-mail '" + email + "' já está cadastrado.");
    }

    String tipoNormalizado = tipo.trim().toLowerCase();
    if ((tipoNormalizado.equals("aluno") || tipoNormalizado.equals("coordenador"))
        && exigirCurso
        && (codigoCurso == null || codigoCurso.trim().isEmpty())) {
      throw new Exception("Erro: O codigo do curso e obrigatorio para aluno e coordenador.");
    }

    Usuario novo =
        UsuarioFactory.criarUsuario(tipoNormalizado, matricula, nome, email, senha, codigoCurso);

    repository.salvar(novo);
  }

  /** Task 1713: Motor de autenticação e validação de credenciais/perfis (US02) */
  public void realizarLogin(String id, String senha) throws Exception {

    if (id == null || id.trim().isEmpty() || senha == null || senha.trim().isEmpty()) {
      throw new Exception("Erro: Identificador e senha devem ser preenchidos.");
    }

    Usuario usuario = repository.buscarPorId(id);

    if (usuario == null || !usuario.getSenha().equals(senha)) {
      throw new Exception(
          "Erro: Credenciais inválidas (Usuário não encontrado ou senha incorreta).");
    }

    String perfil = usuario.getPerfil();
    if (perfil == null || perfil.trim().isEmpty()) {
      throw new Exception("Erro: Usuário não possui um perfil de acesso válido configurado.");
    }

    this.usuarioLogado = usuario;
    System.out.println("Acesso autorizado! Bem-vindo(a), perfil: " + perfil);
  }

  public Usuario getUsuarioLogado() {
    return usuarioLogado;
  }

  /** RF43: Gera o relatorio geral de usuarios cadastrados para administradores autenticados. */
  public RelatorioUsuariosCadastrados gerarRelatorioGeralUsuariosCadastrados()
      throws ValidacaoException {
    String perfilLogado = usuarioLogado == null ? null : usuarioLogado.getPerfil();
    return new RelatorioUsuariosCadastradosService(repository)
        .gerarRelatorioGeralUsuariosCadastrados(perfilLogado);
  }

  public void vincularCursoUsuario(String matricula, String codigoCurso) throws Exception {
    if (usuarioLogado == null || !"ADMINISTRADOR".equalsIgnoreCase(usuarioLogado.getPerfil())) {
      throw new Exception("Acesso negado: apenas administradores podem vincular cursos.");
    }
    if (codigoCurso == null || codigoCurso.isBlank()) {
      throw new Exception("Erro: O codigo do curso e obrigatorio.");
    }

    Usuario usuario = repository.buscarPorMatricula(matricula);
    if (usuario == null
        || !("ALUNO".equalsIgnoreCase(usuario.getPerfil())
            || "COORDENADOR".equalsIgnoreCase(usuario.getPerfil()))) {
      throw new Exception("Erro: matricula nao corresponde a um aluno ou coordenador.");
    }

    repository.atualizarCurso(usuario.getMatricula(), codigoCurso);
  }

  public void realizarLogout() {
    this.usuarioLogado = null;
  }
}
