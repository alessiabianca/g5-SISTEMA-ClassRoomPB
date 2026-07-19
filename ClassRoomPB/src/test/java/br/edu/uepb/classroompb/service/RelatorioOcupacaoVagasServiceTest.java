package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.OcupacaoVagasTurma;
import br.edu.uepb.classroompb.model.RelatorioOcupacaoVagas;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
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
  public void deveCalcularDensidadeETetoDaTurmaComBaseNasMatriculasAtivas() throws Exception {
    turmaRepository.salvar(new Turma("ES41", "PROF_RF41", "2026.2", 4, "24M12", "Sala_RF41"));
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
  public void deveConsolidarRelatorioDeOcupacaoPorPeriodo() throws Exception {
    turmaRepository.salvar(new Turma("ES41", "PROF_A", "2026.2", 10, "24M12", "Sala_A"));
    turmaRepository.salvar(new Turma("BD41", "PROF_B", "2026.2", 5, "35M12", "Sala_B"));
    turmaRepository.salvar(new Turma("IA41", "PROF_C", "2027.1", 20, "46M12", "Sala_C"));

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
  public void deveIndicarExcedenteQuandoOcupacaoUltrapassarOTeto() throws Exception {
    turmaRepository.salvar(new Turma("LOT41", "PROF_RF41", "2026.2", 2, "24M12", "Sala_LOT"));
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
  public void deveLancarExcecaoQuandoTurmaDoCalculoIndividualNaoExistir() throws Exception {
    try {
      relatorioService.calcularOcupacaoVagasTurma("NAO_EXISTE", "2026.2");
      fail("Deveria lancar ValidacaoException para turma inexistente.");
    } catch (ValidacaoException e) {
      assertTrue(e.getMessage().contains("turma informada nao existe"));
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
