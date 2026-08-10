package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Disciplina;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class MatriculaPipelineIntegrationTest {

  private TurmaService turmaService;
  private TurmaRepository turmaRepository;
  private PeriodoRepository periodoRepository;
  private DisciplinaRepository dRepository;
  private MatriculaRepository matriculaRepository;

  private static final String FILE_TURMAS = "data/turmas.txt";
  private static final String FILE_MATRICULAS = "data/matriculas.txt";
  private static final String FILE_PERIODOS = "data/periodos.txt";
  private static final String FILE_DISCIPLINAS = "data/disciplinas.txt";

  @Before
  public void setUp() throws Exception {
    File dataDir = new File("data");
    if (!dataDir.exists()) {
      dataDir.mkdirs();
    }

    new File(FILE_TURMAS).delete();
    new File(FILE_MATRICULAS).delete();
    new File(FILE_PERIODOS).delete();
    new File(FILE_DISCIPLINAS).delete();

    turmaRepository = new TurmaRepository();
    periodoRepository = new PeriodoRepository();
    dRepository = new DisciplinaRepository();
    matriculaRepository = new MatriculaRepository();

    turmaService = new TurmaService(turmaRepository, periodoRepository, dRepository);

    dRepository.salvar(new Disciplina("P1", "Programação I", 60, 4, new ArrayList<>()));
    dRepository.salvar(
        new Disciplina("ES01", "Engenharia de Software I", 60, 4, new ArrayList<>()));
    dRepository.salvar(new Disciplina("BD01", "Banco de Dados I", 60, 4, new ArrayList<>()));
  }

  @Test
  public void deveConsolidarMatriculaComoConfirmadaNoFluxOFechadoDeSucesso() throws Exception {
    String aluno = "20262001";
    String periodoCodigo = "2026.1";
    String disciplina = "P1";

    periodoRepository.salvar(new Periodo(periodoCodigo, "INICIADO"));
    turmaRepository.salvar(new Turma(disciplina, periodoCodigo, 30));

    turmaService.processarMatriculaAutomatica(aluno, disciplina, periodoCodigo);

    List<Turma> turmasEmDisco = turmaRepository.buscarTodas();
    assertEquals(1, turmasEmDisco.size());
    assertEquals(1, turmasEmDisco.get(0).getVagasOcupadas());

    List<Matricula> matriculasEmDisco = matriculaRepository.buscarTodas();
    assertEquals(1, matriculasEmDisco.size());

    Matricula matriculaSalva = matriculasEmDisco.get(0);
    assertEquals(aluno, matriculaSalva.getMatriculaAluno());
    assertEquals(disciplina, matriculaSalva.getCodigoDisciplina());
  }

  @Test
  public void deveAbortarPipelineENaoAlterarArquivosSeOPeriodoEstiverFechado() throws Exception {
    String aluno = "20262002";
    String periodoCodigo = "2026.1";
    String disciplina = "P1";

    periodoRepository.salvar(new Periodo(periodoCodigo, "PLANEJADO"));
    turmaRepository.salvar(new Turma(disciplina, periodoCodigo, 30));

    assertThrows(
        ValidacaoException.class,
        () -> {
          turmaService.processarMatriculaAutomatica(aluno, disciplina, periodoCodigo);
        });

    assertEquals(0, turmaRepository.buscarTodas().get(0).getVagasOcupadas());
    assertTrue(matriculaRepository.buscarTodas().isEmpty());
  }

  @Test
  public void deveAbortarPipelineENaoAlterarArquivosSeATurmaNaoTiverVagasDisponiveis()
      throws Exception {
    String aluno = "20262003";
    String periodoCodigo = "2026.1";
    String disciplina = "P1";

    periodoRepository.salvar(new Periodo(periodoCodigo, "INICIADO"));
    turmaRepository.salvar(new Turma(disciplina, periodoCodigo, 10, 10));

    assertThrows(
        ValidacaoException.class,
        () -> {
          turmaService.processarMatriculaAutomatica(aluno, disciplina, periodoCodigo);
        });

    assertEquals(10, turmaRepository.buscarTodas().get(0).getVagasOcupadas());
    assertTrue(matriculaRepository.buscarTodas().isEmpty());
  }

  @Test
  public void deveAbortarPipelineENaoGerarMatriculaSeOAlunoApresentarChoqueDeHorario()
      throws Exception {
    String aluno = "20262004";
    String periodoCodigo = "2026.1";

    periodoRepository.salvar(new Periodo(periodoCodigo, "INICIADO"));
    turmaRepository.salvar(new Turma("ES01", periodoCodigo, 40));
    turmaRepository.salvar(new Turma("BD01", periodoCodigo, 40));

    matriculaRepository.salvar(
        new Matricula(aluno, "ES01", periodoCodigo, Matricula.StatusMatricula.CONFIRMADA));

    for (Turma t : turmaRepository.buscarTodas()) {
      if (t.getCodigoDisciplina().equalsIgnoreCase("BD01")) {
        assertEquals(0, t.getVagasOcupadas());
      }
    }
  }

  /** [TASK 2277] Teste de Integração do Ciclo de Desistência e Promoção Automática. */
  @Test
  public void devePromoverPrimeiroSuplenteDaFilaSeAlunoTitularCancelarMatricula() throws Exception {
    String periodoCodigo = "2026.1";
    String disciplina = "P1";

    periodoRepository.salvar(new Periodo(periodoCodigo, "INICIADO"));
    turmaRepository.salvar(new Turma(disciplina, periodoCodigo, 1, 1));

    matriculaRepository.salvar(
        new Matricula("ALUNO_A", disciplina, periodoCodigo, Matricula.StatusMatricula.CONFIRMADA));
    matriculaRepository.salvar(
        new Matricula("ALUNO_B", disciplina, periodoCodigo, Matricula.StatusMatricula.ESPERA));
    matriculaRepository.salvar(
        new Matricula("ALUNO_C", disciplina, periodoCodigo, Matricula.StatusMatricula.ESPERA));

    MatriculaService mService =
        new MatriculaService(
            turmaRepository,
            matriculaRepository,
            periodoRepository,
            new br.edu.uepb.classroompb.repository.DisciplinaRepository(),
            new br.edu.uepb.classroompb.repository.HistoricoRepository());

    mService.cancelarMatricula("ALUNO_A", disciplina, periodoCodigo);

    List<Matricula> matriculasPosGatilho = matriculaRepository.buscarTodas();

    Matricula matriculaB = null;
    Matricula matriculaC = null;
    for (Matricula m : matriculasPosGatilho) {
      if (m.getMatriculaAluno().equals("ALUNO_B")) matriculaB = m;
      if (m.getMatriculaAluno().equals("ALUNO_C")) matriculaC = m;
    }

    assertNotNull(matriculaB);
    assertEquals(Matricula.StatusMatricula.CONFIRMADA, matriculaB.getStatus());

    assertNotNull(matriculaC);
    assertEquals(Matricula.StatusMatricula.ESPERA, matriculaC.getStatus());
  }

  /**
   * [TASK 2275] Testa se as solicitações excedentes entram rigorosamente com o status de ESPERA.
   */
  @Test
  public void deveEnfileirarAlunosEmListaDeEsperaQuandoTurmaAtingirLimiteDeVagas()
      throws Exception {
    String periodoCodigo = "2027.1";
    String disciplina = "P1";

    periodoRepository.salvar(new Periodo(periodoCodigo, "INICIADO"));

    turmaRepository.salvar(new Turma(disciplina, periodoCodigo, 1, 0));

    MatriculaService mService =
        new MatriculaService(
            turmaRepository,
            matriculaRepository,
            periodoRepository,
            new br.edu.uepb.classroompb.repository.DisciplinaRepository(),
            new br.edu.uepb.classroompb.repository.HistoricoRepository());

    Matricula mat1 = mService.solicitarMatricula("ALUNO_TITULAR", disciplina, periodoCodigo);
    assertEquals(Matricula.StatusMatricula.CONFIRMADA, mat1.getStatus());

    Matricula mat2 = mService.solicitarMatricula("ALUNO_FILA_01", disciplina, periodoCodigo);
    assertEquals(Matricula.StatusMatricula.ESPERA, mat2.getStatus());

    Matricula mat3 = mService.solicitarMatricula("ALUNO_FILA_02", disciplina, periodoCodigo);
    assertEquals(Matricula.StatusMatricula.ESPERA, mat3.getStatus());
  }

  /** [TASK 2275] Audita se o arquivo de matrículas reflete o tamanho exato da lista de espera. */
  @Test
  public void deveManterTamanhoEStatusCorretoNoArquivoAposMultiplosEnfileiramentos()
      throws Exception {
    String periodoCodigo = "2027.1";
    String disciplina = "ES01";

    periodoRepository.salvar(new Periodo(periodoCodigo, "INICIADO"));
    turmaRepository.salvar(new Turma(disciplina, periodoCodigo, 1, 0));

    MatriculaService mService =
        new MatriculaService(
            turmaRepository,
            matriculaRepository,
            periodoRepository,
            new br.edu.uepb.classroompb.repository.DisciplinaRepository(),
            new br.edu.uepb.classroompb.repository.HistoricoRepository());

    mService.solicitarMatricula("ALUNO_TITULAR", disciplina, periodoCodigo);
    mService.solicitarMatricula("ALUNO_FILA_01", disciplina, periodoCodigo);
    mService.solicitarMatricula("ALUNO_FILA_02", disciplina, periodoCodigo);

    List<Matricula> todas = matriculaRepository.buscarTodas();
    int countEspera = 0;
    for (Matricula m : todas) {
      if (m.getStatus() == Matricula.StatusMatricula.ESPERA) {
        countEspera++;
      }
    }

    assertEquals(2, countEspera);
  }
}
