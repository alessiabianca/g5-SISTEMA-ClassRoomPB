package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import org.junit.Before;
import org.junit.Test;

/**
 * US35 — Testes Unitários de Restrição de Alteração de Notas Após Encerramento do Semestre. Segue o
 * mesmo padrão adotado pelo NotaServiceTest (repositórios reais, @Before com limpeza de arquivos).
 */
public class NotaRetificacaoTest {

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
   * Garante que o professor responsável consiga retificar uma nota já lançada quando o período
   * letivo ainda estiver com status INICIADO.
   */
  @Test
  public void deveRetificarNotaComSucessoEmPeriodoIniciado() throws Exception {
    String professor = "PROF_A";
    String aluno = "20261001";
    String disciplina = "P1";
    String periodo = "2026.1";

    periodoRepository.salvar(new Periodo(periodo, "INICIADO"));
    turmaRepository.salvar(new Turma(disciplina, professor, periodo, 30, "24M12", "Sala_101"));
    notaService.lancarNota(professor, aluno, disciplina, periodo, 1, 7.0);

    Nota notaAntes = notaRepository.buscarPorAlunoEDisciplina(aluno, disciplina, periodo);
    assertNotNull(notaAntes);
    assertEquals(7.0, notaAntes.getNota1(), 0.01);

    notaService.retificarNota(professor, aluno, disciplina, periodo, 1, 9.5);

    Nota notaDepois = notaRepository.buscarPorAlunoEDisciplina(aluno, disciplina, periodo);
    assertNotNull(notaDepois);
    assertEquals(9.5, notaDepois.getNota1(), 0.01);
  }

  /**
   * Garante o bloqueio absoluto de retificação de notas quando o período letivo estiver com status
   * ENCERRADO, estourando ValidacaoException.
   */
  @Test
  public void deveBloquearRetificacaoEmPeriodoEncerrado() throws Exception {
    String professor = "PROF_A";
    String aluno = "20261001";
    String disciplina = "P1";
    String periodo = "2026.1";

    turmaRepository.salvar(new Turma(disciplina, professor, periodo, 30, "24M12", "Sala_101"));
    notaRepository.salvar(new Nota(aluno, disciplina, periodo, 7.0, 6.0, -1.0));
    periodoRepository.salvar(new Periodo(periodo, "ENCERRADO"));

    ValidacaoException exception =
        assertThrows(
            ValidacaoException.class,
            () -> {
              notaService.retificarNota(professor, aluno, disciplina, periodo, 1, 9.5);
            });

    assertTrue(exception.getMessage().contains("ENCERRADO"));

    Nota notaIntacta = notaRepository.buscarPorAlunoEDisciplina(aluno, disciplina, periodo);
    assertNotNull(notaIntacta);
    assertEquals(7.0, notaIntacta.getNota1(), 0.01);
  }

  /**
   * Garante que um professor NÃO associado à turma seja bloqueado de retificar notas dos alunos
   * vinculados a outro docente.
   */
  @Test
  public void deveBloquearRetificacaoPorProfessorNaoResponsavel() throws Exception {
    String professorResponsavel = "PROF_A";
    String professorInvasor = "PROF_B";
    String aluno = "20261001";
    String disciplina = "P1";
    String periodo = "2026.1";

    periodoRepository.salvar(new Periodo(periodo, "INICIADO"));
    turmaRepository.salvar(
        new Turma(disciplina, professorResponsavel, periodo, 30, "24M12", "Sala_101"));
    notaRepository.salvar(new Nota(aluno, disciplina, periodo, 7.0, 6.0, -1.0));

    assertThrows(
        ValidacaoException.class,
        () -> {
          notaService.retificarNota(professorInvasor, aluno, disciplina, periodo, 1, 9.5);
        });

    Nota notaIntacta = notaRepository.buscarPorAlunoEDisciplina(aluno, disciplina, periodo);
    assertEquals(7.0, notaIntacta.getNota1(), 0.01);
  }

  /**
   * Garante que a retificação falhe com ValidacaoException quando não houver nota previamente
   * lançada para o aluno na disciplina/período informados.
   */
  @Test
  public void deveBloquearRetificacaoParaNotaInexistente() throws Exception {
    String professor = "PROF_A";
    String aluno = "20261001";
    String disciplina = "P1";
    String periodo = "2026.1";

    periodoRepository.salvar(new Periodo(periodo, "INICIADO"));
    turmaRepository.salvar(new Turma(disciplina, professor, periodo, 30, "24M12", "Sala_101"));

    ValidacaoException exception =
        assertThrows(
            ValidacaoException.class,
            () -> {
              notaService.retificarNota(professor, aluno, disciplina, periodo, 1, 9.5);
            });

    assertTrue(exception.getMessage().contains("lancarNota"));
  }

  /**
   * Garante que notas com valores fora do intervalo estrito [0.0, 10.0] sejam rejeitadas mesmo
   * durante uma retificação legítima.
   */
  @Test
  public void deveBloquearRetificacaoComValorForaDoIntervalo() throws Exception {
    String professor = "PROF_A";
    String aluno = "20261001";
    String disciplina = "P1";
    String periodo = "2026.1";

    periodoRepository.salvar(new Periodo(periodo, "INICIADO"));
    turmaRepository.salvar(new Turma(disciplina, professor, periodo, 30, "24M12", "Sala_101"));
    notaRepository.salvar(new Nota(aluno, disciplina, periodo, 7.0, 6.0, -1.0));

    assertThrows(
        ValidacaoException.class,
        () -> {
          notaService.retificarNota(professor, aluno, disciplina, periodo, 1, 11.0);
        });

    assertThrows(
        ValidacaoException.class,
        () -> {
          notaService.retificarNota(professor, aluno, disciplina, periodo, 1, -2.0);
        });

    Nota notaIntacta = notaRepository.buscarPorAlunoEDisciplina(aluno, disciplina, periodo);
    assertEquals(7.0, notaIntacta.getNota1(), 0.01);
  }
}
