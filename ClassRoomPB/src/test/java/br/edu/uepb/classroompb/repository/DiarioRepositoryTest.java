package br.edu.uepb.classroompb.repository;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Diario;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class DiarioRepositoryTest {

  private DiarioRepository diarioRepository;
  private static final String FILE_DIARIOS = "data/diarios.txt";

  @Before
  public void setUp() {
    File f = new File(FILE_DIARIOS);
    if (f.exists()) {
      f.delete();
    }
    diarioRepository = new DiarioRepository();
  }

  @Test
  public void deveSalvarEBuscarDiarioComSucesso() {
    Diario diario =
        new Diario(
            "DIA_D01_20261",
            "D01",
            "2026.1",
            "Diário de ES",
            "PROF123",
            "SEG 08:00-10:00",
            "Sala 101",
            60);

    diarioRepository.salvar(diario);

    List<Diario> diarios = diarioRepository.buscarTodos();
    assertNotNull(diarios);
    assertFalse(diarios.isEmpty());

    Diario encontrado = diarioRepository.buscarPorCodigo("DIA_D01_20261");
    assertNotNull(encontrado);
    assertEquals("PROF123", encontrado.getMatriculaProfessor());
    assertEquals("Sala 101", encontrado.getSala());
  }

  @Test
  public void deveRetornarNuloQuandoDiarioNaoExistir() {
    Diario encontrado = diarioRepository.buscarPorCodigo("CODIGO_INEXISTENTE");
    assertNull(encontrado);
  }
}
