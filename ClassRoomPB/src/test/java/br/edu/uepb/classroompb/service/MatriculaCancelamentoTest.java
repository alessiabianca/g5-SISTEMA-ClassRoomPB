package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class MatriculaCancelamentoTest {

  private TurmaRepository turmaRepository;
  private MatriculaRepository matriculaRepository;
  private PeriodoRepository periodoRepository;
  private MatriculaService matriculaService;

  @Before
  public void setUp() throws Exception {

    File dataDir = new File("data");
    if (!dataDir.exists()) {
      dataDir.mkdirs();
    }

    new File("data/turmas.txt").delete();
    new File("data/matriculas.txt").delete();
    new File("data/periodos.txt").delete();

    this.turmaRepository = new TurmaRepository();
    this.matriculaRepository = new MatriculaRepository();
    this.periodoRepository = new PeriodoRepository();

    this.matriculaService =
        new MatriculaService(
            turmaRepository,
            matriculaRepository,
            periodoRepository,
            new br.edu.uepb.classroompb.repository.DisciplinaRepository(),
            new br.edu.uepb.classroompb.repository.HistoricoRepository());

    periodoRepository.salvar(new Periodo("2026.2", "INICIADO"));
    periodoRepository.salvar(new Periodo("2026.1", "ENCERRADO"));

    turmaRepository.salvar(new Turma("ES01", "2026.2", 10));
    turmaRepository.salvar(new Turma("ES02", "2026.1", 10));
  }

  @Test
  public void deveCancelarMatriculaComSucessoDentroDoPrazo() throws Exception {
    Matricula mat =
        new Matricula("ALUNO_001", "ES01", "2026.2", Matricula.StatusMatricula.CONFIRMADA);
    matriculaRepository.salvar(mat);

    assertEquals(
        "A matrícula inicial deve estar salva", 1, matriculaRepository.buscarTodas().size());

    matriculaService.cancelarMatricula("ALUNO_001", "ES01", "2026.2");

    List<Matricula> salvas = matriculaRepository.buscarTodas();
    assertTrue("A lista de matrículas deve estar vazia após o cancelamento", salvas.isEmpty());
  }

  @Test
  public void deveLancarExcecaoAoTentarCancelarMatriculaForaDoPrazo() throws Exception {
    Matricula mat =
        new Matricula("ALUNO_001", "ES02", "2026.1", Matricula.StatusMatricula.CONFIRMADA);
    matriculaRepository.salvar(mat);

    try {
      matriculaService.cancelarMatricula("ALUNO_001", "ES02", "2026.1");
      fail("Deveria ter lançado ValidacaoException por estar fora do prazo permitido.");
    } catch (ValidacaoException e) {
      assertTrue(e.getMessage().contains("não está aberto para modificações"));
    }

    assertEquals("A matrícula não deve ser removida", 1, matriculaRepository.buscarTodas().size());
  }

  @Test
  public void deveLancarExcecaoAoCancelarMatriculaInexistente() throws Exception {
    try {
      matriculaService.cancelarMatricula("ALUNO_FANTASMA", "DISCIPLINA_99", "2026.2");
      fail("Deveria ter lançado ValidacaoException por não encontrar a matrícula solicitada.");
    } catch (ValidacaoException e) {
      assertTrue(e.getMessage().contains("Matrícula não encontrada"));
    }
  }
}
