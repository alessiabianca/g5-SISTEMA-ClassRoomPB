package br.edu.uepb.classroompb.repository;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Turma;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class TurmaRepositoryTest {

  private TurmaRepository turmaRepository;

  @Before
  public void setUp() {
    java.io.File f = new java.io.File("data/turmas.txt");
    if (f.exists()) f.delete();
    turmaRepository = new TurmaRepository();
  }

  @Test
  public void testSalvarEBuscarTodas() {
    Turma turma = new Turma("D01", "P01", 40);
    turmaRepository.salvar(turma);

    List<Turma> turmas = turmaRepository.buscarTodas();
    assertNotNull(turmas);
    assertFalse(turmas.isEmpty());

    boolean encontrada = false;
    for (Turma t : turmas) {
      if (t.getCodigoDisciplina().equalsIgnoreCase("D01")
          && t.getPeriodo().equalsIgnoreCase("P01")) {
        encontrada = true;
        assertEquals(40, t.getVagas());
        break;
      }
    }
    assertTrue(encontrada);
  }

  @Test
  public void testAtualizarArquivoCompleto() {
    List<Turma> turmas = turmaRepository.buscarTodas();
    Turma novaTurma = new Turma("D02", "P01", 30);
    turmas.add(novaTurma);

    turmaRepository.atualizarArquivoCompleto(turmas);

    List<Turma> turmasAtualizadas = turmaRepository.buscarTodas();
    boolean encontrada = false;
    for (Turma t : turmasAtualizadas) {
      if (t.getCodigoDisciplina().equalsIgnoreCase("D02")
          && t.getPeriodo().equalsIgnoreCase("P01")) {
        encontrada = true;
        break;
      }
    }
    assertTrue(encontrada);
  }
}
