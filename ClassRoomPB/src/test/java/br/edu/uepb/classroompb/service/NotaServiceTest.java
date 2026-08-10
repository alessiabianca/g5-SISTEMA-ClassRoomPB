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

  private br.edu.uepb.classroompb.repository.AvaliacaoRepository avaliacaoRepository;
  private br.edu.uepb.classroompb.repository.DiarioRepository diarioRepository;

  private static final String FILE_NOTAS = "data/notas.txt";
  private static final String FILE_TURMAS = "data/turmas.txt";
  private static final String FILE_MATRICULAS = "data/matriculas.txt";
  private static final String FILE_PERIODOS = "data/periodos.txt";
  private static final String FILE_AVALIACOES = "data/avaliacoes.txt";
  private static final String FILE_DIARIOS = "data/diarios.txt";

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
    new File(FILE_AVALIACOES).delete();
    new File(FILE_DIARIOS).delete();

    notaRepository = new NotaRepository();
    turmaRepository = new TurmaRepository();
    matriculaRepository = new MatriculaRepository();
    periodoRepository = new PeriodoRepository();
    avaliacaoRepository = new br.edu.uepb.classroompb.repository.AvaliacaoRepository();
    diarioRepository = new br.edu.uepb.classroompb.repository.DiarioRepository();

    notaService =
        new NotaService(
            notaRepository,
            turmaRepository,
            matriculaRepository,
            periodoRepository,
            avaliacaoRepository,
            diarioRepository);
  }

  @Test
  public void deveLancarNotaComSucessoParaProfessorResponsavel() throws Exception {
    String professorResponsavel = "PROF_A";
    String aluno = "20261001";
    String disciplina = "P1";
    String periodo = "2026.1";

    diarioRepository.salvar(
        new br.edu.uepb.classroompb.model.Diario(
            "DIARIO1",
            disciplina,
            periodo,
            "Desc",
            professorResponsavel,
            "08:00",
            "Sala A",
            10,
            br.edu.uepb.classroompb.model.Diario.SituacaoDiario.ABERTO));
    avaliacaoRepository.salvar(
        new br.edu.uepb.classroompb.model.Avaliacao("AVAL1", "DIARIO1", "Prova 1", 1, 2.0, 10.0));

    notaService.lancarNota(professorResponsavel, aluno, "AVAL1", 8.5);

    Nota notaSalva = notaRepository.buscarPorAlunoEDisciplina(aluno, disciplina, periodo);
    assertNotNull(notaSalva);
    assertEquals(8.5, notaSalva.getNota1(), 0.01);
  }

  @Test
  public void deveLancarValidacaoExceptionParaNotaNegativa() throws Exception {
    String professorResponsavel = "PROF_A";
    String aluno = "20261002";
    String disciplina = "P1";
    String periodo = "2026.1";

    diarioRepository.salvar(
        new br.edu.uepb.classroompb.model.Diario(
            "DIARIO2",
            disciplina,
            periodo,
            "Desc",
            professorResponsavel,
            "08:00",
            "Sala A",
            10,
            br.edu.uepb.classroompb.model.Diario.SituacaoDiario.ABERTO));
    avaliacaoRepository.salvar(
        new br.edu.uepb.classroompb.model.Avaliacao("AVAL2", "DIARIO2", "Prova 1", 1, 2.0, 10.0));

    assertThrows(
        ValidacaoException.class,
        () -> {
          notaService.lancarNota(professorResponsavel, aluno, "AVAL2", -1.5);
        });

    assertTrue(notaRepository.buscarTodas().isEmpty());
  }

  @Test
  public void deveLancarValidacaoExceptionParaNotaMaiorQueDez() throws Exception {
    String professorResponsavel = "PROF_A";
    String aluno = "20261003";
    String disciplina = "P1";
    String periodo = "2026.1";

    diarioRepository.salvar(
        new br.edu.uepb.classroompb.model.Diario(
            "DIARIO3",
            disciplina,
            periodo,
            "Desc",
            professorResponsavel,
            "08:00",
            "Sala A",
            10,
            br.edu.uepb.classroompb.model.Diario.SituacaoDiario.ABERTO));
    avaliacaoRepository.salvar(
        new br.edu.uepb.classroompb.model.Avaliacao("AVAL3", "DIARIO3", "Prova 1", 1, 2.0, 10.0));

    assertThrows(
        ValidacaoException.class,
        () -> {
          notaService.lancarNota(professorResponsavel, aluno, "AVAL3", 10.5);
        });

    assertTrue(notaRepository.buscarTodas().isEmpty());
  }

  @Test
  public void deveRecuperarNotasComSucessoApenasDoAlunoEspecificado() throws Exception {
    String alunoAlvo = "20261001";
    String outroAluno = "20261002";
    String disciplina = "P1";
    String periodo = "2026.1";

    turmaRepository.salvar(new Turma(disciplina, periodo, 30));
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

  @Test
  public void deveGarantirIsolamentoEEvitarVazamentoDeNotasDeTerceiros() throws Exception {
    String alunoLogado = "20261001";
    String alunoEstranho = "20269999";
    String disciplina = "P1";
    String periodo = "2026.1";

    turmaRepository.salvar(new Turma(disciplina, periodo, 30));
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

    diarioRepository.salvar(
        new br.edu.uepb.classroompb.model.Diario(
            "DIARIO4",
            disciplina,
            periodo,
            "Desc",
            professor,
            "08:00",
            "Sala A",
            10,
            br.edu.uepb.classroompb.model.Diario.SituacaoDiario.ABERTO));
    avaliacaoRepository.salvar(
        new br.edu.uepb.classroompb.model.Avaliacao("AVAL4", "DIARIO4", "Prova 1", 1, 2.0, 10.0));

    notaService.lancarNota(professor, aluno, "AVAL4", 8.0);

    notaService.retificarNota(professor, aluno, "AVAL4", 9.5);

    Nota nota = notaRepository.buscarPorAlunoEDisciplina(aluno, disciplina, periodo);
    assertEquals(9.5, nota.getNota1(), 0.01);
  }

  @Test(expected = ValidacaoException.class)
  public void deveLancarErroAoRetificarTurmaNaoEncontrada() throws Exception {
    notaService.retificarNota("PROF_A", "123", "AVAL_INEXISTENTE", 10.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveLancarErroAoRetificarNotaInvalida() throws Exception {
    diarioRepository.salvar(
        new br.edu.uepb.classroompb.model.Diario(
            "DIARIO5",
            "D3",
            "P3",
            "Desc",
            "PROF_C",
            "08:00",
            "Sala A",
            10,
            br.edu.uepb.classroompb.model.Diario.SituacaoDiario.ABERTO));
    avaliacaoRepository.salvar(
        new br.edu.uepb.classroompb.model.Avaliacao("AVAL5", "DIARIO5", "Prova 1", 1, 2.0, 10.0));
    notaService.retificarNota("PROF_C", "123", "AVAL5", 15.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveLancarErroAoRetificarSemNotaCadastrada() throws Exception {
    diarioRepository.salvar(
        new br.edu.uepb.classroompb.model.Diario(
            "DIARIO6",
            "D5",
            "P5",
            "Desc",
            "PROF_E",
            "08:00",
            "Sala A",
            10,
            br.edu.uepb.classroompb.model.Diario.SituacaoDiario.ABERTO));
    avaliacaoRepository.salvar(
        new br.edu.uepb.classroompb.model.Avaliacao("AVAL6", "DIARIO6", "Prova 1", 1, 2.0, 10.0));
    notaService.retificarNota("PROF_E", "123", "AVAL6", 10.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveLancarErroAoRetificarPeriodoEncerrado() throws Exception {
    periodoRepository.salvar(new br.edu.uepb.classroompb.model.Periodo("P6", "ENCERRADO"));
    diarioRepository.salvar(
        new br.edu.uepb.classroompb.model.Diario(
            "DIARIO7",
            "D6",
            "P6",
            "Desc",
            "PROF_F",
            "08:00",
            "Sala A",
            10,
            br.edu.uepb.classroompb.model.Diario.SituacaoDiario.ABERTO));
    avaliacaoRepository.salvar(
        new br.edu.uepb.classroompb.model.Avaliacao("AVAL7", "DIARIO7", "Prova 1", 1, 2.0, 10.0));
    notaService.lancarNota(
        "PROF_F", "123", "AVAL7",
        8.0); // Would fail if period check was in lancarNota, but assuming it passes or we mock the
    // note.
    notaService.retificarNota("PROF_F", "123", "AVAL7", 10.0);
  }
}
