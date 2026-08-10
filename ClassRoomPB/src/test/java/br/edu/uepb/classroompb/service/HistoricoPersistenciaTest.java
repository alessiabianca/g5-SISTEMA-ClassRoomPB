package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.*;
import br.edu.uepb.classroompb.repository.*;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HistoricoPersistenciaTest {

  private PeriodoRepository periodoRepository;
  private DisciplinaRepository disciplinaRepository;
  private TurmaRepository turmaRepository;
  private MatriculaRepository matriculaRepository;
  private NotaRepository notaRepository;
  private FrequenciaRepository frequenciaRepository;
  private HistoricoRepository historicoRepository;

  private TurmaService turmaService;
  private MatriculaService matriculaService;
  private NotaService notaService;
  private FrequenciaService frequenciaService;
  private SituacaoAcademicaService situacaoService;
  private HistoricoService historicoService;
  private PeriodoService periodoService;

  @Before
  public void setUp() throws Exception {
    periodoRepository = new PeriodoRepository();
    disciplinaRepository = new DisciplinaRepository();
    turmaRepository = new TurmaRepository();
    matriculaRepository = new MatriculaRepository();
    notaRepository = new NotaRepository();
    frequenciaRepository = new FrequenciaRepository();
    historicoRepository = new HistoricoRepository();

    turmaService = new TurmaService(turmaRepository, periodoRepository, disciplinaRepository);
    matriculaService =
        new MatriculaService(
            turmaRepository,
            matriculaRepository,
            periodoRepository,
            new br.edu.uepb.classroompb.repository.DisciplinaRepository(),
            new br.edu.uepb.classroompb.repository.HistoricoRepository());
    notaService =
        new NotaService(notaRepository, turmaRepository, matriculaRepository, periodoRepository);
    frequenciaService =
        new FrequenciaService(
            turmaRepository, matriculaRepository, frequenciaRepository, notaRepository, new br.edu.uepb.classroompb.repository.DiarioRepository(), new br.edu.uepb.classroompb.repository.AulaRepository());
    situacaoService = new SituacaoAcademicaService(notaRepository, frequenciaService);
    historicoService =
        new HistoricoService(
            historicoRepository,
            matriculaRepository,
            turmaRepository,
            situacaoService,
            frequenciaService);
    periodoService = new PeriodoService(periodoRepository, historicoService);

    Periodo p = new Periodo("2026.HIST", "INICIADO");
    periodoRepository.salvar(p);

    Disciplina d = new Disciplina("D_HIST", "História", 60, 4, null);
    disciplinaRepository.salvar(d);

    Turma t = new Turma("D_HIST", "2026.HIST", 10);
    turmaRepository.salvar(t);

    Matricula m1 =
        new Matricula("ALUNO1", "D_HIST", "2026.HIST", Matricula.StatusMatricula.CONFIRMADA);
    matriculaRepository.salvar(m1);

    Nota n1 = new Nota("ALUNO1", "D_HIST", "2026.HIST", 8.0, 8.0, -1.0);
    notaRepository.salvar(n1);

    frequenciaRepository.salvarLote(
        List.of(
            new Frequencia(
                "AULA_01",
                "DIARIO_01",
                "10/10/2026",
                "ALUNO1",
                "D_HIST",
                "2026.HIST",
                Frequencia.TipoFrequencia.PRESENCA)));

    Matricula m2 =
        new Matricula("ALUNO2", "D_HIST", "2026.HIST", Matricula.StatusMatricula.CONFIRMADA);
    matriculaRepository.salvar(m2);

    frequenciaRepository.salvarLote(
        List.of(
            new Frequencia(
                "AULA_01",
                "DIARIO_01",
                "10/10/2026",
                "ALUNO2",
                "D_HIST",
                "2026.HIST",
                Frequencia.TipoFrequencia.PRESENCA)));
  }

  @After
  public void tearDown() {
    new File("data/periodos.txt").delete();
    new File("data/disciplinas.txt").delete();
    new File("data/turmas.txt").delete();
    new File("data/matriculas.txt").delete();
    new File("data/notas.txt").delete();
    new File("data/frequencias.txt").delete();
    new File("data/historico.txt").delete();
  }

  @Test
  public void deveGerarHistoricoParaAlunosAoEncerrarPeriodo() throws ValidacaoException {

    periodoService.encerrarPeriodo("2026.HIST");

    List<Historico> h1 = historicoService.consultarHistorico("ALUNO1");
    assertEquals(1, h1.size());
    assertEquals(8.0, h1.get(0).getMediaFinal(), 0.01);
    assertEquals("N/A", h1.get(0).getMatriculaProfessor());
    assertEquals(StatusAcademico.APROVADO, h1.get(0).getStatus());

    List<Historico> h2 = historicoService.consultarHistorico("ALUNO2");
    assertEquals(1, h2.size());
    assertEquals(0.0, h2.get(0).getMediaFinal(), 0.01);
    assertEquals(StatusAcademico.REPROVADO_NOTA, h2.get(0).getStatus());
  }
}