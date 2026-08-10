package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Administrador;
import br.edu.uepb.classroompb.model.Aluno;
import br.edu.uepb.classroompb.model.Coordenador;
import br.edu.uepb.classroompb.model.Professor;
import br.edu.uepb.classroompb.model.RelatorioUsuariosCadastrados;
import br.edu.uepb.classroompb.model.UsuarioCadastradoResumo;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RelatorioUsuariosCadastradosServiceTest {
  private static final Path ARQUIVO_USUARIOS = Path.of("usuarios.json");

  private UsuarioRepository usuarioRepository;
  private RelatorioUsuariosCadastradosService relatorioService;
  private boolean arquivoUsuariosExistia;
  private byte[] conteudoOriginalUsuarios;

  @Before
  public void setUp() throws Exception {
    arquivoUsuariosExistia = Files.exists(ARQUIVO_USUARIOS);
    conteudoOriginalUsuarios =
        arquivoUsuariosExistia ? Files.readAllBytes(ARQUIVO_USUARIOS) : new byte[0];

    File file = ARQUIVO_USUARIOS.toFile();
    if (file.exists()) {
      file.delete();
    }

    resetarAutenticacaoSingleton();
    usuarioRepository = new UsuarioRepository();
    relatorioService = new RelatorioUsuariosCadastradosService(usuarioRepository);
  }

  @After
  public void tearDown() throws Exception {
    resetarAutenticacaoSingleton();
    if (arquivoUsuariosExistia) {
      Files.write(ARQUIVO_USUARIOS, conteudoOriginalUsuarios);
    } else {
      Files.deleteIfExists(ARQUIVO_USUARIOS);
    }
  }

  @Test
  public void deveRetornarRelatorioVazioQuandoNaoHouverUsuariosCadastrados() {
    RelatorioUsuariosCadastrados relatorio =
        relatorioService.gerarRelatorioGeralUsuariosCadastrados();

    assertTrue(relatorio.isVazio());
    assertEquals(0, relatorio.getTotalUsuarios());
    assertEquals(0, relatorio.getTotalAlunos());
    assertEquals(0, relatorio.getTotalProfessores());
    assertEquals(0, relatorio.getTotalCoordenadores());
    assertEquals(0, relatorio.getTotalAdministradores());
  }

  @Test
  public void deveGerarRelatorioGeralComTotaisPorPerfilECurso() {
    salvarUsuariosBase();

    RelatorioUsuariosCadastrados relatorio =
        relatorioService.gerarRelatorioGeralUsuariosCadastrados();

    assertEquals(5, relatorio.getTotalUsuarios());
    assertEquals(2, relatorio.getTotalAlunos());
    assertEquals(1, relatorio.getTotalProfessores());
    assertEquals(1, relatorio.getTotalCoordenadores());
    assertEquals(1, relatorio.getTotalAdministradores());
    assertEquals(3, relatorio.getTotalUsuariosComCursoVinculado());
    assertEquals(2, relatorio.getTotalUsuariosSemCursoVinculado());
    assertEquals(2, relatorio.contarUsuariosPorCurso("CC"));
    assertEquals(1, relatorio.contarUsuariosPorCurso("SI"));
  }

  @Test
  public void deveOrdenarUsuariosPorPerfilMatriculaEEmail() {
    salvarUsuariosBase();

    List<UsuarioCadastradoResumo> usuarios =
        relatorioService.gerarRelatorioGeralUsuariosCadastrados().getUsuarios();

    assertEquals("ADM01", usuarios.get(0).getMatricula());
    assertEquals("ALU01", usuarios.get(1).getMatricula());
    assertEquals("ALU02", usuarios.get(2).getMatricula());
    assertEquals("COORD01", usuarios.get(3).getMatricula());
    assertEquals("PROF01", usuarios.get(4).getMatricula());
  }

  @Test
  public void deveExporDadosPublicosSemSenhaNoResumo() {
    usuarioRepository.salvar(new Aluno("ALU01", "Aluno Um", "alu1@test.com", "senha", "CC"));

    UsuarioCadastradoResumo usuario =
        relatorioService.gerarRelatorioGeralUsuariosCadastrados().getUsuarios().get(0);

    assertEquals("ALU01", usuario.getMatricula());
    assertEquals("Aluno Um", usuario.getNome());
    assertEquals("alu1@test.com", usuario.getEmail());
    assertEquals("ALUNO", usuario.getPerfil());
    assertEquals("CC", usuario.getCodigoCurso());
  }

  @Test
  public void deveNormalizarCursoNaoVinculadoNoResumo() {
    usuarioRepository.salvar(new Professor("PROF01", "Prof Um", "prof@test.com", "senha"));

    UsuarioCadastradoResumo usuario =
        relatorioService.gerarRelatorioGeralUsuariosCadastrados().getUsuarios().get(0);

    assertEquals("NAO_VINCULADO", usuario.getCodigoCurso());
    assertFalse(usuario.possuiCursoVinculado());
  }

  @Test
  public void deveExporListaDeUsuariosComoImutavel() {
    usuarioRepository.salvar(new Administrador("ADM01", "Admin", "admin@test.com", "senha"));
    RelatorioUsuariosCadastrados relatorio =
        relatorioService.gerarRelatorioGeralUsuariosCadastrados();

    try {
      relatorio.getUsuarios().clear();
      fail("A lista de usuarios do relatorio deve ser imutavel.");
    } catch (UnsupportedOperationException e) {
      assertEquals(1, relatorio.getTotalUsuarios());
    }
  }

  @Test
  public void devePermitirGeracaoQuandoPerfilLogadoForAdministrador() throws Exception {
    salvarUsuariosBase();

    RelatorioUsuariosCadastrados relatorio =
        relatorioService.gerarRelatorioGeralUsuariosCadastrados("ADMINISTRADOR");

    assertEquals(5, relatorio.getTotalUsuarios());
  }

  @Test
  public void deveBloquearGeracaoQuandoPerfilLogadoNaoForAdministrador() throws Exception {
    try {
      relatorioService.gerarRelatorioGeralUsuariosCadastrados("COORDENADOR");
      fail("Deveria bloquear relatorio para usuario que nao seja administrador.");
    } catch (ValidacaoException e) {
      assertTrue(e.getMessage().contains("Acesso negado"));
    }
  }

  @Test
  public void deveBloquearGeracaoQuandoNaoHouverPerfilLogado() throws Exception {
    try {
      relatorioService.gerarRelatorioGeralUsuariosCadastrados(null);
      fail("Deveria bloquear relatorio sem perfil logado.");
    } catch (ValidacaoException e) {
      assertTrue(e.getMessage().contains("Acesso negado"));
    }
  }

  @Test
  public void deveExporFachadaNoAutenticacaoServiceParaAdministradorLogado() throws Exception {
    AutenticacaoService authService = AutenticacaoService.getInstancia();
    authService.cadastrarUsuario("administrador", "Admin", "ADM01", "admin@test.com", "senha");
    authService.cadastrarUsuario("professor", "Professor", "PROF01", "prof@test.com", "senha");
    authService.realizarLogin("ADM01", "senha");

    RelatorioUsuariosCadastrados relatorio = authService.gerarRelatorioGeralUsuariosCadastrados();

    assertEquals(2, relatorio.getTotalUsuarios());
    assertEquals(1, relatorio.getTotalAdministradores());
    assertEquals(1, relatorio.getTotalProfessores());
  }

  @Test
  public void deveBloquearFachadaNoAutenticacaoServiceSemAdministradorLogado() throws Exception {
    AutenticacaoService authService = AutenticacaoService.getInstancia();
    authService.cadastrarUsuario("professor", "Professor", "PROF01", "prof@test.com", "senha");
    authService.realizarLogin("PROF01", "senha");

    try {
      authService.gerarRelatorioGeralUsuariosCadastrados();
      fail("Deveria bloquear relatorio para professor logado.");
    } catch (ValidacaoException e) {
      assertTrue(e.getMessage().contains("Acesso negado"));
    }
  }

  @Test
  public void deveRecusarRepositorioNuloNaInicializacaoDoMotor() {
    try {
      new RelatorioUsuariosCadastradosService(null);
      fail("Deveria recusar repositorio nulo.");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("usuarios"));
    }
  }

  @Test
  public void deveBuscarTodosUsuariosNoRepositorio() {
    usuarioRepository.salvar(new Administrador("ADM01", "Admin", "admin@test.com", "senha"));
    usuarioRepository.salvar(new Professor("PROF01", "Prof", "prof@test.com", "senha"));

    assertEquals(2, usuarioRepository.buscarTodos().size());
  }

  private void salvarUsuariosBase() {
    usuarioRepository.salvar(new Professor("PROF01", "Prof Um", "prof@test.com", "senha"));
    usuarioRepository.salvar(new Aluno("ALU02", "Aluno Dois", "alu2@test.com", "senha", "SI"));
    usuarioRepository.salvar(new Administrador("ADM01", "Admin", "admin@test.com", "senha"));
    usuarioRepository.salvar(new Aluno("ALU01", "Aluno Um", "alu1@test.com", "senha", "CC"));
    usuarioRepository.salvar(
        new Coordenador("COORD01", "Coord Um", "coord@test.com", "senha", "CC"));
  }

  private void resetarAutenticacaoSingleton() throws Exception {
    Field instancia = AutenticacaoService.class.getDeclaredField("instancia");
    instancia.setAccessible(true);
    instancia.set(null, null);
  }
}
