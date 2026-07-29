package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class NotaServiceTest {

  private NotaService notaService;
  private NotaRepository notaRepository;
  private TurmaRepository turmaRepository;
  private MatriculaRepository matriculaRepository;
  private PeriodoRepository periodoRepository;

  private static final String FILE_NOTAS = "data/notas.txt";
  private static final String FILE_TURMAS = "data/turmas.txt";
  private static final String FILE_MATRICULAS = "data/matriculas.txt";
  private static final String FILE_PERIODOS = "data/periodos.txt";

  @Before
  public void setUp() throws Exception {

    File dataDir = new File("data");
    if (!dataDir.exists()) {
      dataDir.mkdirs();
    }

    new File(FILE_NOTAS).delete();
    new File(FILE_TURMAS).delete();
    new File(FILE_MATRICULAS).delete();
    new File(FILE_PERIODOS).delete();

    notaRepository = new NotaRepository();
    turmaRepository = new TurmaRepository();
    matriculaRepository = new MatriculaRepository();
    periodoRepository = new PeriodoRepository();

    notaService =
        new NotaService(notaRepository, turmaRepository, matriculaRepository, periodoRepository);
  }

  /**
   * FLUXO DE SUCESSO: Garante que o professor responsável consiga lançar uma nota válida (no
   * intervalo de 0.0 a 10.0) com sucesso.
   */
  @Test
  public void deveLancarNotaComSucessoParaProfessorResponsavel() throws Exception {
    String professorResponsavel = "PROF_A";
    String aluno = "20261001";
    String disciplina = "P1";
    String periodo = "2026.1";

    turmaRepository.salvar(
        new Turma(disciplina, professorResponsavel, periodo, 30, "24M12", "Sala_101"));

    notaService.lancarNota(professorResponsavel, aluno, disciplina, periodo, 1, 8.5);

    Nota notaSalva = notaRepository.buscarPorAlunoEDisciplina(aluno, disciplina, periodo);
    assertNotNull(notaSalva);
    assertEquals(8.5, notaSalva.getNota1(), 0.01);
  }

  /**
   * FLUXO DE FALHA (NOTA NEGATIVA): Garante o bloqueio de lançamento de notas abaixo de 0.0,
   * estourando ValidacaoException.
   */
  @Test
  public void deveLancarValidacaoExceptionParaNotaNegativa() throws Exception {
    String professorResponsavel = "PROF_A";
    String aluno = "20261002";
    String disciplina = "P1";
    String periodo = "2026.1";

    turmaRepository.salvar(
        new Turma(disciplina, professorResponsavel, periodo, 30, "24M12", "Sala_101"));

    assertThrows(
        ValidacaoException.class,
        () -> {
          notaService.lancarNota(professorResponsavel, aluno, disciplina, periodo, 1, -1.5);
        });

    assertTrue(notaRepository.buscarTodas().isEmpty());
  }

  /**
   * FLUXO DE FALHA (NOTA MAIOR QUE DEZ): Garante o bloqueio de lançamento de notas acima de 10.0,
   * estourando ValidacaoException.
   */
  @Test
  public void deveLancarValidacaoExceptionParaNotaMaiorQueDez() throws Exception {
    String professorResponsavel = "PROF_A";
    String aluno = "20261003";
    String disciplina = "P1";
    String periodo = "2026.1";

    turmaRepository.salvar(
        new Turma(disciplina, professorResponsavel, periodo, 30, "24M12", "Sala_101"));

    assertThrows(
        ValidacaoException.class,
        () -> {
          notaService.lancarNota(professorResponsavel, aluno, disciplina, periodo, 1, 10.5);
        });

    assertTrue(notaRepository.buscarTodas().isEmpty());
  }

  /**
   * FLUXO DE FALHA (PERMISSÃO / SEGURANÇA): Garante que um professor não associado à turma seja
   * bloqueado de lançar notas para os alunos dela.
   */
  @Test
  public void deveLancarValidacaoExceptionParaProfessorNaoResponsavel() throws Exception {
    String professorResponsavel = "PROF_A";
    String professorInvasor = "PROF_B";
    String aluno = "20261004";
    String disciplina = "P1";
    String periodo = "2026.1";

    turmaRepository.salvar(
        new Turma(disciplina, professorResponsavel, periodo, 30, "24M12", "Sala_101"));

    assertThrows(
        ValidacaoException.class,
        () -> {
          notaService.lancarNota(professorInvasor, aluno, disciplina, periodo, 1, 9.0);
        });

    assertTrue(notaRepository.buscarTodas().isEmpty());
  }

  /**
   * TESTE [TASK 2526]: Garante que a busca por notas do período filtre e retorne com precisão
   * apenas as notas correspondentes ao aluno solicitado.
   */
  @Test
  public void deveRecuperarNotasComSucessoApenasDoAlunoEspecificado() throws Exception {
    String alunoAlvo = "20261001";
    String outroAluno = "20261002";
    String disciplina = "P1";
    String periodo = "2026.1";

    turmaRepository.salvar(new Turma(disciplina, "PROF_A", periodo, 30, "24M12", "Sala_101"));
    matriculaRepository.salvar(
        new br.edu.uepb.classroompb.model.Matricula(
            alunoAlvo,
            disciplina,
            periodo,
            br.edu.uepb.classroompb.model.Matricula.StatusMatricula.CONFIRMADA));
    matriculaRepository.salvar(
        new br.edu.uepb.classroompb.model.Matricula(
            outroAluno,
            disciplina,
            periodo,
            br.edu.uepb.classroompb.model.Matricula.StatusMatricula.CONFIRMADA));

    notaRepository.salvar(new Nota(alunoAlvo, disciplina, periodo, 8.0, 9.0, -1.0));
    notaRepository.salvar(new Nota(outroAluno, disciplina, periodo, 5.0, 6.0, -1.0));

    List<Nota> boletimAlunoAlvo = notaService.buscarNotasPorAlunoEPeriodo(alunoAlvo, periodo);

    assertEquals(1, boletimAlunoAlvo.size());
    Nota notaRetornada = boletimAlunoAlvo.get(0);
    assertEquals(alunoAlvo, notaRetornada.getMatriculaAluno());
    assertEquals(8.0, notaRetornada.getNota1(), 0.01);
    assertEquals(9.0, notaRetornada.getNota2(), 0.01);
  }

  /**
   * TESTE [TASK 2526]: Garante o isolamento absoluto, comprovando que a consulta de notas de um
   * aluno não retorne ou exponha nenhum dado de notas pertencente a terceiros.
   */
  @Test
  public void deveGarantirIsolamentoEEvitarVazamentoDeNotasDeTerceiros() throws Exception {
    String alunoLogado = "20261001";
    String alunoEstranho = "20269999";
    String disciplina = "P1";
    String periodo = "2026.1";

    turmaRepository.salvar(new Turma(disciplina, "PROF_A", periodo, 30, "24M12", "Sala_101"));
    matriculaRepository.salvar(
        new br.edu.uepb.classroompb.model.Matricula(
            alunoLogado,
            disciplina,
            periodo,
            br.edu.uepb.classroompb.model.Matricula.StatusMatricula.CONFIRMADA));

    notaRepository.salvar(new Nota(alunoEstranho, disciplina, periodo, 10.0, 10.0, -1.0));

    List<Nota> boletim = notaService.buscarNotasPorAlunoEPeriodo(alunoLogado, periodo);

    assertEquals(1, boletim.size());
    Nota notaRetornada = boletim.get(0);
    assertEquals(alunoLogado, notaRetornada.getMatriculaAluno());
    assertEquals(0.0, notaRetornada.getNota1(), 0.01);
    assertNotEquals(10.0, notaRetornada.getNota1(), 0.01);
  }

  @Test
  public void deveRetificarNotaComSucesso() throws Exception {
    String professor = "PROF_A";
    String aluno = "20261001";
    String disciplina = "P1";
    String periodo = "2026.1";

    turmaRepository.salvar(new Turma(disciplina, professor, periodo, 30, "24M12", "Sala_101"));
    notaService.lancarNota(professor, aluno, disciplina, periodo, 1, 8.0);

    notaService.retificarNota(professor, aluno, disciplina, periodo, 1, 9.5);

    Nota nota = notaRepository.buscarPorAlunoEDisciplina(aluno, disciplina, periodo);
    assertEquals(9.5, nota.getNota1(), 0.01);
  }

  @Test(expected = ValidacaoException.class)
  public void deveLancarErroAoRetificarTurmaNaoEncontrada() throws Exception {
    notaService.retificarNota("PROF_A", "123", "D_INEXISTENTE", "P_INEXISTENTE", 1, 10.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveLancarErroAoRetificarProfessorNaoAutorizado() throws Exception {
    turmaRepository.salvar(new Turma("D2", "PROF_B", "P2", 30, "24M12", "Sala_101"));
    notaService.retificarNota("PROF_A", "123", "D2", "P2", 1, 10.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveLancarErroAoRetificarNotaInvalida() throws Exception {
    turmaRepository.salvar(new Turma("D3", "PROF_C", "P3", 30, "24M12", "Sala_101"));
    notaService.retificarNota("PROF_C", "123", "D3", "P3", 1, 15.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveLancarErroAoRetificarEtapaInvalida() throws Exception {
    turmaRepository.salvar(new Turma("D4", "PROF_D", "P4", 30, "24M12", "Sala_101"));
    notaService.retificarNota("PROF_D", "123", "D4", "P4", 3, 10.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveLancarErroAoRetificarSemNotaCadastrada() throws Exception {
    turmaRepository.salvar(new Turma("D5", "PROF_E", "P5", 30, "24M12", "Sala_101"));
    notaService.retificarNota("PROF_E", "123", "D5", "P5", 1, 10.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveLancarErroAoRetificarPeriodoEncerrado() throws Exception {
    periodoRepository.salvar(new br.edu.uepb.classroompb.model.Periodo("P6", "ENCERRADO"));
    notaService.retificarNota("PROF_F", "123", "D6", "P6", 1, 10.0);
  }
}
