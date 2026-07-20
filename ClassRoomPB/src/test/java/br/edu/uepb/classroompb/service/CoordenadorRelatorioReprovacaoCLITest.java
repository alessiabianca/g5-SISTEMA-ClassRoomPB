package br.edu.uepb.classroompb.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import br.edu.uepb.classroompb.model.Coordenador;
import br.edu.uepb.classroompb.model.Historico;
import br.edu.uepb.classroompb.model.StatusAcademico;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.repository.HistoricoRepository;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.view.CoordenadorCLI;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CoordenadorRelatorioReprovacaoCLITest {
  private final ByteArrayOutputStream output = new ByteArrayOutputStream();
  private final ByteArrayOutputStream outputError = new ByteArrayOutputStream();
  private PrintStream originalOut;
  private PrintStream originalErr;
  private HistoricoRepository historicoRepository;
  private CoordenadorCLI coordenadorCLI;

  @Before
  public void setUp() throws Exception {
    File dataDir = new File("data");
    if (!dataDir.exists()) {
      dataDir.mkdirs();
    }

    new File("data/historico.txt").delete();

    originalOut = System.out;
    originalErr = System.err;
    System.setOut(new PrintStream(output));
    System.setErr(new PrintStream(outputError));

    historicoRepository = new HistoricoRepository();
    HistoricoService historicoService =
        new HistoricoService(historicoRepository, null, null, null, null);
    coordenadorCLI = new CoordenadorCLI(null, historicoService, new UsuarioRepository());
    definirUsuarioLogado(
        new Coordenador("COORD_RF42", "Coordenador RF42", "coord42@test.com", "123", "CC"));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    System.setErr(originalErr);
    AutenticacaoService.getInstancia().realizarLogout();
    new File("data/historico.txt").delete();
  }

  @Test
  public void deveRenderizarRelatorioGeralDeReprovacaoPorDisciplina() {
    salvarHistorico("ALUNO_001", "2026.1", "ES42", 8.0, 90.0, StatusAcademico.APROVADO);
    salvarHistorico("ALUNO_002", "2026.1", "ES42", 3.0, 90.0, StatusAcademico.REPROVADO_NOTA);
    salvarHistorico("ALUNO_003", "2026.1", "BD42", 2.0, 60.0, StatusAcademico.REPROVADO_FALTA);

    coordenadorCLI.processar("gerarRelatorioReprovacaoPorDisciplina");

    String painel = output.toString();
    assertTrue(painel.contains("RELATORIO DE REPROVACAO POR DISCIPLINA - RF42"));
    assertTrue(painel.contains("ESCOPO: TODAS AS DISCIPLINAS"));
    assertTrue(painel.contains("ES42"));
    assertTrue(painel.contains("BD42"));
    assertTrue(painel.contains("TOTAL DE DISCIPLINAS         : 2"));
    assertTrue(painel.contains("REGISTROS ANALISADOS         : 3"));
    assertTrue(painel.contains("TOTAL DE REPROVACOES         : 2"));
    assertTrue(painel.contains("TAXA GERAL DE REPROVACAO     : 66.7%"));
  }

  @Test
  public void deveRenderizarRelatorioFiltradoPorDisciplina() {
    salvarHistorico("ALUNO_001", "2026.1", "ES42", 8.0, 90.0, StatusAcademico.APROVADO);
    salvarHistorico("ALUNO_002", "2026.1", "ES42", 3.0, 90.0, StatusAcademico.REPROVADO_NOTA);
    salvarHistorico("ALUNO_003", "2026.1", "BD42", 2.0, 60.0, StatusAcademico.REPROVADO_FALTA);

    coordenadorCLI.processar("gerarRelatorioReprovacaoPorDisciplina ES42");

    String painel = output.toString();
    assertTrue(painel.contains("ESCOPO: DISCIPLINA: ES42"));
    assertTrue(painel.contains("ES42"));
    assertFalse(painel.contains("BD42"));
    assertTrue(painel.contains("REPROVACOES POR NOTA         : 1"));
    assertTrue(painel.contains("REPROVACOES POR FALTA        : 0"));
    assertTrue(painel.contains("TAXA GERAL DE REPROVACAO     : 50.0%"));
  }

  @Test
  public void deveRenderizarMensagemQuandoNaoHouverHistoricoNoEscopo() {
    coordenadorCLI.processar("gerarRelatorioReprovacaoDisciplina ES42");

    String painel = output.toString();
    assertTrue(painel.contains("RELATORIO DE REPROVACAO POR DISCIPLINA - RF42"));
    assertTrue(painel.contains("Nao ha historico consolidado para o escopo informado"));
  }

  @Test
  public void deveBloquearRelatorioReprovacaoQuandoUsuarioNaoForCoordenador() {
    AutenticacaoService.getInstancia().realizarLogout();

    coordenadorCLI.processar("gerarRelatorioReprovacaoPorDisciplina");

    assertTrue(
        outputError
            .toString()
            .contains("ACESSO NEGADO: Apenas usuarios autenticados com o perfil de Coordenador"));
  }

  private void salvarHistorico(
      String matriculaAluno,
      String periodo,
      String codigoDisciplina,
      double mediaFinal,
      double percentualFrequencia,
      StatusAcademico status) {
    historicoRepository.salvarLote(
        List.of(
            new Historico(
                matriculaAluno,
                periodo,
                codigoDisciplina,
                "PROF_" + codigoDisciplina,
                mediaFinal,
                percentualFrequencia,
                status)));
  }

  private void definirUsuarioLogado(Usuario usuario) throws Exception {
    Field campo = AutenticacaoService.class.getDeclaredField("usuarioLogado");
    campo.setAccessible(true);
    campo.set(AutenticacaoService.getInstancia(), usuario);
  }
}
