package br.edu.uepb.classroompb.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import br.edu.uepb.classroompb.model.Administrador;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.view.AdminCLI;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AdministradorRelatorioUsuariosCLITest {
  private static final Path ARQUIVO_USUARIOS = Path.of("data/usuarios.json");

  private final ByteArrayOutputStream output = new ByteArrayOutputStream();
  private final ByteArrayOutputStream outputError = new ByteArrayOutputStream();
  private PrintStream originalOut;
  private PrintStream originalErr;
  private boolean arquivoUsuariosExistia;
  private byte[] conteudoOriginalUsuarios;
  private AutenticacaoService authService;
  private AdminCLI adminCLI;

  @Before
  public void setUp() throws Exception {
    arquivoUsuariosExistia = Files.exists(ARQUIVO_USUARIOS);
    conteudoOriginalUsuarios =
        arquivoUsuariosExistia ? Files.readAllBytes(ARQUIVO_USUARIOS) : new byte[0];
    Files.deleteIfExists(ARQUIVO_USUARIOS);
    resetarAutenticacaoSingleton();

    originalOut = System.out;
    originalErr = System.err;
    System.setOut(new PrintStream(output));
    System.setErr(new PrintStream(outputError));

    authService = AutenticacaoService.getInstancia();
    adminCLI = new AdminCLI(null, null);
  }

  @After
  public void tearDown() throws Exception {
    System.setOut(originalOut);
    System.setErr(originalErr);
    resetarAutenticacaoSingleton();
    if (arquivoUsuariosExistia) {
      Files.write(ARQUIVO_USUARIOS, conteudoOriginalUsuarios);
    } else {
      Files.deleteIfExists(ARQUIVO_USUARIOS);
    }
  }

  @Test
  public void deveRenderizarRelatorioGeralUsuariosCadastradosParaAdministrador() throws Exception {
    salvarUsuariosBase();
    authService.realizarLogin("ADM_RF43", "senha");
    output.reset();

    adminCLI.processar("gerarRelatorioGeralUsuariosCadastrados");

    String painel = output.toString();
    assertTrue(painel.contains("RELATORIO GERAL DE USUARIOS CADASTRADOS - RF43"));
    assertTrue(painel.contains("ADM_RF43"));
    assertTrue(painel.contains("ALU_RF43"));
    assertTrue(painel.contains("PROF_RF43"));
    assertTrue(painel.contains("COORD_RF43"));
    assertTrue(painel.contains("TOTAL DE USUARIOS             : 4"));
    assertTrue(painel.contains("ALUNOS                        : 1"));
    assertTrue(painel.contains("PROFESSORES                   : 1"));
    assertTrue(painel.contains("COORDENADORES                 : 1"));
    assertTrue(painel.contains("ADMINISTRADORES               : 1"));
    assertTrue(painel.contains("USUARIOS COM CURSO VINCULADO  : 2"));
    assertTrue(painel.contains("USUARIOS SEM CURSO VINCULADO  : 2"));
    assertFalse(painel.contains("senha"));
  }

  @Test
  public void deveRenderizarMensagemQuandoNaoHouverUsuariosCadastrados() throws Exception {
    definirUsuarioLogado(
        new Administrador("ADM_VAZIO_RF43", "Admin Vazio", "admin.vazio@test.com", "senha"));

    adminCLI.processar("gerarRelatorioUsuariosCadastrados");

    String painel = output.toString();
    assertTrue(painel.contains("RELATORIO GERAL DE USUARIOS CADASTRADOS - RF43"));
    assertTrue(painel.contains("Nao ha usuarios cadastrados no sistema"));
  }

  @Test
  public void deveBloquearRelatorioUsuariosQuandoUsuarioNaoForAdministrador() throws Exception {
    authService.cadastrarUsuario(
        "professor", "Professor RF43", "PROF_RF43", "prof43@test.com", "senha");
    authService.realizarLogin("PROF_RF43", "senha");
    output.reset();
    outputError.reset();

    adminCLI.processar("gerarRelatorioGeralUsuariosCadastrados");

    assertTrue(outputError.toString().contains("Erro de Validacao: Acesso negado"));
    assertFalse(output.toString().contains("RELATORIO GERAL DE USUARIOS CADASTRADOS - RF43"));
  }

  @Test
  public void deveBloquearRelatorioUsuariosQuandoNaoHouverSessaoAtiva() {
    adminCLI.processar("gerarRelatorioGeralUsuariosCadastrados");

    assertTrue(outputError.toString().contains("Erro de Validacao: Acesso negado"));
  }

  private void salvarUsuariosBase() throws Exception {
    authService.cadastrarUsuario(
        "administrador", "Admin RF43", "ADM_RF43", "admin43@test.com", "senha");
    authService.cadastrarUsuario(
        "aluno", "Aluno RF43", "ALU_RF43", "aluno43@test.com", "senha", "CC");
    authService.cadastrarUsuario(
        "coordenador", "Coord RF43", "COORD_RF43", "coord43@test.com", "senha", "SI");
    authService.cadastrarUsuario(
        "professor", "Professor RF43", "PROF_RF43", "prof43@test.com", "senha");
  }

  private void definirUsuarioLogado(Usuario usuario) throws Exception {
    Field campo = AutenticacaoService.class.getDeclaredField("usuarioLogado");
    campo.setAccessible(true);
    campo.set(AutenticacaoService.getInstancia(), usuario);
  }

  private void resetarAutenticacaoSingleton() throws Exception {
    Field instancia = AutenticacaoService.class.getDeclaredField("instancia");
    instancia.setAccessible(true);
    instancia.set(null, null);
  }
}
