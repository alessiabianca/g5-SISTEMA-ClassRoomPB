package br.edu.uepb.classroompb.repository;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Turma;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class TurmaRepositoryTest {
  private TurmaRepository repository;
  private static final String FILE_PATH = "data/turmas.txt";

  @Before
  public void setUp() {
    File f = new File(FILE_PATH);
    if (f.exists()) f.delete();
    repository = new TurmaRepository();
  }

  @Test
  public void testSalvarEBuscarTodas() {
    Turma t = new Turma("D01", "PR123", "P01", 40, "SEG", "S01");
    repository.salvar(t);

    List<Turma> turmas = repository.buscarTodas();
    assertEquals(1, turmas.size());
    Turma lida = turmas.get(0);
    assertEquals("D01", lida.getCodigoDisciplina());
    assertEquals("PR123", lida.getMatriculaProfessor());
    assertEquals("P01", lida.getPeriodo());
    assertEquals(40, lida.getVagas());
    assertEquals("SEG", lida.getHorario());
    assertEquals("S01", lida.getSala());
    assertEquals(0, lida.getVagasOcupadas());
  }

  @Test
  public void testAtualizarArquivoCompleto() {
    Turma t = new Turma("D01", "PR123", "P01", 40, "SEG", "S01");
    repository.salvar(t);

    List<Turma> turmas = repository.buscarTodas();
    turmas.get(0).setVagasOcupadas(1);

    repository.atualizarArquivoCompleto(turmas);

    List<Turma> lidas = repository.buscarTodas();
    assertEquals(1, lidas.get(0).getVagasOcupadas());
  }
}
