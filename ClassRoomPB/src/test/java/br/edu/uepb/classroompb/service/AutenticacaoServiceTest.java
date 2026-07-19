package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.service.exception.UsuarioJaExisteException;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AutenticacaoServiceTest {
  private static final Path ARQUIVO_USUARIOS = Path.of("usuarios.dat");

  private AutenticacaoService authService;
  private boolean arquivoUsuariosExistia;
  private byte[] conteudoOriginalUsuarios;

  @Before
  public void setUp() throws Exception {
    arquivoUsuariosExistia = Files.exists(ARQUIVO_USUARIOS);
    conteudoOriginalUsuarios =
        arquivoUsuariosExistia ? Files.readAllBytes(ARQUIVO_USUARIOS) : new byte[0];

    // Limpa o arquivo de dados físico antes de cada teste para garantir isolamento
    File file = ARQUIVO_USUARIOS.toFile();
    if (file.exists()) {
      file.delete();
    }
    Field instancia = AutenticacaoService.class.getDeclaredField("instancia");
    instancia.setAccessible(true);
    instancia.set(null, null);

    authService = AutenticacaoService.getInstancia();
    authService.realizarLogout();
  }

  @After
  public void tearDown() throws Exception {
    if (arquivoUsuariosExistia) {
      Files.write(ARQUIVO_USUARIOS, conteudoOriginalUsuarios);
    } else {
      Files.deleteIfExists(ARQUIVO_USUARIOS);
    }
  }

  @Test
  public void testCadastroSucessoEPerfil() throws Exception {
    authService.cadastrarUsuario("aluno", "Alessia", "202601", "alessia@uepb.edu.br", "senha123");

    authService.realizarLogin("202601", "senha123");
    assertNotNull(authService.getUsuarioLogado());
    assertEquals("ALUNO", authService.getUsuarioLogado().getPerfil());
  }

  @Test
  public void testImpedimentoDuplicidadeMatricula() throws Exception {
    authService.cadastrarUsuario("professor", "Carlos", "9999", "carlos@uepb.edu.br", "123");

    try {
      authService.cadastrarUsuario("aluno", "Mariana", "9999", "mariana@uepb.edu.br", "456");
      fail("Deveria ter lançado UsuarioJaExisteException.");
    } catch (UsuarioJaExisteException e) {
      assertTrue(e.getMessage().contains("já está cadastrado"));
    }
  }

  @Test
  public void testImpedimentoDuplicidadeEmail() throws Exception {
    authService.cadastrarUsuario("coordenador", "Paula", "8888", "paula@uepb.edu.br", "123");

    try {
      authService.cadastrarUsuario("aluno", "Lucas", "7777", "paula@uepb.edu.br", "456");
      fail("Deveria ter lançado UsuarioJaExisteException.");
    } catch (UsuarioJaExisteException e) {
      assertTrue(e.getMessage().contains("já está cadastrado"));
    }
  }

  @Test
  public void testEntradasVaziasEInvalidas() {
    try {
      authService.cadastrarUsuario("aluno", "", "111", "email@test.com", "senha");
      fail("Deveria ter rejeitado o nome em branco.");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Todos os campos são obrigatórios"));
    }
  }

  @Test
  public void testNegacaoDeAcessoParaSenhaIncorreta() throws Exception {
    authService.cadastrarUsuario("aluno", "Bruno", "171501", "bruno@uepb.edu.br", "senhaCorreta");

    try {
      authService.realizarLogin("171501", "senhaIncorreta");
      fail("Deveria ter lançado exceção por conta da senha incorreta.");
    } catch (Exception e) {
      assertTrue(
          e.getMessage().contains("Credenciais inválidas") || e.getMessage().contains("incorreta"));
    }

    assertNull(authService.getUsuarioLogado());
  }

  @Test
  public void testLoginComUsuarioInexistente() {
    try {
      authService.realizarLogin("usuario_fantasma@uepb.edu.br", "12345");
      fail("Deveria ter lançado exceção por usuário inexistente.");
    } catch (Exception e) {
      assertTrue(
          e.getMessage().contains("Credenciais inválidas")
              || e.getMessage().contains("não encontrado"));
    }

    assertNull(authService.getUsuarioLogado());
  }

  @Test
  public void testBloqueioDeComandosSemSessaoAtiva() {
    authService.realizarLogout();
    assertNull(
        "Usuarios nao logados nao devem ter acesso a uma sessao ativa.",
        authService.getUsuarioLogado());
  }

  @Test
  public void testCadastroDeAlunoComCursoObrigatorio() throws Exception {
    authService.cadastrarUsuario(
        "aluno", "Marina", "202602", "marina@uepb.edu.br", "senha123", "CC");

    authService.realizarLogin("202602", "senha123");
    assertEquals("CC", authService.getUsuarioLogado().getCodigoCurso());

    try {
      authService.cadastrarUsuario(
          "coordenador", "Paulo", "202603", "paulo@uepb.edu.br", "senha123", "");
      fail("Deveria rejeitar o cadastro sem codigo de curso.");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("codigo do curso e obrigatorio"));
    }
  }

  @Test
  public void testAdministradorPodeVincularCursoDeUsuarioExistente() throws Exception {
    authService.cadastrarUsuario("administrador", "Ana", "ADM01", "ana@uepb.edu.br", "123");
    authService.cadastrarUsuario("aluno", "Bruno", "ALU01", "bruno@uepb.edu.br", "123");

    authService.realizarLogin("ADM01", "123");
    authService.vincularCursoUsuario("ALU01", "CC");
    authService.realizarLogout();

    authService.realizarLogin("ALU01", "123");
    assertEquals("CC", authService.getUsuarioLogado().getCodigoCurso());
  }

  // ==========================================
  // CENÁRIOS ESPECÍFICOS DA TASK 1840 (US02)
  // ==========================================

  @Test
  public void testLoginComDadosNulosEVazios() {
    // Testa a rejeição de login com identificador em branco (Exigência da Task 1840)
    try {
      authService.realizarLogin("", "senha123");
      fail("Deveria ter lançado exceção para ID vazio.");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("devem ser preenchidos"));
    }

    // Testa a rejeição de login com senha nula (Exigência da Task 1840)
    try {
      authService.realizarLogin("202601", null);
      fail("Deveria ter lançado exceção para senha nula.");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("devem ser preenchidos"));
    }
  }
}
