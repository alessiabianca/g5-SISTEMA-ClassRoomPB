package br.edu.uepb.classroompb.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import br.edu.uepb.classroompb.model.Historico;
import br.edu.uepb.classroompb.model.StatusAcademico;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HistoricoRepositoryTest {
  private static final Path ARQUIVO_HISTORICO = Path.of("data", "historico.txt");

  private HistoricoRepository historicoRepository;

  @Before
  public void setUp() {
    new File(ARQUIVO_HISTORICO.toString()).delete();
    historicoRepository = new HistoricoRepository();
  }

  @After
  public void tearDown() {
    new File(ARQUIVO_HISTORICO.toString()).delete();
  }

  @Test
  public void deveSalvarTodosOsCamposDoHistoricoConsolidado() throws IOException {
    Historico historico =
        new Historico("2026100", "2026.2", "ES01", "PROF01", 8.75, 83.33, StatusAcademico.APROVADO);

    historicoRepository.salvarLote(List.of(historico));

    List<String> linhasSalvas = Files.readAllLines(ARQUIVO_HISTORICO);
    assertEquals(1, linhasSalvas.size());
    assertEquals("2026.2;ES01;PROF01;8.75;83.33;APROVADO;2026100", linhasSalvas.get(0));
  }

  @Test
  public void deveRecuperarDadosConsolidadosSemAlterarValores() {
    Historico recuperacao =
        new Historico(
            "2026101", "2026.2", "ES02", "PROF02", 5.5, 75.0, StatusAcademico.RECUPERACAO);
    Historico reprovadoPorFalta =
        new Historico(
            "2026101", "2026.2", "ES03", "PROF03", 9.0, 60.0, StatusAcademico.REPROVADO_FALTA);
    Historico outroAluno =
        new Historico("2026102", "2026.2", "ES02", "PROF02", 7.0, 100.0, StatusAcademico.APROVADO);

    historicoRepository.salvarLote(List.of(recuperacao, reprovadoPorFalta, outroAluno));

    List<Historico> encontrados = historicoRepository.buscarPorAluno("2026101");

    assertEquals(2, encontrados.size());
    assertEquals("ES02", encontrados.get(0).getCodigoDisciplina());
    assertEquals("2026.2", encontrados.get(0).getPeriodo());
    assertEquals("PROF02", encontrados.get(0).getMatriculaProfessor());
    assertEquals(5.5, encontrados.get(0).getMediaFinal(), 0.01);
    assertEquals(75.0, encontrados.get(0).getPercentualFrequencia(), 0.01);
    assertEquals(StatusAcademico.RECUPERACAO, encontrados.get(0).getStatus());
    assertEquals("ES03", encontrados.get(1).getCodigoDisciplina());
    assertEquals(9.0, encontrados.get(1).getMediaFinal(), 0.01);
    assertEquals(60.0, encontrados.get(1).getPercentualFrequencia(), 0.01);
    assertEquals(StatusAcademico.REPROVADO_FALTA, encontrados.get(1).getStatus());
    assertTrue(historicoRepository.existe("2026101", "ES02", "2026.2"));
    assertFalse(historicoRepository.existe("2026101", "ES02", "2026.1"));
  }
}
