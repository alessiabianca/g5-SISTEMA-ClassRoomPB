package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Disciplina;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;
import br.edu.uepb.classroompb.service.exception.ChoqueSalaException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class TurmaServiceTest {

  private TurmaService turmaService;
  private FakeTurmaRepository fakeTurmaRepository;
  private FakePeriodoRepository fakePeriodoRepository;
  private FakeDisciplinaRepository fakeDisciplinaRepository;

  private static class FakeTurmaRepository extends TurmaRepository {
    private final List<Turma> turmasEmMemoria = new ArrayList<>();

    @Override
    public void salvar(Turma turma) {
      turmasEmMemoria.add(turma);
    }

    @Override
    public List<Turma> buscarTodas() {
      return new ArrayList<>(turmasEmMemoria);
    }

    @Override
    public void atualizarArquivoCompleto(List<Turma> turmasAtualizadas) {
      turmasEmMemoria.clear();
      turmasEmMemoria.addAll(turmasAtualizadas);
    }
  }

  private static class FakePeriodoRepository extends PeriodoRepository {
    private final List<Periodo> periodosEmMemoria = new ArrayList<>();

    public void adicionarNoFake(Periodo p) {
      periodosEmMemoria.add(p);
    }

    @Override
    public Periodo buscarPorCodigo(String codigo) {
      for (Periodo p : periodosEmMemoria) {
        if (p.getCodigo().equalsIgnoreCase(codigo)) {
          return p;
        }
      }
      return null;
    }

    public List<Periodo> listarTodos() {
      return new ArrayList<>(periodosEmMemoria);
    }
  }

  private static class FakeDisciplinaRepository extends DisciplinaRepository {
    private final List<Disciplina> disciplinasEmMemoria = new ArrayList<>();

    public void adicionarNoFake(Disciplina d) {
      disciplinasEmMemoria.add(d);
    }

    @Override
    public Disciplina buscarPorCodigo(String codigo) {
      for (Disciplina d : disciplinasEmMemoria) {
        if (d.getCodigo().equalsIgnoreCase(codigo)) {
          return d;
        }
      }
      return null;
    }

    @Override
    public List<Disciplina> listarTodas() {
      return new ArrayList<>(disciplinasEmMemoria);
    }
  }

  @Before
  public void setUp() {
    fakeTurmaRepository = new FakeTurmaRepository();
    fakePeriodoRepository = new FakePeriodoRepository();
    fakeDisciplinaRepository = new FakeDisciplinaRepository();
    turmaService =
        new TurmaService(fakeTurmaRepository, fakePeriodoRepository, fakeDisciplinaRepository);
  }

  // ====================================================================
  // TESTES - OFERTA DE TURMA
  // ====================================================================

  @Test
  public void deveOfertarTurmaComSucessoQuandoPeriodoAtivoEDisciplinaExistente() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

    turmaService.ofertarTurma(
        "ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1", "COORDENADOR");
    assertEquals(1, fakeTurmaRepository.buscarTodas().size());
  }

  @Test
  public void deveLancarExcecaoQuandoUsuarioNaoForCoordenador() {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

    assertThrows(
        ValidacaoException.class,
        () -> {
          turmaService.ofertarTurma(
              "ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1", "ALUNO");
        });
  }

  @Test
  public void deveLancarExcecaoQuandoDisciplinaNaoExistirNoSistema() {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));

    assertThrows(
        ValidacaoException.class,
        () -> {
          turmaService.ofertarTurma(
              "ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1", "COORDENADOR");
        });
  }

  @Test
  public void deveLancarExcecaoQuandoPeriodoNaoEstiverAtivo() {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "PLANEJADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

    assertThrows(
        ValidacaoException.class,
        () -> {
          turmaService.ofertarTurma(
              "ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1", "COORDENADOR");
        });
  }

  @Test
  public void deveLancarExcecaoQuandoProfessorJaTemTurmaNoMesmoHorarioEPeriodo() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));
    fakeDisciplinaRepository.adicionarNoFake(new Disciplina("BD01", "Banco de Dados", 60, 4, null));

    fakeTurmaRepository.salvar(
        new Turma("BD01", "PROF_123", "2026.1", 30, "08:00-10:00", "Sala 2"));

    assertThrows(
        ChoqueHorarioException.class,
        () -> {
          turmaService.ofertarTurma(
              "ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1", "COORDENADOR");
        });
  }

  @Test
  public void deveLancarExcecaoQuandoHouverChoqueDeSalaNoMesmoHorarioEPeriodo() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));
    fakeDisciplinaRepository.adicionarNoFake(new Disciplina("BD01", "Banco de Dados", 60, 4, null));

    fakeTurmaRepository.salvar(
        new Turma("BD01", "PROF_AAA", "2026.1", 30, "08:00-10:00", "Sala 1"));

    assertThrows(
        ChoqueSalaException.class,
        () -> {
          turmaService.ofertarTurma(
              "ES01", "PROF_BBB", "2026.1", 40, "08:00-10:00", "Sala 1", "COORDENADOR");
        });
  }

  @Test
  public void devePermitirMesmaSalaEMesmoHorarioSeOsPeriodosLetivosForemDiferentes()
      throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.2", "INICIADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));
    fakeDisciplinaRepository.adicionarNoFake(new Disciplina("BD01", "Banco de Dados", 60, 4, null));

    fakeTurmaRepository.salvar(
        new Turma("BD01", "PROF_AAA", "2026.1", 30, "08:00-10:00", "Sala 1"));

    turmaService.ofertarTurma(
        "ES01", "PROF_BBB", "2026.2", 40, "08:00-10:00", "Sala 1", "COORDENADOR");

    assertEquals(2, fakeTurmaRepository.buscarTodas().size());
  }

  @Test
  public void deveLancarExcecaoQuandoAtributosObrigatoriosContiveremApenasEspacos() {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          turmaService.ofertarTurma(
              "ES01", "PROF_123", "2026.1", 40, "   ", "Sala 1", "COORDENADOR");
        });

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          turmaService.ofertarTurma(
              "ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "    ", "COORDENADOR");
        });
  }

  @Test
  public void deveLancarExcecaoQuandoLimiteDeVagasForInvalido() {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

    assertThrows(
        ValidacaoException.class,
        () -> {
          turmaService.ofertarTurma(
              "ES01", "PROF_123", "2026.1", 0, "08:00-10:00", "Sala 1", "COORDENADOR");
        });

    assertThrows(
        ValidacaoException.class,
        () -> {
          turmaService.ofertarTurma(
              "ES01", "PROF_123", "2026.1", -5, "08:00-10:00", "Sala 1", "COORDENADOR");
        });
  }

  @Test
  public void deveLancarExcecaoQuandoCamposForemNulos() {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          turmaService.ofertarTurma(
              "ES01", null, "2026.1", 40, "08:00-10:00", "Sala 1", "COORDENADOR");
        });

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          turmaService.ofertarTurma(
              "ES01", "PROF_123", "2026.1", 40, null, "Sala 1", "COORDENADOR");
        });

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          turmaService.ofertarTurma(
              "ES01", "PROF_123", "2026.1", 40, "08:00-10:00", null, "COORDENADOR");
        });
  }

  @Test
  public void deveLancarExcecaoQuandoProfessorForVazio() {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          turmaService.ofertarTurma(
              "ES01", "   ", "2026.1", 40, "08:00-10:00", "Sala 1", "COORDENADOR");
        });
  }

  // ====================================================================
  // TESTES - EDIÇÃO E CANCELAMENTO & VALIDAÇÕES DE DOCENTE
  // ====================================================================

  @Test
  public void deveEditarTurmaComSucessoQuandoPeriodoEstiverPlanejado() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.2", "PLANEJADO"));
    fakeTurmaRepository.salvar(
        new Turma("ES01", "PROF_123", "2026.2", 30, "08:00-10:00", "Sala 1"));

    // ADAPTAÇÃO US13: Incluído o parâmetro da nova matrícula do professor ("PROF_789") na chamada
    // de edição
    turmaService.editarTurma("ES01", "2026.2", "PROF_789", 50, "14:00-16:00", "Lab 3");

    List<Turma> turmas = fakeTurmaRepository.buscarTodas();
    assertEquals(1, turmas.size());
    Turma turmaEditada = turmas.get(0);
    assertEquals("PROF_789", turmaEditada.getMatriculaProfessor());
    assertEquals(50, turmaEditada.getVagas());
    assertEquals("14:00-16:00", turmaEditada.getHorario());
    assertEquals("Lab 3", turmaEditada.getSala());
  }

  @Test
  public void deveLancarExcecaoAoTentarEditarTurmaDeixandoProfessorNuloOuVazio() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.2", "PLANEJADO"));
    fakeTurmaRepository.salvar(
        new Turma("ES01", "PROF_123", "2026.2", 30, "08:00-10:00", "Sala 1"));

    // Impede professor nulo na edição
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          turmaService.editarTurma("ES01", "2026.2", null, 50, "14:00-16:00", "Lab 3");
        });

    // Impede professor vazio na edição
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          turmaService.editarTurma("ES01", "2026.2", "   ", 50, "14:00-16:00", "Lab 3");
        });
  }

  @Test
  public void deveCancelarTurmaComSucessoRemovendoDoRepositorio() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.2", "PLANEJADO"));
    fakeTurmaRepository.salvar(
        new Turma("ES01", "PROF_123", "2026.2", 30, "08:00-10:00", "Sala 1"));
    fakeTurmaRepository.salvar(
        new Turma("BD01", "PROF_456", "2026.2", 40, "10:00-12:00", "Sala 2"));

    turmaService.cancelarTurma("ES01", "2026.2");

    List<Turma> turmasRestantes = fakeTurmaRepository.buscarTodas();
    assertEquals(1, turmasRestantes.size());
    assertEquals("BD01", turmasRestantes.get(0).getCodigoDisciplina());
  }

  @Test
  public void deveImpedirEdicaoSePeriodoEstiverIniciado() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeTurmaRepository.salvar(
        new Turma("ES01", "PROF_123", "2026.1", 30, "08:00-10:00", "Sala 1"));

    assertThrows(
        IllegalStateException.class,
        () -> {
          // ADAPTAÇÃO US13: Passando o professor mesmo no cenário de falha por período
          turmaService.editarTurma("ES01", "2026.1", "PROF_123", 40, "10:00-12:00", "Sala 2");
        });
  }

  @Test
  public void deveImpedirCancelamentoSePeriodoEstiverEncerrado() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2025.2", "ENCERRADO"));
    fakeTurmaRepository.salvar(new Turma("BD01", "PROF_456", "2025.2", 40, "14:00-16:00", "Lab 1"));

    assertThrows(
        IllegalStateException.class,
        () -> {
          turmaService.cancelarTurma("BD01", "2025.2");
        });
  }

  // ====================================================================
  // LIMITE DE VAGAS E TURMA LOTADA
  // ====================================================================

  @Test
  public void deveLancarExcecaoQuandoNaoHouerVagasDisponiveisNaTurma() throws Exception {
    // Cria uma turma estável com limite físico de 30 vagas
    Turma turmaLotada = new Turma("ES02", "PROF_333", "2026.1", 30, "08:00-10:00", "Sala_B3");

    // Simula o preenchimento exato do teto físico (30 vagas ocupadas por 30 alunos fictícios)
    turmaLotada.setVagasOcupadas(30);

    // Valida se o motor automatizado dispara o bloqueio correto ao checar o saldo zerado
    ValidacaoException excecao =
        assertThrows(
            ValidacaoException.class,
            () -> {
              turmaService.verificarDisponibilidadeVagas(turmaLotada);
            });

    // Garante que a mensagem de erro bate exatamente com o requisito da Task 2108
    assertEquals("Erro: Não há vagas disponíveis nesta turma.", excecao.getMessage());
  }

  @Test
  public void devePermitirVerificacaoComSucessoSeAindaHouverSaldoDeVagas() throws Exception {
    // Cria uma turma com limite físico de 30 vagas
    Turma turmaComSaldo = new Turma("ES02", "PROF_333", "2026.1", 30, "08:00-10:00", "Sala_B3");

    // Simula que apenas 29 vagas foram preenchidas (ainda resta 1 vaga de saldo)
    turmaComSaldo.setVagasOcupadas(29);

    // O método não deve lançar nenhuma exceção, permitindo a transição segura
    try {
      turmaService.verificarDisponibilidadeVagas(turmaComSaldo);
    } catch (ValidacaoException e) {
      fail("Não deveria ter lançado exceção, pois a turma ainda possui 1 vaga disponível.");
    }
  }

  // ====================================================================
  // CONSISTÊNCIA DE PRÉ-REQUISITOS
  // ====================================================================

  @Test
  public void devePermitirMatriculaQuandoAlunoCumprirTodosOsPreRequisitos() throws Exception {
    // Criamos os pré-requisitos válidos na base em memória
    List<String> preReqs = new ArrayList<>();
    preReqs.add("P1"); // Disciplina base obrigatória

    // Cadastramos a disciplina avançada vinculando "P1" como dependência
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("P2", "Programação II", 60, 4, preReqs));

    // "202601" está mockado no nosso Service para simular que já pagou "P1" (veterano)
    String matriculaAlunoVeterano = "202601";
    String codigoDisciplinaAvancada = "P2";

    // O motor não deve lançar nenhuma exceção, permitindo a transição com sucesso
    try {
      turmaService.validarPreRequisitos(matriculaAlunoVeterano, codigoDisciplinaAvancada);
    } catch (ValidacaoException e) {
      fail("Deveria ter permitido a matrícula, pois o estudante cumpre o pré-requisito P1.");
    }
  }

  @Test
  public void deveBloquearMatriculaQuandoAlunoNaoCumprirOsPreRequisitosNecessarios()
      throws Exception {
    // Criamos os pré-requisitos estáveis na base em memória
    List<String> preReqs = new ArrayList<>();
    preReqs.add("P1");

    // Cadastramos a disciplina avançada "P2" exigindo "P1"
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("P2", "Programação II", 60, 4, preReqs));

    // Usamos uma matrícula fictícia de calouro que não possui histórico de aprovações
    String matriculaAlunoCalouro = "CALOURO_2026";
    String codigoDisciplinaAvancada = "P2";

    // Valida se o motor automatizado dispara o bloqueio acadêmico com sucesso
    ValidacaoException excecao =
        assertThrows(
            ValidacaoException.class,
            () -> {
              turmaService.validarPreRequisitos(matriculaAlunoCalouro, codigoDisciplinaAvancada);
            });

    // Verifica se a mensagem retornada detalha as pendências encontradas na varredura
    assertTrue(excecao.getMessage().contains("Erro de Consistência Acadêmica"));
    assertTrue(excecao.getMessage().contains("P1"));
  }

  // ====================================================================
  //  CONSULTA E TRATAMENTO DE BASE DE DADOS
  // ====================================================================

  @Test
  public void deveRetornarListaVaziaDeFormaSeguraQuandoNaoHouverTurmasSalvas() {
    // O repositório inicia completamente vazio (sem nenhuma turma salva)
    List<Turma> resultado = turmaService.listarTurmasDisponiveis();

    // Valida que o método responde de forma segura, sem NullPointerException
    assertNotNull("A lista de ofertas nunca deve ser nula.", resultado);
    assertTrue(
        "A lista de ofertas deve estar vazia quando não houver persistência.", resultado.isEmpty());
    assertEquals(0, resultado.size());
  }

  @Test
  public void deveRetornarAQuantidadeExataDeTurmasQuandoHouverDadosPersistidos() {
    // Inserimos duas turmas controladas no nosso repositório fake
    fakeTurmaRepository.salvar(
        new Turma("ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1"));
    fakeTurmaRepository.salvar(
        new Turma("BD01", "PROF_456", "2026.1", 30, "10:00-12:00", "Sala 2"));

    // Executa a consulta através do motor de serviço
    List<Turma> resultado = turmaService.listarTurmasDisponiveis();

    // Valida se a quantidade extraída corresponde exatamente aos registros persistidos
    assertNotNull(resultado);
    assertEquals(
        "O motor deve recuperar a quantidade exata de turmas gravadas.", 2, resultado.size());
    assertEquals("ES01", resultado.get(0).getCodigoDisciplina());
    assertEquals("BD01", resultado.get(1).getCodigoDisciplina());
  }

  // ====================================================================
  // TESTES DA TASK 2105 (US16) - VALIDAÇÃO DE MATRÍCULA E EXCEÇÕES
  // ====================================================================

  @Test
  public void deveEfetivarMatriculaComSucessoIncrementandoVagasOcupadas() throws Exception {
    // Configura o cenário com período ativo e disciplina válida
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

    // Cria e salva uma turma que possui 0 vagas ocupadas de 40 totais
    Turma turmaDisponivel = new Turma("ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1");
    fakeTurmaRepository.salvar(turmaDisponivel);

    // Executa a solicitação de matrícula para o aluno
    turmaService.processarMatriculaAutomatica("202601", "ES01", "2026.1");

    // Verifica se a turma foi alterada no repositório e se a vaga foi computada
    List<Turma> turmas = fakeTurmaRepository.buscarTodas();
    assertEquals(1, turmas.get(0).getVagasOcupadas());
  }

  @Test
  public void deveBloquearMatriculaQuandoATurmaAlvoNaoPossuirVagasDisponiveis() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

    // Cria uma turma artificialmente lotada (vagas ocupadas == limite de vagas)
    Turma turmaLotada = new Turma("ES01", "PROF_123", "2026.1", 30, 30, "08:00-10:00", "Sala 1");
    fakeTurmaRepository.salvar(turmaLotada);

    // Valida se o motor bloqueia a operação disparando a ValidacaoException de lotação
    ValidacaoException excecao =
        assertThrows(
            ValidacaoException.class,
            () -> {
              turmaService.processarMatriculaAutomatica("202602", "ES01", "2026.1");
            });

    assertEquals("Erro: Não há vagas disponíveis nesta turma.", excecao.getMessage());
  }

  @Test
  public void deveBloquearMatriculaQuandoOPeriodoLetivoNaoEstiverAtivo() throws Exception {
    // Configura o período letivo como PLANEJADO (inválido para matrículas)
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.2", "PLANEJADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

    Turma turmaPlanejada = new Turma("ES01", "PROF_123", "2026.2", 40, "08:00-10:00", "Sala 1");
    fakeTurmaRepository.salvar(turmaPlanejada);

    // Valida se o motor bloqueia o fluxo devido ao ciclo de vida do período
    ValidacaoException excecao =
        assertThrows(
            ValidacaoException.class,
            () -> {
              turmaService.processarMatriculaAutomatica("202601", "ES01", "2026.2");
            });

    assertTrue(excecao.getMessage().contains("não está aberto para matrículas"));
  }
}
