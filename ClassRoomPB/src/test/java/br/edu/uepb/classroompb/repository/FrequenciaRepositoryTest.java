package br.edu.uepb.classroompb.repository;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Frequencia;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class FrequenciaRepositoryTest {
  private FrequenciaRepository repository;
  private static final String FILE_PATH = "data/frequencias.txt";

  @Before
  public void setUp() {
    File f = new File(FILE_PATH);
    if (f.exists()) f.delete();
    repository = new FrequenciaRepository();
  }

  @Test
  public void testSalvarEBuscarTodas() {
    List<Frequencia> list = new ArrayList<>();
    list.add(
        new Frequencia("27/06/2026", "AL123", "D01", "P01", Frequencia.TipoFrequencia.PRESENCA));
    repository.salvarLote(list);

    List<Frequencia> frequencias = repository.buscarTodas();
    assertEquals(1, frequencias.size());
    Frequencia lida = frequencias.get(0);
    assertEquals("AL123", lida.getMatriculaAluno());
    assertEquals("D01", lida.getCodigoDisciplina());
    assertEquals("P01", lida.getPeriodo());
    assertEquals("27/06/2026", lida.getDataAula());
    assertEquals(Frequencia.TipoFrequencia.PRESENCA, lida.getStatus());
  }
}
