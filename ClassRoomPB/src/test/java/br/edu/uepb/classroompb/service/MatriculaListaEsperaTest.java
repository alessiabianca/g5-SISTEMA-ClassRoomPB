package br.edu.uepb.classroompb.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class MatriculaListaEsperaTest {

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

    br.edu.uepb.classroompb.repository.DisciplinaRepository dr =
        new br.edu.uepb.classroompb.repository.DisciplinaRepository();
    this.matriculaService =
        new MatriculaService(
            turmaRepository,
            matriculaRepository,
            periodoRepository,
            dr,
            new br.edu.uepb.classroompb.repository.HistoricoRepository());

    periodoRepository.salvar(new Periodo("2026.2", "INICIADO"));
    try {
      dr.salvar(
          new br.edu.uepb.classroompb.model.Disciplina(
              "ES01", "Engenharia de Software", 60, 4, new java.util.ArrayList<>()));
    } catch (Exception e) {
    }
  }

  @Test
  public void deveAdicionarAlunoNaListaDeEsperaQuandoTurmaEstiverLotada() throws Exception {

    Turma turma = new Turma("ES01", "2026.2", 1);
    turmaRepository.salvar(turma);

    Matricula mat1 = matriculaService.solicitarMatricula("ALUNO_001", "ES01", "2026.2");

    assertNotNull(mat1);
    assertEquals(Matricula.StatusMatricula.CONFIRMADA, mat1.getStatus());

    Matricula mat2 = matriculaService.solicitarMatricula("ALUNO_002", "ES01", "2026.2");

    assertNotNull(mat2);
    assertEquals(Matricula.StatusMatricula.ESPERA, mat2.getStatus());

    List<Matricula> todasAsMatriculasSalvas = matriculaRepository.buscarTodas();
    assertEquals(2, todasAsMatriculasSalvas.size());
  }
}
