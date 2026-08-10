package br.edu.uepb.classroompb.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import br.edu.uepb.classroompb.model.Frequencia;
import br.edu.uepb.classroompb.model.Historico;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.StatusAcademico;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.repository.HistoricoRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HistoricoAcademicoTest {
  private static final Path ARQUIVO_HISTORICO = Path.of("data", "historico.txt");
  private static final String ALUNO = "ALUNO_RF50";
  private static final String DISCIPLINA = "D_RF50";
  private static final String PERIODO = "2026.RF50";

  private TurmaRepository turmaRepository;
  private MatriculaRepository matriculaRepository;
  private NotaRepository notaRepository;
  private FrequenciaRepository frequenciaRepository;
  private HistoricoRepository historicoRepository;
  private HistoricoService historicoService;

  @Before
  public void setUp() {
    limparArquivos();

    turmaRepository = new TurmaRepository();
    matriculaRepository = new MatriculaRepository();
    notaRepository = new NotaRepository();
    frequenciaRepository = new FrequenciaRepository();
    historicoRepository = new HistoricoRepository();

    FrequenciaService frequenciaService =
        new FrequenciaService(
            turmaRepository,
            matriculaRepository,
            frequenciaRepository,
            notaRepository,
            new br.edu.uepb.classroompb.repository.DiarioRepository(),
            new br.edu.uepb.classroompb.repository.AulaRepository());
    SituacaoAcademicaService situacaoService =
        new SituacaoAcademicaService(
            notaRepository,
            frequenciaService,
            new br.edu.uepb.classroompb.repository.AvaliacaoRepository(),
            new br.edu.uepb.classroompb.repository.DiarioRepository());
    historicoService =
        new HistoricoService(
            historicoRepository,
            matriculaRepository,
            turmaRepository,
            situacaoService,
            frequenciaService);
  }

  @After
  public void tearDown() {
    limparArquivos();
  }

  @Test
  public void devePersistirApenasResultadoFinalDaTurmaSemDetalhesDoDiario() throws Exception {
    turmaRepository.salvar(new Turma(DISCIPLINA, PERIODO, 30));
    matriculaRepository.salvar(
        new Matricula(ALUNO, DISCIPLINA, PERIODO, Matricula.StatusMatricula.CONFIRMADA));
    notaRepository.salvar(new Nota(ALUNO, DISCIPLINA, PERIODO, 8.0, 9.0, 10.0));
    frequenciaRepository.salvarLote(
        List.of(
            new Frequencia(
                "AULA_01",
                "DIARIO_01",
                "01/07/2026",
                ALUNO,
                DISCIPLINA,
                PERIODO,
                Frequencia.TipoFrequencia.PRESENCA),
            new Frequencia(
                "AULA_01",
                "DIARIO_01",
                "02/07/2026",
                ALUNO,
                DISCIPLINA,
                PERIODO,
                Frequencia.TipoFrequencia.PRESENCA),
            new Frequencia(
                "AULA_01",
                "DIARIO_01",
                "03/07/2026",
                ALUNO,
                DISCIPLINA,
                PERIODO,
                Frequencia.TipoFrequencia.FALTA),
            new Frequencia(
                "AULA_01",
                "DIARIO_01",
                "04/07/2026",
                ALUNO,
                DISCIPLINA,
                PERIODO,
                Frequencia.TipoFrequencia.PRESENCA)));

    historicoService.gerarHistoricoDoPeriodo(PERIODO);

    List<Historico> historico = historicoService.consultarHistorico(ALUNO);
    assertEquals(1, historico.size());
    assertEquals(DISCIPLINA, historico.get(0).getCodigoDisciplina());
    assertEquals(PERIODO, historico.get(0).getPeriodo());
    assertEquals(9.0, historico.get(0).getMediaFinal(), 0.01);
    assertEquals(75.0, historico.get(0).getPercentualFrequencia(), 0.01);
    assertEquals(StatusAcademico.APROVADO, historico.get(0).getStatus());

    List<String> linhas = Files.readAllLines(ARQUIVO_HISTORICO);
    assertEquals(1, linhas.size());
    assertEquals(7, linhas.get(0).split(";").length);
    assertFalse(linhas.get(0).contains("01/07/2026"));
    assertFalse(linhas.get(0).contains("02/07/2026"));
    assertFalse(linhas.get(0).contains("LAB_01"));
    assertFalse(linhas.get(0).toUpperCase().contains("AVALIACAO"));
    assertFalse(linhas.get(0).toUpperCase().contains("DIARIO"));
  }

  @Test
  public void deveGerarSomenteUmHistoricoPorAlunoDisciplinaEPeriodo() {
    turmaRepository.salvar(new Turma(DISCIPLINA, PERIODO, 30));
    matriculaRepository.salvar(
        new Matricula(ALUNO, DISCIPLINA, PERIODO, Matricula.StatusMatricula.CONFIRMADA));
    matriculaRepository.salvar(
        new Matricula(ALUNO, DISCIPLINA, PERIODO, Matricula.StatusMatricula.CONFIRMADA));
    notaRepository.salvar(new Nota(ALUNO, DISCIPLINA, PERIODO, 7.0, 7.0, -1.0));

    historicoService.gerarHistoricoDoPeriodo(PERIODO);

    List<Historico> historicos = historicoRepository.buscarTodos();
    assertEquals(1, historicos.size());
    assertEquals(ALUNO, historicos.get(0).getMatriculaAluno());
    assertEquals(DISCIPLINA, historicos.get(0).getCodigoDisciplina());
    assertEquals(PERIODO, historicos.get(0).getPeriodo());
  }

  @Test
  public void deveConsolidarHistoricoQuandoProfessorDaTurmaNaoEstiverMaisNaOferta() {
    turmaRepository.salvar(new Turma(DISCIPLINA, PERIODO, 30));
    matriculaRepository.salvar(
        new Matricula(ALUNO, DISCIPLINA, PERIODO, Matricula.StatusMatricula.CONFIRMADA));
    notaRepository.salvar(new Nota(ALUNO, DISCIPLINA, PERIODO, 8.0, 8.0, -1.0));

    historicoService.gerarHistoricoDoPeriodo(PERIODO);

    List<Historico> historico = historicoService.consultarHistorico(ALUNO);
    assertEquals(1, historico.size());
    assertEquals("N/A", historico.get(0).getMatriculaProfessor());
    assertEquals(StatusAcademico.APROVADO, historico.get(0).getStatus());
  }

  private void limparArquivos() {
    new File("data/turmas.txt").delete();
    new File("data/matriculas.txt").delete();
    new File("data/notas.txt").delete();
    new File("data/frequencias.txt").delete();
    new File("data/historico.txt").delete();
  }
}
