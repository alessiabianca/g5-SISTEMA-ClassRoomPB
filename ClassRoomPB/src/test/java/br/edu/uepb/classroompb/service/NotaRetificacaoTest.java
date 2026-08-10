package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import org.junit.Before;
import org.junit.Test;

public class NotaRetificacaoTest {

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
  public void deveRetificarNotaComSucessoEmPeriodoIniciado() throws Exception {
    String professor = "PROF_A";
    String aluno = "20261001";
    String disciplina = "P1";
    String periodo = "2026.1";

    periodoRepository.salvar(new Periodo(periodo, "INICIADO"));
    diarioRepository.salvar(
        new br.edu.uepb.classroompb.model.Diario(
            "DIARIO1",
            disciplina,
            periodo,
            "Desc",
            professor,
            "08:00",
            "Sala A",
            10,
            br.edu.uepb.classroompb.model.Diario.SituacaoDiario.ABERTO));
    avaliacaoRepository.salvar(
        new br.edu.uepb.classroompb.model.Avaliacao("AVAL1", "DIARIO1", "Prova 1", 1, 2.0, 10.0));

    notaService.lancarNota(professor, aluno, "AVAL1", 7.0);

    Nota notaAntes = notaRepository.buscarPorAlunoEDisciplina(aluno, disciplina, periodo);
    assertNotNull(notaAntes);
    assertEquals(7.0, notaAntes.getNota1(), 0.01);

    notaService.retificarNota(professor, aluno, "AVAL1", 9.5);

    Nota notaDepois = notaRepository.buscarPorAlunoEDisciplina(aluno, disciplina, periodo);
    assertNotNull(notaDepois);
    assertEquals(9.5, notaDepois.getNota1(), 0.01);
  }

  @Test
  public void deveBloquearRetificacaoEmPeriodoEncerrado() throws Exception {
    String professor = "PROF_A";
    String aluno = "20261001";
    String disciplina = "P1";
    String periodo = "2026.1";

    diarioRepository.salvar(
        new br.edu.uepb.classroompb.model.Diario(
            "DIARIO2",
            disciplina,
            periodo,
            "Desc",
            professor,
            "08:00",
            "Sala A",
            10,
            br.edu.uepb.classroompb.model.Diario.SituacaoDiario.ABERTO));
    avaliacaoRepository.salvar(
        new br.edu.uepb.classroompb.model.Avaliacao("AVAL2", "DIARIO2", "Prova 1", 1, 2.0, 10.0));
    notaRepository.salvar(new Nota(aluno, disciplina, periodo, 7.0, 6.0, -1.0));
    periodoRepository.salvar(new Periodo(periodo, "ENCERRADO"));

    ValidacaoException exception =
        assertThrows(
            ValidacaoException.class,
            () -> {
              notaService.retificarNota(professor, aluno, "AVAL2", 9.5);
            });

    assertTrue(exception.getMessage().contains("ENCERRADO"));

    Nota notaIntacta = notaRepository.buscarPorAlunoEDisciplina(aluno, disciplina, periodo);
    assertNotNull(notaIntacta);
    assertEquals(7.0, notaIntacta.getNota1(), 0.01);
  }

  @Test
  public void deveBloquearRetificacaoParaNotaInexistente() throws Exception {
    String professor = "PROF_A";
    String aluno = "20261001";
    String disciplina = "P1";
    String periodo = "2026.1";

    periodoRepository.salvar(new Periodo(periodo, "INICIADO"));
    diarioRepository.salvar(
        new br.edu.uepb.classroompb.model.Diario(
            "DIARIO3",
            disciplina,
            periodo,
            "Desc",
            professor,
            "08:00",
            "Sala A",
            10,
            br.edu.uepb.classroompb.model.Diario.SituacaoDiario.ABERTO));
    avaliacaoRepository.salvar(
        new br.edu.uepb.classroompb.model.Avaliacao("AVAL3", "DIARIO3", "Prova 1", 1, 2.0, 10.0));

    ValidacaoException exception =
        assertThrows(
            ValidacaoException.class,
            () -> {
              notaService.retificarNota(professor, aluno, "AVAL3", 9.5);
            });

    assertTrue(exception.getMessage().contains("lancarNota"));
  }

  @Test
  public void deveBloquearRetificacaoComValorForaDoIntervalo() throws Exception {
    String professor = "PROF_A";
    String aluno = "20261001";
    String disciplina = "P1";
    String periodo = "2026.1";

    periodoRepository.salvar(new Periodo(periodo, "INICIADO"));
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
    notaRepository.salvar(new Nota(aluno, disciplina, periodo, 7.0, 6.0, -1.0));

    assertThrows(
        ValidacaoException.class,
        () -> {
          notaService.retificarNota(professor, aluno, "AVAL4", 11.0);
        });

    assertThrows(
        ValidacaoException.class,
        () -> {
          notaService.retificarNota(professor, aluno, "AVAL4", -2.0);
        });

    Nota notaIntacta = notaRepository.buscarPorAlunoEDisciplina(aluno, disciplina, periodo);
    assertEquals(7.0, notaIntacta.getNota1(), 0.01);
  }
}
