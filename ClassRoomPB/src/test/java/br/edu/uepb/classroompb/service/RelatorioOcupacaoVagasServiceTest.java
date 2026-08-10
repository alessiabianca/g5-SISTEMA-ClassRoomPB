package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.OcupacaoVagasTurma;
import br.edu.uepb.classroompb.model.RelatorioOcupacaoVagas;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import org.junit.Before;
import org.junit.Test;

public class RelatorioOcupacaoVagasServiceTest {
  private TurmaRepository turmaRepository;
  private MatriculaRepository matriculaRepository;
  private RelatorioOcupacaoVagasService relatorioService;

  @Before
  public void setUp() {
    File dataDir = new File("data");
    if (!dataDir.exists()) {
      dataDir.mkdirs();
    }

    new File("data/turmas.txt").delete();
    new File("data/matriculas.txt").delete();

    turmaRepository = new TurmaRepository();
    matriculaRepository = new MatriculaRepository();
    relatorioService = new RelatorioOcupacaoVagasService(turmaRepository, matriculaRepository);
  }

  @Test
  public void deveRetornarRelatorioVazioQuandoNaoHouverTurmasOfertadas() {
    RelatorioOcupacaoVagas relatorio = relatorioService.gerarRelatorioOcupacaoVagas();

    assertTrue(relatorio.isVazio());
    assertEquals(0, relatorio.getTotalTurmas());
    assertEquals(0, relatorio.getTetoTotalVagas());
    assertEquals(0, relatorio.getTotalVagasOcupadas());
    assertEquals(0, relatorio.getTotalVagasDisponiveis());
    assertEquals(0, relatorio.getTotalAlunosEmEspera());
    assertEquals(0.0, relatorio.getDensidadeGeralPercentual(), 0.001);
    assertEquals(0.0, relatorio.getDensidadeMediaPercentual(), 0.001);
  }

  @Test
  public void deveCalcularDensidadeETetoDaTurmaComBaseNasMatriculasAtivas() throws Exception {
    turmaRepository.salvar(new Turma("ES41", "2026.2", 4));
    matriculaRepository.salvar(
        new Matricula("ALUNO_001", "ES41", "2026.2", Matricula.StatusMatricula.CONFIRMADA));
    matriculaRepository.salvar(
        new Matricula("ALUNO_002", "ES41", "2026.2", Matricula.StatusMatricula.CONFIRMADA));
    matriculaRepository.salvar(
        new Matricula("ALUNO_003", "ES41", "2026.2", Matricula.StatusMatricula.SOLICITADA));
    matriculaRepository.salvar(
        new Matricula("ALUNO_FILA", "ES41", "2026.2", Matricula.StatusMatricula.ESPERA));
    matriculaRepository.salvar(
        new Matricula("ALUNO_REJ", "ES41", "2026.2", Matricula.StatusMatricula.REJEITADA));

    OcupacaoVagasTurma ocupacao = relatorioService.calcularOcupacaoVagasTurma("ES41", "2026.2");

    assertEquals(4, ocupacao.getTetoVagas());
    assertEquals(3, ocupacao.getVagasOcupadas());
    assertEquals(1, ocupacao.getVagasDisponiveis());
    assertEquals(1, ocupacao.getAlunosEmEspera());
    assertEquals(0, ocupacao.getOcupacaoExcedente());
    assertEquals(75.0, ocupacao.getDensidadePercentual(), 0.001);
    assertFalse(ocupacao.isLotada());
    assertFalse(ocupacao.isAcimaDoTeto());
    assertTrue(ocupacao.isComListaEspera());
  }

  @Test
  public void deveIgnorarMatriculasDeOutrasTurmasEPeriodosNoCalculoIndividual() throws Exception {
    turmaRepository.salvar(new Turma("ES41", "2026.2", 3));
    turmaRepository.salvar(new Turma("ES41", "2027.1", 3));
    turmaRepository.salvar(new Turma("BD41", "2026.2", 3));

    matriculaRepository.salvar(
        new Matricula("ALUNO_ALVO", "ES41", "2026.2", Matricula.StatusMatricula.CONFIRMADA));
    matriculaRepository.salvar(
        new Matricula("ALUNO_OUTRO_PER", "ES41", "2027.1", Matricula.StatusMatricula.CONFIRMADA));
    matriculaRepository.salvar(
        new Matricula("ALUNO_OUTRA_DISC", "BD41", "2026.2", Matricula.StatusMatricula.CONFIRMADA));

    OcupacaoVagasTurma ocupacao = relatorioService.calcularOcupacaoVagasTurma("ES41", "2026.2");

    assertEquals(1, ocupacao.getVagasOcupadas());
    assertEquals(2, ocupacao.getVagasDisponiveis());
    assertEquals(33.333, ocupacao.getDensidadePercentual(), 0.001);
  }

  @Test
  public void deveConsolidarRelatorioDeOcupacaoPorPeriodo() throws Exception {
    turmaRepository.salvar(new Turma("ES41", "2026.2", 10));
    turmaRepository.salvar(new Turma("BD41", "2026.2", 5));
    turmaRepository.salvar(new Turma("IA41", "2027.1", 20));

    salvarMatriculasConfirmadas("ES_ALUNO_", "ES41", "2026.2", 3);
    salvarMatriculasConfirmadas("BD_ALUNO_", "BD41", "2026.2", 5);
    salvarMatriculasConfirmadas("IA_ALUNO_", "IA41", "2027.1", 1);
    matriculaRepository.salvar(
        new Matricula("BD_FILA", "BD41", "2026.2", Matricula.StatusMatricula.ESPERA));

    RelatorioOcupacaoVagas relatorio =
        relatorioService.gerarRelatorioOcupacaoVagasPorPeriodo("2026.2");

    assertEquals(2, relatorio.getTotalTurmas());
    assertEquals(15, relatorio.getTetoTotalVagas());
    assertEquals(8, relatorio.getTotalVagasOcupadas());
    assertEquals(7, relatorio.getTotalVagasDisponiveis());
    assertEquals(1, relatorio.getTotalAlunosEmEspera());
    assertEquals(1, relatorio.getTurmasLotadas());
    assertEquals(1, relatorio.getTurmasComListaEspera());
    assertEquals(0, relatorio.getTurmasAcimaDoTeto());
    assertEquals(53.333, relatorio.getDensidadeGeralPercentual(), 0.001);
    assertEquals(65.0, relatorio.getDensidadeMediaPercentual(), 0.001);
  }

  @Test
  public void deveConsolidarRelatorioGeralComTurmasDeTodosOsPeriodos() {
    turmaRepository.salvar(new Turma("ES41", "2026.2", 4));
    turmaRepository.salvar(new Turma("BD41", "2027.1", 6));
    salvarMatriculasConfirmadas("ES_ALUNO_", "ES41", "2026.2", 2);
    salvarMatriculasConfirmadas("BD_ALUNO_", "BD41", "2027.1", 3);
    matriculaRepository.salvar(
        new Matricula("BD_FILA", "BD41", "2027.1", Matricula.StatusMatricula.ESPERA));

    RelatorioOcupacaoVagas relatorio = relatorioService.gerarRelatorioOcupacaoVagas();

    assertEquals(2, relatorio.getTotalTurmas());
    assertEquals(10, relatorio.getTetoTotalVagas());
    assertEquals(5, relatorio.getTotalVagasOcupadas());
    assertEquals(5, relatorio.getTotalVagasDisponiveis());
    assertEquals(1, relatorio.getTotalAlunosEmEspera());
    assertEquals(50.0, relatorio.getDensidadeGeralPercentual(), 0.001);
    assertEquals(50.0, relatorio.getDensidadeMediaPercentual(), 0.001);
  }

  @Test
  public void deveRetornarRelatorioVazioQuandoPeriodoNaoPossuirTurmas() throws Exception {
    turmaRepository.salvar(new Turma("ES41", "2026.2", 4));

    RelatorioOcupacaoVagas relatorio =
        relatorioService.gerarRelatorioOcupacaoVagasPorPeriodo("2030.1");

    assertTrue(relatorio.isVazio());
    assertEquals(0, relatorio.getTotalTurmas());
    assertEquals(0, relatorio.getTetoTotalVagas());
    assertEquals(0.0, relatorio.getDensidadeGeralPercentual(), 0.001);
  }

  @Test
  public void deveIndicarExcedenteQuandoOcupacaoUltrapassarOTeto() throws Exception {
    turmaRepository.salvar(new Turma("LOT41", "2026.2", 2));
    salvarMatriculasConfirmadas("LOT_ALUNO_", "LOT41", "2026.2", 3);

    OcupacaoVagasTurma ocupacao = relatorioService.calcularOcupacaoVagasTurma("LOT41", "2026.2");

    assertEquals(2, ocupacao.getTetoVagas());
    assertEquals(3, ocupacao.getVagasOcupadas());
    assertEquals(0, ocupacao.getVagasDisponiveis());
    assertEquals(1, ocupacao.getOcupacaoExcedente());
    assertEquals(150.0, ocupacao.getDensidadePercentual(), 0.001);
    assertTrue(ocupacao.isLotada());
    assertTrue(ocupacao.isAcimaDoTeto());
  }

  @Test
  public void deveEvitarDivisaoPorZeroQuandoTurmaNaoPossuirTetoDeVagas() throws Exception {
    turmaRepository.salvar(new Turma("ZERO41", "2026.2", 0));
    matriculaRepository.salvar(
        new Matricula("ALUNO_001", "ZERO41", "2026.2", Matricula.StatusMatricula.CONFIRMADA));

    OcupacaoVagasTurma ocupacao = relatorioService.calcularOcupacaoVagasTurma("ZERO41", "2026.2");

    assertEquals(0, ocupacao.getTetoVagas());
    assertEquals(1, ocupacao.getVagasOcupadas());
    assertEquals(0, ocupacao.getVagasDisponiveis());
    assertEquals(1, ocupacao.getOcupacaoExcedente());
    assertEquals(0.0, ocupacao.getDensidadePercentual(), 0.001);
    assertFalse(ocupacao.isLotada());
    assertTrue(ocupacao.isAcimaDoTeto());
  }

  @Test
  public void deveLancarExcecaoQuandoTurmaDoCalculoIndividualNaoExistir() throws Exception {
    try {
      relatorioService.calcularOcupacaoVagasTurma("NAO_EXISTE", "2026.2");
      fail("Deveria lancar ValidacaoException para turma inexistente.");
    } catch (ValidacaoException e) {
      assertTrue(e.getMessage().contains("turma informada nao existe"));
    }
  }

  @Test
  public void deveValidarCamposObrigatoriosParaConsultaIndividual() throws Exception {
    assertCampoObrigatorioCalculoIndividual("", "2026.2", "disciplina");
    assertCampoObrigatorioCalculoIndividual("ES41", "   ", "periodo");
  }

  @Test
  public void deveValidarPeriodoObrigatorioNoRelatorioFiltrado() throws Exception {
    try {
      relatorioService.gerarRelatorioOcupacaoVagasPorPeriodo(" ");
      fail("Deveria lancar ValidacaoException para periodo em branco.");
    } catch (ValidacaoException e) {
      assertTrue(e.getMessage().contains("periodo"));
    }
  }

  @Test
  public void deveExporListaDeOcupacoesComoImutavel() {
    turmaRepository.salvar(new Turma("ES41", "2026.2", 4));
    RelatorioOcupacaoVagas relatorio = relatorioService.gerarRelatorioOcupacaoVagas();

    try {
      relatorio.getOcupacoes().clear();
      fail("A lista do relatorio deve ser imutavel para preservar o consolidado.");
    } catch (UnsupportedOperationException e) {
      assertEquals(1, relatorio.getTotalTurmas());
    }
  }

  @Test
  public void deveExporMetodosDeFachadaNoTurmaServiceParaRF41() throws Exception {
    turmaRepository.salvar(new Turma("ES41", "2026.2", 4));
    salvarMatriculasConfirmadas("ES_ALUNO_", "ES41", "2026.2", 2);
    TurmaService turmaService =
        new TurmaService(turmaRepository, new PeriodoRepository(), new DisciplinaRepository());

    OcupacaoVagasTurma ocupacao = turmaService.calcularOcupacaoVagasTurma("ES41", "2026.2");
    RelatorioOcupacaoVagas relatorio = turmaService.gerarRelatorioOcupacaoVagasPorPeriodo("2026.2");

    assertEquals(2, ocupacao.getVagasOcupadas());
    assertEquals(50.0, ocupacao.getDensidadePercentual(), 0.001);
    assertEquals(1, relatorio.getTotalTurmas());
    assertEquals(2, relatorio.getTotalVagasOcupadas());
  }

  @Test
  public void deveRecusarRepositoriosNulosNaInicializacaoDoMotor() {
    try {
      new RelatorioOcupacaoVagasService(null, matriculaRepository);
      fail("Deveria recusar repositorio de turmas nulo.");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Repositorios"));
    }

    try {
      new RelatorioOcupacaoVagasService(turmaRepository, null);
      fail("Deveria recusar repositorio de matriculas nulo.");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Repositorios"));
    }
  }

  private void assertCampoObrigatorioCalculoIndividual(
      String codigoDisciplina, String periodo, String campoEsperado) throws Exception {
    try {
      relatorioService.calcularOcupacaoVagasTurma(codigoDisciplina, periodo);
      fail("Deveria lancar ValidacaoException para campo obrigatorio ausente.");
    } catch (ValidacaoException e) {
      assertTrue(e.getMessage().contains(campoEsperado));
    }
  }

  private void salvarMatriculasConfirmadas(
      String prefixo, String codigoDisciplina, String periodo, int quantidade) {
    for (int i = 1; i <= quantidade; i++) {
      matriculaRepository.salvar(
          new Matricula(
              prefixo + i, codigoDisciplina, periodo, Matricula.StatusMatricula.CONFIRMADA));
    }
  }
}
