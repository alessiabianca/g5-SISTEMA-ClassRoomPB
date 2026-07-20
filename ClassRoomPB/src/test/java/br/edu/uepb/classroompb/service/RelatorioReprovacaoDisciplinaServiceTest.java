package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Historico;
import br.edu.uepb.classroompb.model.RelatorioReprovacaoDisciplina;
import br.edu.uepb.classroompb.model.ReprovacaoDisciplina;
import br.edu.uepb.classroompb.model.StatusAcademico;
import br.edu.uepb.classroompb.repository.HistoricoRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class RelatorioReprovacaoDisciplinaServiceTest {
  private HistoricoRepository historicoRepository;
  private RelatorioReprovacaoDisciplinaService relatorioService;

  @Before
  public void setUp() {
    File dataDir = new File("data");
    if (!dataDir.exists()) {
      dataDir.mkdirs();
    }

    new File("data/historico.txt").delete();
    historicoRepository = new HistoricoRepository();
    relatorioService = new RelatorioReprovacaoDisciplinaService(historicoRepository);
  }

  @Test
  public void deveRetornarRelatorioVazioQuandoNaoHouverHistoricosConsolidados() {
    RelatorioReprovacaoDisciplina relatorio =
        relatorioService.gerarRelatorioReprovacaoPorDisciplina();

    assertTrue(relatorio.isVazio());
    assertEquals(0, relatorio.getTotalDisciplinas());
    assertEquals(0, relatorio.getTotalRegistrosAnalisados());
    assertEquals(0, relatorio.getTotalReprovados());
    assertEquals(0.0, relatorio.getTaxaReprovacaoGeralPercentual(), 0.001);
  }

  @Test
  public void deveCalcularReprovacaoDaDisciplinaComSeparacaoPorMotivo() throws Exception {
    salvarHistorico("ALUNO_001", "2026.1", "ES42", 8.0, 90.0, StatusAcademico.APROVADO);
    salvarHistorico("ALUNO_002", "2026.1", "ES42", 6.0, 90.0, StatusAcademico.RECUPERACAO);
    salvarHistorico("ALUNO_003", "2026.1", "ES42", 3.0, 90.0, StatusAcademico.REPROVADO_NOTA);
    salvarHistorico("ALUNO_004", "2026.1", "ES42", 8.0, 60.0, StatusAcademico.REPROVADO_FALTA);
    salvarHistorico("ALUNO_005", "2026.2", "ES42", 2.0, 50.0, StatusAcademico.REPROVADO_FALTA);

    ReprovacaoDisciplina indicador = relatorioService.calcularReprovacaoDisciplina("ES42");

    assertEquals("ES42", indicador.getCodigoDisciplina());
    assertEquals(5, indicador.getTotalRegistros());
    assertEquals(1, indicador.getTotalAprovados());
    assertEquals(1, indicador.getTotalRecuperacao());
    assertEquals(1, indicador.getTotalReprovadosPorNota());
    assertEquals(2, indicador.getTotalReprovadosPorFalta());
    assertEquals(3, indicador.getTotalReprovados());
    assertEquals(60.0, indicador.getTaxaReprovacaoPercentual(), 0.001);
    assertEquals(20.0, indicador.getTaxaReprovacaoPorNotaPercentual(), 0.001);
    assertEquals(40.0, indicador.getTaxaReprovacaoPorFaltaPercentual(), 0.001);
    assertFalse(indicador.isSemReprovacoes());
  }

  @Test
  public void deveGerarRelatorioConsolidadoOrdenadoPorDisciplina() {
    salvarHistorico("ALUNO_001", "2026.1", "ES42", 3.0, 90.0, StatusAcademico.REPROVADO_NOTA);
    salvarHistorico("ALUNO_002", "2026.1", "BD42", 8.0, 90.0, StatusAcademico.APROVADO);
    salvarHistorico("ALUNO_003", "2026.1", "BD42", 2.0, 60.0, StatusAcademico.REPROVADO_FALTA);

    RelatorioReprovacaoDisciplina relatorio =
        relatorioService.gerarRelatorioReprovacaoPorDisciplina();

    assertEquals(2, relatorio.getTotalDisciplinas());
    assertEquals("BD42", relatorio.getIndicadores().get(0).getCodigoDisciplina());
    assertEquals("ES42", relatorio.getIndicadores().get(1).getCodigoDisciplina());
    assertEquals(3, relatorio.getTotalRegistrosAnalisados());
    assertEquals(1, relatorio.getTotalAprovados());
    assertEquals(2, relatorio.getTotalReprovados());
    assertEquals(1, relatorio.getTotalReprovadosPorNota());
    assertEquals(1, relatorio.getTotalReprovadosPorFalta());
    assertEquals(66.666, relatorio.getTaxaReprovacaoGeralPercentual(), 0.001);
  }

  @Test
  public void deveFiltrarRelatorioPorDisciplinaIgnorandoMaiusculasEMinusculas() throws Exception {
    salvarHistorico("ALUNO_001", "2026.1", "ES42", 3.0, 90.0, StatusAcademico.REPROVADO_NOTA);
    salvarHistorico("ALUNO_002", "2026.1", "BD42", 2.0, 60.0, StatusAcademico.REPROVADO_FALTA);

    RelatorioReprovacaoDisciplina relatorio =
        relatorioService.gerarRelatorioReprovacaoPorDisciplina("es42");

    assertEquals(1, relatorio.getTotalDisciplinas());
    assertEquals("ES42", relatorio.getIndicadores().get(0).getCodigoDisciplina());
    assertEquals(1, relatorio.getTotalReprovadosPorNota());
    assertEquals(0, relatorio.getTotalReprovadosPorFalta());
  }

  @Test
  public void deveRetornarRelatorioVazioQuandoDisciplinaNaoPossuirHistorico() throws Exception {
    salvarHistorico("ALUNO_001", "2026.1", "ES42", 8.0, 90.0, StatusAcademico.APROVADO);

    RelatorioReprovacaoDisciplina relatorio =
        relatorioService.gerarRelatorioReprovacaoPorDisciplina("NAO_EXISTE");

    assertTrue(relatorio.isVazio());
    assertEquals(0, relatorio.getTotalRegistrosAnalisados());
  }

  @Test
  public void deveLancarExcecaoNoCalculoIndividualQuandoDisciplinaNaoPossuirHistorico()
      throws Exception {
    try {
      relatorioService.calcularReprovacaoDisciplina("NAO_EXISTE");
      fail("Deveria lancar ValidacaoException para disciplina sem historico.");
    } catch (ValidacaoException e) {
      assertTrue(e.getMessage().contains("Nao ha historico consolidado"));
    }
  }

  @Test
  public void deveValidarCodigoDisciplinaObrigatorio() throws Exception {
    assertCodigoDisciplinaObrigatorio(null);
    assertCodigoDisciplinaObrigatorio(" ");
  }

  @Test
  public void deveCalcularTaxasZeradasQuandoDisciplinaNaoPossuirReprovacoes() throws Exception {
    salvarHistorico("ALUNO_001", "2026.1", "ES42", 8.0, 90.0, StatusAcademico.APROVADO);
    salvarHistorico("ALUNO_002", "2026.1", "ES42", 5.5, 90.0, StatusAcademico.RECUPERACAO);

    ReprovacaoDisciplina indicador = relatorioService.calcularReprovacaoDisciplina("ES42");

    assertEquals(2, indicador.getTotalRegistros());
    assertEquals(0, indicador.getTotalReprovados());
    assertEquals(0.0, indicador.getTaxaReprovacaoPercentual(), 0.001);
    assertTrue(indicador.isSemReprovacoes());
  }

  @Test
  public void deveExporIndicadoresComoListaImutavel() {
    salvarHistorico("ALUNO_001", "2026.1", "ES42", 8.0, 90.0, StatusAcademico.APROVADO);
    RelatorioReprovacaoDisciplina relatorio =
        relatorioService.gerarRelatorioReprovacaoPorDisciplina();

    try {
      relatorio.getIndicadores().clear();
      fail("A lista de indicadores deve ser imutavel.");
    } catch (UnsupportedOperationException e) {
      assertEquals(1, relatorio.getTotalDisciplinas());
    }
  }

  @Test
  public void deveExporMetodosDeFachadaNoHistoricoServiceParaRF42() throws Exception {
    salvarHistorico("ALUNO_001", "2026.1", "ES42", 3.0, 90.0, StatusAcademico.REPROVADO_NOTA);
    HistoricoService historicoService =
        new HistoricoService(historicoRepository, null, null, null, null);

    ReprovacaoDisciplina indicador = historicoService.calcularReprovacaoDisciplina("ES42");
    RelatorioReprovacaoDisciplina relatorio =
        historicoService.gerarRelatorioReprovacaoPorDisciplina("ES42");

    assertEquals(1, indicador.getTotalReprovadosPorNota());
    assertEquals(1, relatorio.getTotalDisciplinas());
    assertEquals(100.0, relatorio.getTaxaReprovacaoGeralPercentual(), 0.001);
  }

  @Test
  public void deveRecusarRepositorioNuloNaInicializacaoDoMotor() {
    try {
      new RelatorioReprovacaoDisciplinaService(null);
      fail("Deveria recusar repositorio de historico nulo.");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("historico"));
    }
  }

  @Test
  public void deveBuscarTodosHistoricosNoRepositorio() {
    salvarHistorico("ALUNO_001", "2026.1", "ES42", 8.0, 90.0, StatusAcademico.APROVADO);
    salvarHistorico("ALUNO_002", "2026.1", "BD42", 2.0, 60.0, StatusAcademico.REPROVADO_FALTA);

    List<Historico> historicos = historicoRepository.buscarTodos();

    assertEquals(2, historicos.size());
    assertEquals("ES42", historicos.get(0).getCodigoDisciplina());
    assertEquals("BD42", historicos.get(1).getCodigoDisciplina());
  }

  private void assertCodigoDisciplinaObrigatorio(String codigoDisciplina) throws Exception {
    try {
      relatorioService.gerarRelatorioReprovacaoPorDisciplina(codigoDisciplina);
      fail("Deveria lancar ValidacaoException para codigo de disciplina ausente.");
    } catch (ValidacaoException e) {
      assertTrue(e.getMessage().contains("codigo da disciplina"));
    }
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
}
