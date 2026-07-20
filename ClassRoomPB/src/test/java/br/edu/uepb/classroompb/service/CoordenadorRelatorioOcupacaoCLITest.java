package br.edu.uepb.classroompb.service;

import static org.junit.Assert.assertTrue;

import br.edu.uepb.classroompb.model.Coordenador;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.view.CoordenadorCLI;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CoordenadorRelatorioOcupacaoCLITest {
  private final ByteArrayOutputStream output = new ByteArrayOutputStream();
  private PrintStream originalOut;

  @Before
  public void setUp() throws Exception {
    File dataDir = new File("data");
    if (!dataDir.exists()) {
      dataDir.mkdirs();
    }

    new File("data/turmas.txt").delete();
    new File("data/matriculas.txt").delete();

    originalOut = System.out;
    System.setOut(new PrintStream(output));
    definirUsuarioLogado(
        new Coordenador("COORD_RF41", "Coordenador RF41", "coord41@test.com", "123", "CC"));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    AutenticacaoService.getInstancia().realizarLogout();
    new File("data/turmas.txt").delete();
    new File("data/matriculas.txt").delete();
  }

  @Test
  public void deveRenderizarRelatorioDeOcupacaoDeVagasPorPeriodo() throws Exception {
    TurmaRepository turmaRepository = new TurmaRepository();
    MatriculaRepository matriculaRepository = new MatriculaRepository();
    TurmaService turmaService =
        new TurmaService(turmaRepository, new PeriodoRepository(), new DisciplinaRepository());
    CoordenadorCLI coordenadorCLI = new CoordenadorCLI(turmaService, null, new UsuarioRepository());

    turmaRepository.salvar(new Turma("ES41", "PROF_A", "2026.2", 4, "24M12", "Sala_A"));
    turmaRepository.salvar(new Turma("BD41", "PROF_B", "2027.1", 10, "35M12", "Sala_B"));
    matriculaRepository.salvar(
        new Matricula("ALUNO_001", "ES41", "2026.2", Matricula.StatusMatricula.CONFIRMADA));
    matriculaRepository.salvar(
        new Matricula("ALUNO_002", "ES41", "2026.2", Matricula.StatusMatricula.CONFIRMADA));
    matriculaRepository.salvar(
        new Matricula("ALUNO_FILA", "ES41", "2026.2", Matricula.StatusMatricula.ESPERA));

    coordenadorCLI.processar("gerarRelatorioOcupacaoVagas 2026.2");

    String painel = output.toString();
    assertTrue(painel.contains("RELATORIO DE OCUPACAO DE VAGAS - RF41"));
    assertTrue(painel.contains("ESCOPO: PERIODO: 2026.2"));
    assertTrue(painel.contains("ES41"));
    assertTrue(painel.contains("50.0%"));
    assertTrue(painel.contains("ALUNOS EM LISTA DE ESPERA    : 1"));
    assertTrue(painel.contains("DENSIDADE GERAL              : 50.0%"));
  }

  private void definirUsuarioLogado(Usuario usuario) throws Exception {
    Field campo = AutenticacaoService.class.getDeclaredField("usuarioLogado");
    campo.setAccessible(true);
    campo.set(AutenticacaoService.getInstancia(), usuario);
  }
}
