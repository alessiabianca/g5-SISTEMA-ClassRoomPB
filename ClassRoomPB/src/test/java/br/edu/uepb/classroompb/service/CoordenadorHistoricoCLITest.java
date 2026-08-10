package br.edu.uepb.classroompb.service;

import static org.junit.Assert.assertTrue;

import br.edu.uepb.classroompb.model.Aluno;
import br.edu.uepb.classroompb.model.Coordenador;
import br.edu.uepb.classroompb.model.Historico;
import br.edu.uepb.classroompb.model.StatusAcademico;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.repository.HistoricoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.view.CoordenadorCLI;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CoordenadorHistoricoCLITest {
  private final ByteArrayOutputStream output = new ByteArrayOutputStream();
  private PrintStream originalOut;

  @Before
  public void setUp() {
    new File("data/historico.txt").delete();
    originalOut = System.out;
    System.setOut(new PrintStream(output));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    AutenticacaoService.getInstancia().realizarLogout();
    new File("data/historico.txt").delete();
  }

  @Test
  public void deveRenderizarHistoricoAnaliticoDeAlunoExistente() throws Exception {
    HistoricoRepository historicoRepository = new HistoricoRepository();
    historicoRepository.salvarLote(
        List.of(
            new Historico(
                "ALUNO01", "2026.1", "ES01", "PROF01", 8.5, 87.5, StatusAcademico.APROVADO)));

    HistoricoService historicoService =
        new HistoricoService(historicoRepository, null, null, null, null);
    UsuarioRepository repoUsuario = repositorioComAluno("ALUNO01", "Aluno Teste", "CC");
    DiarioService diarioService =
        new DiarioService(new DiarioRepository(), new TurmaRepository(), repoUsuario);

    CoordenadorCLI coordenadorCLI =
        new CoordenadorCLI(null, historicoService, diarioService, repoUsuario);
    definirUsuarioLogado(
        new Coordenador("COORD01", "Coordenador Teste", "coord@test.com", "123", "CC"));

    coordenadorCLI.processar("consultarHistoricoAluno ALUNO01");

    String painel = output.toString();
    assertTrue(painel.contains("HISTORICO ACADEMICO - CONSULTA DA COORDENACAO"));
    assertTrue(painel.contains("Aluno Teste"));
    assertTrue(painel.contains("ES01"));
    assertTrue(painel.contains("PROF01"));
    assertTrue(painel.contains("8.5"));
    assertTrue(painel.contains("87.5%"));
  }

  private UsuarioRepository repositorioComAluno(String matricula, String nome, String codigoCurso) {
    return new UsuarioRepository() {
      @Override
      public Usuario buscarPorMatricula(String identificador) {
        return matricula.equalsIgnoreCase(identificador)
            ? new Aluno(matricula, nome, "aluno@test.com", "123", codigoCurso)
            : null;
      }
    };
  }

  private void definirUsuarioLogado(Usuario usuario) throws Exception {
    Field campo = AutenticacaoService.class.getDeclaredField("usuarioLogado");
    campo.setAccessible(true);
    campo.set(AutenticacaoService.getInstancia(), usuario);
  }
}
