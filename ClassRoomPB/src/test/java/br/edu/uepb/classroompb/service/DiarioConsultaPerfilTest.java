package br.edu.uepb.classroompb.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import br.edu.uepb.classroompb.model.Aluno;
import br.edu.uepb.classroompb.model.Aula;
import br.edu.uepb.classroompb.model.Avaliacao;
import br.edu.uepb.classroompb.model.Coordenador;
import br.edu.uepb.classroompb.model.Diario;
import br.edu.uepb.classroompb.model.ExtratoDiarioAluno;
import br.edu.uepb.classroompb.model.Frequencia;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.Professor;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.AulaRepository;
import br.edu.uepb.classroompb.repository.AvaliacaoRepository;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
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

public class DiarioConsultaPerfilTest {
  private static final String[] ARQUIVOS = {
    "data/aulas.txt",
    "data/avaliacoes.txt",
    "data/diarios.txt",
    "data/frequencias.txt",
    "data/matriculas.txt",
    "data/notas.txt",
    "data/turmas.txt",
    "data/usuarios.json"
  };

  private final Map<String, byte[]> estadoAnterior = new HashMap<>();
  private DiarioRepository diarioRepository;
  private MatriculaRepository matriculaRepository;
  private FrequenciaRepository frequenciaRepository;
  private NotaRepository notaRepository;
  private DiarioService diarioService;
  private Coordenador coordenador;
  private Professor professor1;
  private Professor professor2;
  private Aluno aluno1;

  @Before
  public void setUp() throws IOException {
    salvarEstadoAtual();
    limparArquivos();

    AulaRepository aulaRepository = new AulaRepository();
    AvaliacaoRepository avaliacaoRepository = new AvaliacaoRepository();
    diarioRepository = new DiarioRepository();
    frequenciaRepository = new FrequenciaRepository();
    matriculaRepository = new MatriculaRepository();
    notaRepository = new NotaRepository();
    TurmaRepository turmaRepository = new TurmaRepository();
    UsuarioRepository usuarioRepository = new UsuarioRepository();

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

    coordenador = new Coordenador("COORD1", "Coordenador", "coord@teste", "senha", "CC");
    professor1 = new Professor("PROF1", "Professor 1", "p1@teste", "senha");
    professor2 = new Professor("PROF2", "Professor 2", "p2@teste", "senha");
    aluno1 = new Aluno("AL1", "Aluno 1", "a1@teste", "senha", "CC");

    turmaRepository.salvar(new Turma("D1", "P1", 30));
    turmaRepository.salvar(new Turma("D2", "P1", 30));
    diarioRepository.salvar(novoDiario("DIA1", "D1", professor1.getMatricula()));
    diarioRepository.salvar(novoDiario("DIA2", "D1", professor2.getMatricula()));
    diarioRepository.salvar(novoDiario("DIA3", "D2", professor2.getMatricula()));

    matriculaRepository.salvar(
        new Matricula("AL1", "D1", "P1", Matricula.StatusMatricula.CONFIRMADA));
    matriculaRepository.salvar(
        new Matricula("AL2", "D2", "P1", Matricula.StatusMatricula.CONFIRMADA));

    aulaRepository.salvar(new Aula("A1", "DIA1", "10/08/2026", "Introducao", 2));
    aulaRepository.salvar(new Aula("A2", "DIA1", "12/08/2026", "Pratica", 2));
    avaliacaoRepository.salvar(new Avaliacao("AV1", "DIA1", "Prova", 1, 1.0, 10.0));
    avaliacaoRepository.salvar(new Avaliacao("AV2", "DIA1", "Projeto", 2, 3.0, 10.0));

    frequenciaRepository.salvarLote(
        List.of(
            frequencia("A1", "AL1", Frequencia.TipoFrequencia.PRESENCA),
            frequencia("A2", "AL1", Frequencia.TipoFrequencia.FALTA),
            frequencia("A1", "OUTRO", Frequencia.TipoFrequencia.PRESENCA)));
    notaRepository.salvar(new Nota("AL1", "D1", "P1", "DIA1", 8.0, 6.0, -1.0));
    notaRepository.salvar(new Nota("OUTRO", "D1", "P1", "DIA1", 10.0, 10.0, -1.0));
  }

  @After
  public void tearDown() throws IOException {
    limparArquivos();
    restaurarEstadoAnterior();
  }

  @Test
  public void coordenadorDeveVisualizarTodosOsDiariosDaTurmaSelecionada() throws Exception {
    List<Diario> diarios = diarioService.consultarDiariosDaTurma(coordenador, "D1", "P1");

    assertEquals(2, diarios.size());
    assertTrue(diarios.stream().anyMatch(d -> d.getCodigo().equals("DIA1")));
    assertTrue(diarios.stream().anyMatch(d -> d.getCodigo().equals("DIA2")));
    assertFalse(diarios.stream().anyMatch(d -> d.getCodigo().equals("DIA3")));
  }

  @Test
  public void professorDeveVisualizarExclusivamenteOsPropriosDiarios() throws Exception {
    List<Diario> diarios = diarioService.consultarMeusDiarios(professor1);

    assertEquals(1, diarios.size());
    assertEquals("DIA1", diarios.get(0).getCodigo());
  }

  @Test
  public void professorNaoPodeConsultarDiarioDeTerceiro() {
    ValidacaoException erro =
        assertThrows(
            ValidacaoException.class,
            () -> diarioService.consultarDiarioDoProfessor(professor1, "DIA2"));

    assertTrue(erro.getMessage().contains("Acesso negado"));
  }

  @Test
  public void alunoNaoPodeConsultarDiarioSemMatricula() {
    ValidacaoException erro =
        assertThrows(
            ValidacaoException.class, () -> diarioService.consultarExtratoDoAluno(aluno1, "DIA3"));

    assertTrue(erro.getMessage().contains("Acesso negado"));
  }

  @Test
  public void alunoDeveReceberSomenteSeusLancamentosEMediaParcial() throws Exception {
    List<Diario> diarios = diarioService.consultarDiariosDoAluno(aluno1);
    ExtratoDiarioAluno extrato = diarioService.consultarExtratoDoAluno(aluno1, "DIA1");

    assertEquals(2, diarios.size());
    assertTrue(diarios.stream().noneMatch(d -> d.getCodigo().equals("DIA3")));
    assertEquals("AL1", extrato.getMatriculaAluno());
    assertEquals(2, extrato.getFrequencias().size());
    assertTrue(
        extrato.getFrequencias().stream()
            .allMatch(f -> f.getMatriculaAluno().equalsIgnoreCase("AL1")));
    assertEquals(2, extrato.getNotasAvaliacoes().size());
    assertEquals(8.0, extrato.getNotasAvaliacoes().get(0).getValor(), 0.01);
    assertEquals(6.0, extrato.getNotasAvaliacoes().get(1).getValor(), 0.01);
    assertEquals(6.5, extrato.getMediaParcial(), 0.01);
  }

  @Test
  public void perfisNaoPodemUsarConsultaGlobalDoCoordenador() {
    assertThrows(
        ValidacaoException.class,
        () -> diarioService.consultarDiariosDaTurma(professor1, "D1", "P1"));
    assertThrows(
        ValidacaoException.class, () -> diarioService.consultarDiariosDaTurma(aluno1, "D1", "P1"));
  }

  private Diario novoDiario(String codigo, String disciplina, String professor) {
    return new Diario(codigo, disciplina, "P1", "Diario", professor, "08:00", "S1", 60);
  }

  private Frequencia frequencia(String aula, String aluno, Frequencia.TipoFrequencia situacao) {
    return new Frequencia(aula, "DIA1", "10/08/2026", aluno, "D1", "P1", situacao);
  }

  private void salvarEstadoAtual() throws IOException {
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

  private void limparArquivos() {
    for (String arquivo : ARQUIVOS) {
      new File(arquivo).delete();
    }
  }
}
