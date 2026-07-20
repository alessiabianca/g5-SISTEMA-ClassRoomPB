package br.edu.uepb.classroompb.repository;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Matricula.StatusMatricula;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class MatriculaRepositoryTest {
  private MatriculaRepository repository;
  private static final String FILE_PATH = "data/matriculas.txt";

  @Before
  public void setUp() {
    File f = new File(FILE_PATH);
    if (f.exists()) f.delete();
    repository = new MatriculaRepository();
  }

  @Test
  public void testSalvarEBuscarTodas() throws Exception {
    Matricula m = new Matricula("AL123", "D01", "P01", StatusMatricula.CONFIRMADA);
    repository.salvar(m);

    List<Matricula> matriculas = repository.buscarTodas();
    assertEquals(1, matriculas.size());
    Matricula lida = matriculas.get(0);
    assertEquals("AL123", lida.getMatriculaAluno());
    assertEquals("D01", lida.getCodigoDisciplina());
    assertEquals("P01", lida.getPeriodo());
    assertEquals(StatusMatricula.CONFIRMADA, lida.getStatus());
  }

  @Test
  public void testAtualizarArquivoCompleto() throws Exception {
    Matricula m = new Matricula("AL123", "D01", "P01", StatusMatricula.CONFIRMADA);
    repository.salvar(m);

    List<Matricula> matriculas = repository.buscarTodas();
    matriculas.get(0).transitarPara(StatusMatricula.REJEITADA);

    repository.atualizarArquivoCompleto(matriculas);

    List<Matricula> lidas = repository.buscarTodas();
    assertEquals(StatusMatricula.REJEITADA, lidas.get(0).getStatus());
  }
}
