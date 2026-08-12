package br.edu.uepb.classroompb.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import br.edu.uepb.classroompb.model.Aula;
import br.edu.uepb.classroompb.model.Avaliacao;
import br.edu.uepb.classroompb.model.Diario;
import br.edu.uepb.classroompb.model.Historico;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.StatusAcademico;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.AulaRepository;
import br.edu.uepb.classroompb.repository.AvaliacaoRepository;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.repository.HistoricoRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FechamentoDiarioIntegrationTest {
  private static final String[] ARQUIVOS = {
    "data/aulas.txt",
    "data/avaliacoes.txt",
    "data/diarios.txt",
    "data/disciplinas.txt",
    "data/frequencias.txt",
    "data/historico.txt",
    "data/matriculas.txt",
    "data/notas.txt",
    "data/periodos.txt",
    "data/turmas.txt",
    "data/usuarios.json"
  };

  private AulaRepository aulaRepository;
  private AvaliacaoRepository avaliacaoRepository;
  private DiarioRepository diarioRepository;
  private FrequenciaRepository frequenciaRepository;
  private HistoricoRepository historicoRepository;
  private MatriculaRepository matriculaRepository;
  private NotaRepository notaRepository;
  private PeriodoRepository periodoRepository;
  private TurmaRepository turmaRepository;
  private UsuarioRepository usuarioRepository;
  private DiarioService diarioService;
  private FrequenciaService frequenciaService;
  private NotaService notaService;
  private AvaliacaoService avaliacaoService;
  private final Map<String, byte[]> estadoAnterior = new HashMap<>();

  @Before
  public void setUp() throws IOException {
    salvarEstadoAtual();
    limparArquivos();
    aulaRepository = new AulaRepository();
    avaliacaoRepository = new AvaliacaoRepository();
    diarioRepository = new DiarioRepository();
    frequenciaRepository = new FrequenciaRepository();
    historicoRepository = new HistoricoRepository();
    matriculaRepository = new MatriculaRepository();
    notaRepository = new NotaRepository();
    periodoRepository = new PeriodoRepository();
    turmaRepository = new TurmaRepository();
    usuarioRepository = new UsuarioRepository();

    diarioService =
        new DiarioService(
            diarioRepository,
            turmaRepository,
            usuarioRepository,
            aulaRepository,
            avaliacaoRepository,
            matriculaRepository,
            frequenciaRepository,
            notaRepository);
    frequenciaService =
        new FrequenciaService(
            turmaRepository,
            matriculaRepository,
            frequenciaRepository,
            notaRepository,
            diarioRepository,
            aulaRepository);
    notaService =
        new NotaService(
            notaRepository,
            turmaRepository,
            matriculaRepository,
            periodoRepository,
            avaliacaoRepository,
            diarioRepository);
    avaliacaoService = new AvaliacaoService(avaliacaoRepository, diarioRepository);
  }

  @After
  public void tearDown() throws IOException {
    limparArquivos();
    restaurarEstadoAnterior();
  }

  @Test
  public void deveValidarPendenciasEImpedirAlteracoesDepoisDoFechamento() throws Exception {
    prepararTurma("D1", "P1", "AL1");
    diarioRepository.salvar(novoDiario("DIARIO1", "D1", "P1", "PROF1"));
    aulaRepository.salvar(new Aula("AULA1", "DIARIO1", "12/08/2026", "US48", 2));
    avaliacaoRepository.salvar(new Avaliacao("AV1", "DIARIO1", "Prova", 1, 1.0, 10.0));

    ValidacaoException pendencias =
        assertThrows(
            ValidacaoException.class, () -> diarioService.fecharDiario("PROF1", "DIARIO1"));
    assertTrue(pendencias.getMessage().contains("frequencia"));
    assertTrue(pendencias.getMessage().contains("nota"));
    assertEquals(
        Diario.SituacaoDiario.ABERTO, diarioRepository.buscarPorCodigo("DIARIO1").getSituacao());

    frequenciaService.registrarChamadaLote(
        "PROF1",
        "DIARIO1",
        "AULA1",
        List.of(new Matricula("AL1", "D1", "P1", Matricula.StatusMatricula.CONFIRMADA)));
    notaService.lancarNota("PROF1", "AL1", "AV1", 8.0);
    diarioService.fecharDiario("PROF1", "DIARIO1");

    Diario fechado = diarioRepository.buscarPorCodigo("DIARIO1");
    assertTrue(fechado.isFechado());
    assertThrows(IllegalStateException.class, () -> fechado.setDescricao("alterada"));
    assertThrows(
        ValidacaoException.class, () -> notaService.retificarNota("PROF1", "AL1", "AV1", 9.0));
    assertThrows(
        ValidacaoException.class,
        () ->
            frequenciaService.registrarChamadaLote(
                "PROF1",
                "DIARIO1",
                "AULA1",
                List.of(new Matricula("AL1", "D1", "P1", Matricula.StatusMatricula.CONFIRMADA))));
    assertThrows(
        ValidacaoException.class,
        () -> avaliacaoService.cadastrarAvaliacao("PROF1", "DIARIO1", "Outra", 2, 1.0, 10.0));
    assertThrows(
        ValidacaoException.class,
        () ->
            new AulaService(aulaRepository, diarioRepository)
                .cadastrarAula(new Aula("AULA2", "DIARIO1", "13/08/2026", "Bloqueada", 2)));

    MatriculaService matriculaService =
        new MatriculaService(
            turmaRepository,
            matriculaRepository,
            periodoRepository,
            new DisciplinaRepository(),
            historicoRepository,
            diarioRepository);
    assertThrows(
        ValidacaoException.class, () -> matriculaService.cancelarMatricula("AL1", "D1", "P1"));
  }

  @Test
  public void deveConsolidarDoisDiariosEmUmUnicoResultadoNoHistorico() throws Exception {
    prepararTurma("D1", "P1", "AL1");
    periodoRepository.salvar(new Periodo("P1", "INICIADO"));
    diarioRepository.salvar(novoDiario("DIARIO1", "D1", "P1", "PROF1"));
    diarioRepository.salvar(novoDiario("DIARIO2", "D1", "P1", "PROF2"));
    aulaRepository.salvar(new Aula("AULA1", "DIARIO1", "10/08/2026", "Parte 1", 2));
    aulaRepository.salvar(new Aula("AULA2", "DIARIO2", "11/08/2026", "Parte 2", 2));
    avaliacaoRepository.salvar(new Avaliacao("AV1", "DIARIO1", "Prova 1", 1, 1.0, 10.0));
    avaliacaoRepository.salvar(new Avaliacao("AV2", "DIARIO2", "Prova 2", 1, 3.0, 10.0));

    registrarPresenca("PROF1", "DIARIO1", "AULA1");
    registrarPresenca("PROF2", "DIARIO2", "AULA2");
    notaService.lancarNota("PROF1", "AL1", "AV1", 10.0);
    notaService.lancarNota("PROF2", "AL1", "AV2", 6.0);
    diarioService.fecharDiario("PROF1", "DIARIO1");
    diarioService.fecharDiario("PROF2", "DIARIO2");

    SituacaoAcademicaService situacaoService =
        new SituacaoAcademicaService(
            notaRepository, frequenciaService, avaliacaoRepository, diarioRepository);
    HistoricoService historicoService =
        new HistoricoService(
            historicoRepository,
            matriculaRepository,
            turmaRepository,
            situacaoService,
            frequenciaService,
            diarioRepository,
            avaliacaoRepository,
            notaRepository,
            frequenciaRepository);
    PeriodoService periodoService =
        new PeriodoService(periodoRepository, historicoService, diarioRepository);

    periodoService.encerrarPeriodo("P1");
    historicoService.gerarHistoricoDoPeriodo("P1");

    List<Historico> historico = historicoRepository.buscarPorAluno("AL1");
    assertEquals(1, historico.size());
    // Media global ponderada: ((10 * 1) + (6 * 3)) / (1 + 3) = 7.0.
    // Uma media simples entre os diarios resultaria em 8.0 e faria este teste falhar.
    assertEquals(7.0, historico.get(0).getMediaFinal(), 0.01);
    assertEquals(100.0, historico.get(0).getPercentualFrequencia(), 0.01);
    assertEquals(StatusAcademico.APROVADO, historico.get(0).getStatus());
    assertEquals("PROF1,PROF2", historico.get(0).getMatriculaProfessor());
  }

  private void prepararTurma(String disciplina, String periodo, String aluno) {
    turmaRepository.salvar(new Turma(disciplina, periodo, 30, 1));
    matriculaRepository.salvar(
        new Matricula(aluno, disciplina, periodo, Matricula.StatusMatricula.CONFIRMADA));
  }

  private Diario novoDiario(String codigo, String disciplina, String periodo, String professor) {
    return new Diario(codigo, disciplina, periodo, "Diario", professor, "08:00", "S1", 60);
  }

  private void registrarPresenca(String professor, String diario, String aula)
      throws ValidacaoException {
    frequenciaService.registrarChamadaLote(
        professor,
        diario,
        aula,
        List.of(new Matricula("AL1", "D1", "P1", Matricula.StatusMatricula.CONFIRMADA)));
  }

  private void limparArquivos() {
    for (String arquivo : ARQUIVOS) {
      new File(arquivo).delete();
    }
  }

  private void salvarEstadoAtual() throws IOException {
    estadoAnterior.clear();
    for (String arquivo : ARQUIVOS) {
      Path caminho = Path.of(arquivo);
      estadoAnterior.put(arquivo, Files.exists(caminho) ? Files.readAllBytes(caminho) : null);
    }
  }

  private void restaurarEstadoAnterior() throws IOException {
    for (Map.Entry<String, byte[]> registro : estadoAnterior.entrySet()) {
      if (registro.getValue() != null) {
        Path caminho = Path.of(registro.getKey());
        Files.createDirectories(caminho.getParent());
        Files.write(caminho, registro.getValue());
      }
    }
  }
}
