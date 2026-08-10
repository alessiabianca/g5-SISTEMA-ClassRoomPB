package br.edu.uepb.classroompb.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import br.edu.uepb.classroompb.repository.CursoRepository;
import br.edu.uepb.classroompb.service.CursoService;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AdminCursoCLITest {

  private static final String DATA_DIR = "data";
  private static final String FILE_CURSOS = DATA_DIR + "/cursos.txt";

  private CursoService cursoService;
  private AdminCursoCLI cli;

  @Before
  public void setUp() {
    File dir = new File(DATA_DIR);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    new File(FILE_CURSOS).delete();

    CursoRepository cursoRepo = new CursoRepository();
    cursoService = new CursoService(cursoRepo);
    cli = new AdminCursoCLI(cursoService);
  }

  @After
  public void tearDown() {
    new File(FILE_CURSOS).delete();
  }

  @Test
  public void deveExibirMenuESair() {
    String input = "0\n";
    InputStream in = new ByteArrayInputStream(input.getBytes());
    System.setIn(in);

    cli = new AdminCursoCLI(cursoService); // recreate with new System.in
    cli.exibirMenu("ADMINISTRADOR");
    
    // Nothing was created
  }

  @Test
  public void deveTratarOpcaoInvalida() {
    String input = "9\n0\n";
    InputStream in = new ByteArrayInputStream(input.getBytes());
    System.setIn(in);

    cli = new AdminCursoCLI(cursoService);
    cli.exibirMenu("ADMINISTRADOR");

    // Nothing was created
  }

  @Test
  public void deveCadastrarCursoComSucesso() throws Exception {
    String input = "1\nC01\nCiencia da Computacao\n0\n";
    InputStream in = new ByteArrayInputStream(input.getBytes());
    System.setIn(in);

    cli = new AdminCursoCLI(cursoService);
    cli.exibirMenu("ADMINISTRADOR");

    CursoRepository repo = new CursoRepository();
    br.edu.uepb.classroompb.model.Curso curso = repo.buscarPorCodigo("C01");
    org.junit.Assert.assertNotNull(curso);
    assertEquals("Ciencia da Computacao", curso.getNome());
  }

  @Test
  public void deveTratarErroValidacaoExceptionPerfilIncorreto() throws Exception {
    String input = "1\nC02\nCiencia da Computacao\n0\n";
    InputStream in = new ByteArrayInputStream(input.getBytes());
    System.setIn(in);
    
    cli = new AdminCursoCLI(cursoService);
    cli.exibirMenu("ALUNO"); // Nao tem permissao
    
    CursoRepository repo = new CursoRepository();
    br.edu.uepb.classroompb.model.Curso curso = repo.buscarPorCodigo("C02");
    org.junit.Assert.assertNull(curso);
  }
}
