package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

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

public class MatriculaFilaCronologiaTest {

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

    periodoRepository.salvar(new Periodo("2027.1", "INICIADO"));
    try {
      dr.salvar(
          new br.edu.uepb.classroompb.model.Disciplina(
              "ES30", "Engenharia", 60, 4, new java.util.ArrayList<>()));
    } catch (Exception e) {
    }
  }

  @Test
  public void deveManterOrdemCronologicaEstritaFilaEsperaFIFO() throws Exception {

    turmaRepository.salvar(new Turma("ES30", "2027.1", 1));

    matriculaService.solicitarMatricula("ALUNO_A", "ES30", "2027.1");

    matriculaService.solicitarMatricula("ALUNO_B", "ES30", "2027.1");
    matriculaService.solicitarMatricula("ALUNO_C", "ES30", "2027.1");
    matriculaService.solicitarMatricula("ALUNO_D", "ES30", "2027.1");

    List<Matricula> salvas = matriculaRepository.buscarTodas();

    assertEquals(
        "O arquivo plano deve conter exatamente 4 registros persistidos", 4, salvas.size());

    assertEquals("ALUNO_A", salvas.get(0).getMatriculaAluno());
    assertEquals(Matricula.StatusMatricula.CONFIRMADA, salvas.get(0).getStatus());

    assertEquals(
        "O primeiro elemento da fila (índice 1) deve ser o ALUNO_B",
        "ALUNO_B",
        salvas.get(1).getMatriculaAluno());
    assertEquals(
        "O status do ALUNO_B deve ser ESPERA",
        Matricula.StatusMatricula.ESPERA,
        salvas.get(1).getStatus());

    assertEquals(
        "O segundo elemento da fila (índice 2) deve ser o ALUNO_C",
        "ALUNO_C",
        salvas.get(2).getMatriculaAluno());
    assertEquals(
        "O status do ALUNO_C deve ser ESPERA",
        Matricula.StatusMatricula.ESPERA,
        salvas.get(2).getStatus());

    assertEquals(
        "O terceiro elemento da fila (índice 3) deve ser o ALUNO_D",
        "ALUNO_D",
        salvas.get(3).getMatriculaAluno());
    assertEquals(
        "O status do ALUNO_D deve ser ESPERA",
        Matricula.StatusMatricula.ESPERA,
        salvas.get(3).getStatus());
  }
}