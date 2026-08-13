package br.edu.uepb.classroompb.view;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import br.edu.uepb.classroompb.model.Aluno;
import br.edu.uepb.classroompb.model.Aula;
import br.edu.uepb.classroompb.model.Avaliacao;
import br.edu.uepb.classroompb.model.Coordenador;
import br.edu.uepb.classroompb.model.Diario;
import br.edu.uepb.classroompb.model.Frequencia;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.Professor;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.repository.AulaRepository;
import br.edu.uepb.classroompb.repository.AvaliacaoRepository;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.service.DiarioService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DiarioConsultaCLITest {
  private static final String[] ARQUIVOS = {
    "data/aulas.txt",
    "data/avaliacoes.txt",
    "data/cursos.txt",
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

  private final Map<String, byte[]> estadoAnterior = new HashMap<>();
  private final ByteArrayOutputStream output = new ByteArrayOutputStream();
  private final ByteArrayOutputStream outputError = new ByteArrayOutputStream();
  private PrintStream originalOut;
  private PrintStream originalErr;
  private Locale localeOriginal;
  private CoordenadorCLI coordenadorCLI;
  private ProfessorCLI professorCLI;
  private AlunoCLI alunoCLI;
  private Coordenador coordenador;
  private Professor professor1;
  private Aluno aluno1;

  @Before
  public void setUp() throws Exception {
    salvarEstadoAtual();
    limparArquivos();

    originalOut = System.out;
    originalErr = System.err;
    localeOriginal = Locale.getDefault();
    Locale.setDefault(Locale.US);
    System.setOut(new PrintStream(output));
    System.setErr(new PrintStream(outputError));

    TurmaRepository turmaRepository = new TurmaRepository();
    DiarioRepository diarioRepository = new DiarioRepository();
    AulaRepository aulaRepository = new AulaRepository();
    AvaliacaoRepository avaliacaoRepository = new AvaliacaoRepository();
    MatriculaRepository matriculaRepository = new MatriculaRepository();
    FrequenciaRepository frequenciaRepository = new FrequenciaRepository();
    NotaRepository notaRepository = new NotaRepository();
    UsuarioRepository usuarioRepository = new UsuarioRepository();

    DiarioService diarioService =
        new DiarioService(
            diarioRepository,
            turmaRepository,
            usuarioRepository,
            aulaRepository,
            avaliacaoRepository,
            matriculaRepository,
            frequenciaRepository,
            notaRepository);

    coordenadorCLI = new CoordenadorCLI(null, null, diarioService, usuarioRepository);
    professorCLI = new ProfessorCLI(null, null, null, null, diarioService);
    alunoCLI = new AlunoCLI(null, null, null, diarioService);

    coordenador = new Coordenador("COORD1", "Coordenador", "coord@teste", "senha", "CC");
    professor1 = new Professor("PROF1", "Professor 1", "p1@teste", "senha");
    Professor professor2 = new Professor("PROF2", "Professor 2", "p2@teste", "senha");
    aluno1 = new Aluno("AL1", "Aluno 1", "a1@teste", "senha", "CC");

    turmaRepository.salvar(new Turma("D1", "P1", 30));
    turmaRepository.salvar(new Turma("D2", "P1", 30));
    turmaRepository.salvar(new Turma("D3", "P1", 30));
    diarioRepository.salvar(new Diario("DIA1", "D1", "P1", "Teorico", "PROF1", "08:00", "S1", 60));
    diarioRepository.salvar(
        new Diario("DIA2", "D1", "P1", "Laboratorio", "PROF2", "10:00", "S2", 30));
    diarioRepository.salvar(
        new Diario("DIA3", "D2", "P1", "Outra turma", "PROF2", "12:00", "S3", 60));

    matriculaRepository.salvar(
        new Matricula("AL1", "D1", "P1", Matricula.StatusMatricula.CONFIRMADA));
    matriculaRepository.salvar(
        new Matricula("AL2", "D2", "P1", Matricula.StatusMatricula.CONFIRMADA));

    aulaRepository.salvar(new Aula("A1", "DIA1", "10/08/2026", "Introducao", 2));
    aulaRepository.salvar(new Aula("A2", "DIA1", "12/08/2026", "Pratica", 2));
    aulaRepository.salvar(new Aula("B1", "DIA2", "14/08/2026", "Laboratorio", 2));

    avaliacaoRepository.salvar(new Avaliacao("AV1", "DIA1", "Prova", 1, 1.0, 10.0));
    avaliacaoRepository.salvar(new Avaliacao("AV2", "DIA1", "Projeto", 2, 3.0, 10.0));
    avaliacaoRepository.salvar(new Avaliacao("AV3", "DIA2", "Pratica", 1, 2.0, 20.0));

    frequenciaRepository.salvarLote(
        List.of(
            frequencia("DIA1", "A1", "10/08/2026", "AL1", Frequencia.TipoFrequencia.PRESENCA),
            frequencia("DIA1", "A2", "12/08/2026", "AL1", Frequencia.TipoFrequencia.FALTA),
            frequencia("DIA1", "A1", "10/08/2026", "OUTRO", Frequencia.TipoFrequencia.PRESENCA),
            frequencia("DIA2", "B1", "14/08/2026", "AL1", Frequencia.TipoFrequencia.PRESENCA)));

    notaRepository.salvar(new Nota("AL1", "D1", "P1", "DIA1", 8.0, 6.0, -1.0));
    notaRepository.salvar(new Nota("AL1", "D1", "P1", "DIA2", 16.0, -1.0, -1.0));
    notaRepository.salvar(new Nota("OUTRO", "D1", "P1", "DIA1", 10.0, 10.0, -1.0));
  }

  @After
  public void tearDown() throws Exception {
    System.setOut(originalOut);
    System.setErr(originalErr);
    Locale.setDefault(localeOriginal);
    AutenticacaoService.getInstancia().realizarLogout();
    limparArquivos();
    restaurarEstadoAnterior();
  }

  @Test
  public void coordenadorDeveConsultarDiariosDaTurmaPeloComandoEAlias() throws Exception {
    definirUsuarioLogado(coordenador);

    coordenadorCLI.processar("consultarDiariosTurma D1 P1");
    String painel = output.toString();

    assertTrue(painel.contains("DIARIOS VINCULADOS A TURMA"));
    assertTrue(painel.contains("DIA1"));
    assertTrue(painel.contains("DIA2"));
    assertFalse(painel.contains("DIA3"));

    resetarSaida();
    coordenadorCLI.processar("diariosTurma D1 P1");

    assertTrue(output.toString().contains("DIA1"));
    assertTrue(output.toString().contains("DIA2"));
  }

  @Test
  public void professorDeveConsultarSomenteSeusDiariosPelosComandosDaRf51() throws Exception {
    definirUsuarioLogado(professor1);

    professorCLI.processar("meusDiarios");
    String painelAliasOriginal = output.toString();

    assertTrue(painelAliasOriginal.contains("MEUS DIARIOS"));
    assertTrue(painelAliasOriginal.contains("DIA1"));
    assertFalse(painelAliasOriginal.contains("DIA2"));

    resetarSaida();
    professorCLI.processar("consultarDiarios");
    String painel = output.toString();

    assertTrue(painel.contains("MEUS DIARIOS"));
    assertTrue(painel.contains("DIA1"));
    assertFalse(painel.contains("DIA2"));

    resetarSaida();
    professorCLI.processar("consultarMeuDiario DIA1");
    assertTrue(output.toString().contains("DIA1"));

    resetarSaida();
    professorCLI.processar("consultarDiario DIA1");
    assertTrue(output.toString().contains("DIA1"));

    resetarSaida();
    professorCLI.processar("consultarDiario DIA2");
    assertTrue(output.toString().contains("Acesso negado"));
  }

  @Test
  public void cliDeveExibirMensagensQuandoConsultaRf51NaoTemResultado() throws Exception {
    definirUsuarioLogado(coordenador);
    coordenadorCLI.processar("consultarDiariosTurma D3 P1");
    assertTrue(output.toString().contains("Nenhum diario vinculado a esta turma."));

    resetarSaida();
    definirUsuarioLogado(new Professor("PROF0", "Professor 0", "p0@teste", "senha"));
    professorCLI.processar("meusDiarios");
    assertTrue(output.toString().contains("Nenhum diario sob sua responsabilidade."));

    resetarSaida();
    definirUsuarioLogado(new Aluno("AL0", "Aluno 0", "a0@teste", "senha", "CC"));
    alunoCLI.processar("consultarExtratosDiarios");
    assertTrue(output.toString().contains("Nenhum diario encontrado"));
  }

  @Test
  public void cliDeveOrientarUsoQuandoConsultaRf51RecebeParametrosInsuficientes() throws Exception {
    definirUsuarioLogado(coordenador);
    coordenadorCLI.processar("consultarDiariosTurma D1");
    assertTrue(outputError.toString().contains("Uso: consultarDiariosTurma"));

    resetarSaida();
    definirUsuarioLogado(professor1);
    professorCLI.processar("consultarDiario");
    assertTrue(outputError.toString().contains("Uso: consultarMeuDiario"));

    resetarSaida();
    definirUsuarioLogado(aluno1);
    alunoCLI.processar("consultarDiario");
    assertTrue(outputError.toString().contains("Uso: consultarDiario"));
  }

  @Test
  public void alunoDeveConsultarDiariosExtratoIndividualETodosOsExtratos() throws Exception {
    definirUsuarioLogado(aluno1);

    alunoCLI.processar("consultarDiarios");
    String listaDiarios = output.toString();

    assertTrue(listaDiarios.contains("MEUS DIARIOS DE MATRICULA"));
    assertTrue(listaDiarios.contains("DIA1"));
    assertTrue(listaDiarios.contains("DIA2"));
    assertFalse(listaDiarios.contains("DIA3"));

    resetarSaida();
    alunoCLI.processar("consultarDiario DIA1");
    String extratoIndividual = output.toString();

    assertTrue(extratoIndividual.contains("EXTRATO DETALHADO DO DIARIO"));
    assertTrue(extratoIndividual.contains("DIARIO: DIA1"));
    assertTrue(extratoIndividual.contains("PRESENCA"));
    assertTrue(extratoIndividual.contains("FALTA"));
    assertTrue(extratoIndividual.contains("MEDIA PARCIAL: 6.5"));
    assertTrue(extratoIndividual.contains("RESUMO DE FREQUENCIA"));
    assertTrue(extratoIndividual.contains("Aulas cadastradas: 2"));
    assertTrue(extratoIndividual.contains("Frequencias lancadas: 2"));
    assertTrue(extratoIndividual.contains("Percentual de frequencia: 50.0%"));

    resetarSaida();
    alunoCLI.processar("consultarExtratosDiarios");
    String extratos = output.toString();

    assertTrue(extratos.contains("EXTRATOS DOS MEUS DIARIOS"));
    assertTrue(extratos.contains("DIARIO: DIA1"));
    assertTrue(extratos.contains("DIARIO: DIA2"));
    assertTrue(extratos.contains("MEDIA PARCIAL: 6.5"));
    assertTrue(extratos.contains("MEDIA PARCIAL: 8.0"));

    resetarSaida();
    alunoCLI.processar("consultarMeusExtratos");
    assertTrue(output.toString().contains("EXTRATOS DOS MEUS DIARIOS"));

    resetarSaida();
    alunoCLI.processar("consultarDiariosDetalhados");
    assertTrue(output.toString().contains("EXTRATOS DOS MEUS DIARIOS"));
  }

  @Test
  public void terminalDeveExporOpcaoDoAlunoParaConsultarTodosOsExtratos() throws Exception {
    definirUsuarioLogado(aluno1);
    InputStream originalIn = System.in;

    try {
      System.setIn(new ByteArrayInputStream("10\n8\n0\n".getBytes(StandardCharsets.UTF_8)));
      TerminalCLI terminalCLI = new TerminalCLI();
      terminalCLI.iniciar();
    } finally {
      System.setIn(originalIn);
    }

    String painel = output.toString();
    assertTrue(painel.contains("10. Consultar Extrato de Todos"));
    assertTrue(painel.contains("EXTRATOS DOS MEUS DIARIOS"));
    assertTrue(painel.contains("DIARIO: DIA1"));
    assertTrue(painel.contains("DIARIO: DIA2"));
  }

  private Frequencia frequencia(
      String codigoDiario,
      String aula,
      String data,
      String aluno,
      Frequencia.TipoFrequencia situacao) {
    return new Frequencia(aula, codigoDiario, data, aluno, "D1", "P1", situacao);
  }

  private void resetarSaida() {
    output.reset();
    outputError.reset();
  }

  private void definirUsuarioLogado(Usuario usuario) throws Exception {
    Field campo = AutenticacaoService.class.getDeclaredField("usuarioLogado");
    campo.setAccessible(true);
    campo.set(AutenticacaoService.getInstancia(), usuario);
  }

  private void salvarEstadoAtual() throws IOException {
    for (String arquivo : ARQUIVOS) {
      Path caminho = Path.of(arquivo);
      estadoAnterior.put(arquivo, Files.exists(caminho) ? Files.readAllBytes(caminho) : null);
    }
  }

  private void restaurarEstadoAnterior() throws IOException {
    for (Map.Entry<String, byte[]> registro : estadoAnterior.entrySet()) {
      Path caminho = Path.of(registro.getKey());
      if (registro.getValue() == null) {
        Files.deleteIfExists(caminho);
      } else {
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
