package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Disciplina;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
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

  @Test
  public void deveOfertarTurmaComSucessoQuandoPeriodoAtivoEDisciplinaExistente() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

    turmaService.ofertarTurma("ES01", "2026.1", 40, "COORDENADOR");
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
          turmaService.ofertarTurma("ES01", "2026.1", 40, "ALUNO");
        });
  }

  @Test
  public void deveLancarExcecaoQuandoDisciplinaNaoExistirNoSistema() {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));

    assertThrows(
        ValidacaoException.class,
        () -> {
          turmaService.ofertarTurma("ES01", "2026.1", 40, "COORDENADOR");
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
          turmaService.ofertarTurma("ES01", "2026.1", 40, "COORDENADOR");
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
          turmaService.ofertarTurma("ES01", "2026.1", 0, "COORDENADOR");
        });

    assertThrows(
        ValidacaoException.class,
        () -> {
          turmaService.ofertarTurma("ES01", "2026.1", -5, "COORDENADOR");
        });
  }

  @Test
  public void deveEditarTurmaComSucessoQuandoPeriodoEstiverPlanejado() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.2", "PLANEJADO"));
    fakeTurmaRepository.salvar(new Turma("ES01", "2026.2", 30));

    turmaService.editarTurma("ES01", "2026.2", 50);

    List<Turma> turmas = fakeTurmaRepository.buscarTodas();
    assertEquals(1, turmas.size());
    Turma turmaEditada = turmas.get(0);
    assertEquals(50, turmaEditada.getVagas());
  }

  @Test
  public void deveCancelarTurmaComSucessoRemovendoDoRepositorio() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.2", "PLANEJADO"));
    fakeTurmaRepository.salvar(new Turma("ES01", "2026.2", 30));
    fakeTurmaRepository.salvar(new Turma("BD01", "2026.2", 40));

    turmaService.cancelarTurma("ES01", "2026.2");

    List<Turma> turmasRestantes = fakeTurmaRepository.buscarTodas();
    assertEquals(1, turmasRestantes.size());
    assertEquals("BD01", turmasRestantes.get(0).getCodigoDisciplina());
  }

  @Test
  public void deveImpedirEdicaoSePeriodoEstiverIniciado() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeTurmaRepository.salvar(new Turma("ES01", "2026.1", 30));

    assertThrows(
        IllegalStateException.class,
        () -> {
          turmaService.editarTurma("ES01", "2026.1", 40);
        });
  }

  @Test
  public void deveImpedirCancelamentoSePeriodoEstiverEncerrado() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2025.2", "ENCERRADO"));
    fakeTurmaRepository.salvar(new Turma("BD01", "2025.2", 40));

    assertThrows(
        IllegalStateException.class,
        () -> {
          turmaService.cancelarTurma("BD01", "2025.2");
        });
  }

  @Test
  public void deveLancarExcecaoQuandoNaoHouerVagasDisponiveisNaTurma() throws Exception {

    Turma turmaLotada = new Turma("ES02", "2026.1", 30, 30);

    ValidacaoException excecao =
        assertThrows(
            ValidacaoException.class,
            () -> {
              turmaService.verificarDisponibilidadeVagas(turmaLotada);
            });

    assertEquals("Erro: Não há vagas disponíveis nesta turma.", excecao.getMessage());
  }

  @Test
  public void devePermitirVerificacaoComSucessoSeAindaHouverSaldoDeVagas() throws Exception {

    Turma turmaComSaldo = new Turma("ES02", "2026.1", 30, 29);

    try {
      turmaService.verificarDisponibilidadeVagas(turmaComSaldo);
    } catch (ValidacaoException e) {
      fail("Não deveria ter lançado exceção, pois a turma ainda possui 1 vaga disponível.");
    }
  }

  @Test
  public void devePermitirMatriculaQuandoAlunoCumprirTodosOsPreRequisitos() throws Exception {

    List<String> preReqs = new ArrayList<>();
    preReqs.add("P1");

    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("P2", "Programação II", 60, 4, preReqs));

    String matriculaAlunoVeterano = "202601";
    String codigoDisciplinaAvancada = "P2";

    try {
      turmaService.validarPreRequisitos(matriculaAlunoVeterano, codigoDisciplinaAvancada);
    } catch (ValidacaoException e) {
      fail("Deveria ter permitido a matrícula, pois o estudante cumpre o pré-requisito P1.");
    }
  }

  @Test
  public void deveBloquearMatriculaQuandoAlunoNaoCumprirOsPreRequisitosNecessarios()
      throws Exception {

    List<String> preReqs = new ArrayList<>();
    preReqs.add("P1");

    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("P2", "Programação II", 60, 4, preReqs));

    String matriculaAlunoCalouro = "CALOURO_2026";
    String codigoDisciplinaAvancada = "P2";

    ValidacaoException excecao =
        assertThrows(
            ValidacaoException.class,
            () -> {
              turmaService.validarPreRequisitos(matriculaAlunoCalouro, codigoDisciplinaAvancada);
            });

    assertTrue(excecao.getMessage().contains("Erro de Consistência Acadêmica"));
    assertTrue(excecao.getMessage().contains("P1"));
  }

  @Test
  public void deveRetornarListaVaziaDeFormaSeguraQuandoNaoHouverTurmasSalvas() {

    List<Turma> resultado = turmaService.listarTurmasDisponiveis();

    assertNotNull("A lista de ofertas nunca deve ser nula.", resultado);
    assertTrue(
        "A lista de ofertas deve estar vazia quando não houver persistência.", resultado.isEmpty());
    assertEquals(0, resultado.size());
  }

  @Test
  public void deveRetornarAQuantidadeExataDeTurmasQuandoHouverDadosPersistidos() {

    fakeTurmaRepository.salvar(new Turma("ES01", "2026.1", 40));
    fakeTurmaRepository.salvar(new Turma("BD01", "2026.1", 30));

    List<Turma> resultado = turmaService.listarTurmasDisponiveis();

    assertNotNull(resultado);
    assertEquals(
        "O motor deve recuperar a quantidade exata de turmas gravadas.", 2, resultado.size());
    assertEquals("ES01", resultado.get(0).getCodigoDisciplina());
    assertEquals("BD01", resultado.get(1).getCodigoDisciplina());
  }

  @Test
  public void deveEfetivarMatriculaComSucessoIncrementandoVagasOcupadas() throws Exception {

    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

    Turma turmaDisponivel = new Turma("ES01", "2026.1", 40);
    fakeTurmaRepository.salvar(turmaDisponivel);

    turmaService.processarMatriculaAutomatica("202601", "ES01", "2026.1");

    List<Turma> turmas = fakeTurmaRepository.buscarTodas();
    assertEquals(1, turmas.get(0).getVagasOcupadas());
  }

  @Test
  public void deveBloquearMatriculaQuandoATurmaAlvoNaoPossuirVagasDisponiveis() throws Exception {
    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

    Turma turmaLotada = new Turma("ES01", "2026.1", 30, 30);
    fakeTurmaRepository.salvar(turmaLotada);

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

    fakePeriodoRepository.adicionarNoFake(new Periodo("2026.2", "PLANEJADO"));
    fakeDisciplinaRepository.adicionarNoFake(
        new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

    Turma turmaPlanejada = new Turma("ES01", "2026.2", 40);
    fakeTurmaRepository.salvar(turmaPlanejada);

    ValidacaoException excecao =
        assertThrows(
            ValidacaoException.class,
            () -> {
              turmaService.processarMatriculaAutomatica("202601", "ES01", "2026.2");
            });

    assertTrue(excecao.getMessage().contains("não está aberto para matrículas"));
  }
}