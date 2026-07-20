package br.edu.uepb.classroompb.repository;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Nota;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class NotaRepositoryTest {
  private NotaRepository repository;
  private static final String FILE_PATH = "data/notas.txt";

  @Before
  public void setUp() {
    File f = new File(FILE_PATH);
    if (f.exists()) f.delete();
    repository = new NotaRepository();
  }

  @Test
  public void testSalvarEBuscarTodas() {
    Nota n = new Nota("AL123", "D01", "P01", 8.0, 9.0, 8.5);
    repository.salvar(n);
    
    List<Nota> notas = repository.buscarTodas();
    assertEquals(1, notas.size());
    Nota lida = notas.get(0);
    assertEquals("AL123", lida.getMatriculaAluno());
    assertEquals("D01", lida.getCodigoDisciplina());
    assertEquals("P01", lida.getPeriodo());
    assertEquals(8.0, lida.getNota1(), 0.01);
    assertEquals(9.0, lida.getNota2(), 0.01);
  }

  @Test
  public void testBuscarPorAlunoEDisciplina() {
    Nota n1 = new Nota("AL123", "D01", "P01", 8.0, 9.0, 8.5);
    Nota n2 = new Nota("AL123", "D02", "P01", 5.0, 6.0, 5.5);
    repository.salvar(n1);
    repository.salvar(n2);
    
    Nota enc = repository.buscarPorAlunoEDisciplina("AL123", "D02", "P01");
    assertNotNull(enc);
    assertEquals("D02", enc.getCodigoDisciplina());
  }

  @Test
  public void testBuscarPorAlunoEDisciplinaNaoEncontrada() {
    Nota enc = repository.buscarPorAlunoEDisciplina("AL123", "D03", "P01");
    assertNull(enc);
  }
}
