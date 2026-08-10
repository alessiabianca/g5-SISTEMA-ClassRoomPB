package br.edu.uepb.classroompb.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import br.edu.uepb.classroompb.model.Coordenador;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.view.CoordenadorCLI;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CoordenadorRelatorioOcupacaoCLITest {
  private final ByteArrayOutputStream output = new ByteArrayOutputStream();
  private final ByteArrayOutputStream outputError = new ByteArrayOutputStream();
  private PrintStream originalOut;
  private PrintStream originalErr;

  @Before
  public void setUp() throws Exception {
    File dataDir = new File("data");
    if (!dataDir.exists()) {
      dataDir.mkdirs();
    }

    new File("data/turmas.txt").delete();
    new File("data/matriculas.txt").delete();

    originalOut = System.out;
    originalErr = System.err;
    System.setOut(new PrintStream(output));
    System.setErr(new PrintStream(outputError));
    definirUsuarioLogado(
        new Coordenador("COORD_RF41", "Coordenador RF41", "coord41@test.com", "123", "CC"));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    System.setErr(originalErr);
    AutenticacaoService.getInstancia().realizarLogout();
    new File("data/turmas.txt").delete();
    new File("data/matriculas.txt").delete();
  }

  @Test
  public void deveRenderizarRelatorioDeOcupacaoDeVagasPorPeriodo() throws Exception {
    DadosCliRF41 dados = prepararDadosCliRF41();

    dados.turmaRepository.salvar(new Turma("ES41", "2026.2", 4));
    dados.turmaRepository.salvar(new Turma("BD41", "2027.1", 10));
    dados.matriculaRepository.salvar(
        new Matricula("ALUNO_001", "ES41", "2026.2", Matricula.StatusMatricula.CONFIRMADA));
    dados.matriculaRepository.salvar(
        new Matricula("ALUNO_002", "ES41", "2026.2", Matricula.StatusMatricula.CONFIRMADA));
    dados.matriculaRepository.salvar(
        new Matricula("ALUNO_FILA", "ES41", "2026.2", Matricula.StatusMatricula.ESPERA));

    dados.coordenadorCLI.processar("gerarRelatorioOcupacaoVagas 2026.2");

    String painel = output.toString();
    assertTrue(painel.contains("RELATORIO DE OCUPACAO DE VAGAS - RF41"));
    assertTrue(painel.contains("ESCOPO: PERIODO: 2026.2"));
    assertTrue(painel.contains("ES41"));
    assertFalse(painel.contains("BD41"));
    assertTrue(painel.contains("ALUNOS EM LISTA DE ESPERA    : 1"));
    assertTrue(
        painel.contains("DENSIDADE GERAL              : 50,0%")
            || painel.contains("DENSIDADE GERAL              : 50.0%"));
  }

  @Test
  public void deveRenderizarRelatorioGeralDeOcupacaoDeVagas() throws Exception {
    DadosCliRF41 dados = prepararDadosCliRF41();

    dados.turmaRepository.salvar(new Turma("ES41", "2026.2", 4));
    dados.turmaRepository.salvar(new Turma("BD41", "2027.1", 6));
    salvarMatriculasConfirmadas(dados.matriculaRepository, "ES_ALUNO_", "ES41", "2026.2", 2);
    salvarMatriculasConfirmadas(dados.matriculaRepository, "BD_ALUNO_", "BD41", "2027.1", 3);

    dados.coordenadorCLI.processar("gerarRelatorioOcupacaoVagas");

    String painel = output.toString();
    assertTrue(painel.contains("ESCOPO: TODOS OS PERIODOS"));
    assertTrue(painel.contains("ES41"));
    assertTrue(painel.contains("BD41"));
    assertTrue(painel.contains("TOTAL DE TURMAS              : 2"));
    assertTrue(painel.contains("TETO TOTAL DE VAGAS          : 10"));
    assertTrue(painel.contains("VAGAS OCUPADAS               : 5"));
    assertTrue(
        painel.contains("DENSIDADE GERAL              : 50,0%")
            || painel.contains("DENSIDADE GERAL              : 50.0%"));
  }

  @Test
  public void deveRenderizarMensagemQuandoRelatorioNaoPossuirTurmas() {
    DadosCliRF41 dados = prepararDadosCliRF41();

    dados.coordenadorCLI.processar("gerarRelatorioOcupacaoVagas 2030.1");

    String painel = output.toString();
    assertTrue(painel.contains("RELATORIO DE OCUPACAO DE VAGAS - RF41"));
    assertTrue(painel.contains("ESCOPO: PERIODO: 2030.1"));
    assertTrue(painel.contains("Nao ha turmas ofertadas para o escopo informado"));
  }

  @Test
  public void deveBloquearRelatorioOcupacaoQuandoUsuarioNaoForCoordenador() {
    DadosCliRF41 dados = prepararDadosCliRF41();
    AutenticacaoService.getInstancia().realizarLogout();

    dados.coordenadorCLI.processar("gerarRelatorioOcupacaoVagas");

    assertTrue(
        outputError
            .toString()
            .contains("ACESSO NEGADO: Apenas usuarios autenticados com o perfil de Coordenador"));
  }

  private DadosCliRF41 prepararDadosCliRF41() {
    TurmaRepository turmaRepository = new TurmaRepository();
    MatriculaRepository matriculaRepository = new MatriculaRepository();
    UsuarioRepository usuarioRepository = new UsuarioRepository();
    TurmaService turmaService =
        new TurmaService(turmaRepository, new PeriodoRepository(), new DisciplinaRepository());
    DiarioService diarioService =
        new DiarioService(new DiarioRepository(), turmaRepository, usuarioRepository);

    CoordenadorCLI coordenadorCLI =
        new CoordenadorCLI(turmaService, null, diarioService, usuarioRepository);
    return new DadosCliRF41(turmaRepository, matriculaRepository, coordenadorCLI);
  }

  private void salvarMatriculasConfirmadas(
      MatriculaRepository matriculaRepository,
      String prefixo,
      String codigoDisciplina,
      String periodo,
      int quantidade) {
    for (int i = 1; i <= quantidade; i++) {
      matriculaRepository.salvar(
          new Matricula(
              prefixo + i, codigoDisciplina, periodo, Matricula.StatusMatricula.CONFIRMADA));
    }
  }

  private void definirUsuarioLogado(Usuario usuario) throws Exception {
    Field campo = AutenticacaoService.class.getDeclaredField("usuarioLogado");
    campo.setAccessible(true);
    campo.set(AutenticacaoService.getInstancia(), usuario);
  }

  private static class DadosCliRF41 {
    private final TurmaRepository turmaRepository;
    private final MatriculaRepository matriculaRepository;
    private final CoordenadorCLI coordenadorCLI;

    private DadosCliRF41(
        TurmaRepository turmaRepository,
        MatriculaRepository matriculaRepository,
        CoordenadorCLI coordenadorCLI) {
      this.turmaRepository = turmaRepository;
      this.matriculaRepository = matriculaRepository;
      this.coordenadorCLI = coordenadorCLI;
    }
  }
}
