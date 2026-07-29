package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Professor;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import org.junit.Before;
import org.junit.Test;

public class DiarioServiceTest {

  private DiarioRepository diarioRepository;
  private TurmaRepository turmaRepository;
  private UsuarioRepository usuarioRepository;
  private DiarioService diarioService;

  @Before
  public void setUp() {
    new File("data/diarios.txt").delete();
    new File("data/turmas.txt").delete();
    new File("data/usuarios.txt").delete();

    diarioRepository = new DiarioRepository();
    turmaRepository = new TurmaRepository();
    usuarioRepository = new UsuarioRepository();

    diarioService = new DiarioService(diarioRepository, turmaRepository, usuarioRepository);

    // Massa de dados inicial: Turma ofertada e Professor cadastrado
    turmaRepository.salvar(new Turma("ES01", "2026.1", 30));
    usuarioRepository.salvar(
        new Professor("PROF123", "Professor Teste", "prof@uepb.edu.br", "123456"));
  }

  @Test
  public void deveCriarDiarioComSucessoQuandoDadosForemValidos() throws Exception {
    var diario =
        diarioService.criarDiario(
            "DIA_ES01_20261",
            "ES01",
            "2026.1",
            "Engenharia de Software I",
            "PROF123",
            "SEG 08:00-10:00",
            "Sala 101",
            60);

    assertNotNull(diario);
    assertEquals("DIA_ES01_20261", diario.getCodigo());
    assertEquals("PROF123", diario.getMatriculaProfessor());
    assertEquals(1, diarioRepository.buscarTodos().size());
  }

  @Test(expected = ValidacaoException.class)
  public void deveBloquearCriacaoDeDiarioSemTurmaOfertada() throws Exception {
    // Tenta criar diário para uma turma inexistente no período (BD01)
    diarioService.criarDiario(
        "DIA_BD01_20261",
        "BD01",
        "2026.1",
        "Banco de Dados",
        "PROF123",
        "TER 08:00-10:00",
        "Sala 102",
        60);
  }

  @Test(expected = ValidacaoException.class)
  public void deveBloquearCriacaoDeDiarioOrfaoSemProfessorCadastrado() throws Exception {
    // RN18: Tenta criar diário com professor inexistente (PROF_INEXISTENTE)
    diarioService.criarDiario(
        "DIA_ES01_20261",
        "ES01",
        "2026.1",
        "Engenharia de Software I",
        "PROF_INEXISTENTE",
        "SEG 08:00-10:00",
        "Sala 101",
        60);
  }

  @Test(expected = ChoqueHorarioException.class)
  public void deveBloquearChoqueDeHorarioDeProfessorEntreDiariosAtivos() throws Exception {
    // RF12: Cadastra primeiro diário para o PROF123 no período 2026.1 e horário SEG 08:00-10:00
    diarioService.criarDiario(
        "DIA_ES01_20261",
        "ES01",
        "2026.1",
        "Engenharia de Software I",
        "PROF123",
        "SEG 08:00-10:00",
        "Sala 101",
        60);

    // Ofertando segunda turma no mesmo período para simular o choque
    turmaRepository.salvar(new Turma("BD01", "2026.1", 30));

    // Tenta cadastrar um segundo diário para o MESMO professor, no MESMO período e MESMO horário
    diarioService.criarDiario(
        "DIA_BD01_20261",
        "BD01",
        "2026.1",
        "Banco de Dados I",
        "PROF123",
        "SEG 08:00-10:00",
        "Sala 102",
        60);
  }

  @Test(expected = ValidacaoException.class)
  public void deveBloquearCriacaoDeDiarioComCodigoDuplicado() throws Exception {
    diarioService.criarDiario(
        "DIA_ES01_20261",
        "ES01",
        "2026.1",
        "Engenharia de Software I",
        "PROF123",
        "SEG 08:00-10:00",
        "Sala 101",
        60);

    // Tenta criar com o mesmo código 'DIA_ES01_20261'
    diarioService.criarDiario(
        "DIA_ES01_20261",
        "ES01",
        "2026.1",
        "Engenharia de Software I - Turma B",
        "PROF123",
        "QUA 08:00-10:00",
        "Sala 103",
        60);
  }
}