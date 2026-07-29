package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import org.junit.Before;
import org.junit.Test;

public class PeriodoServiceTest {

  private PeriodoService periodoService;

  @Before
  public void setUp() {

    File file = new File("data/periodos.txt");
    if (file.exists()) {
      file.delete();
    }
    periodoService = new PeriodoService();
  }

  @Test
  public void testCadastrarPeriodoPersistidoComSucesso() throws ValidacaoException {
    periodoService.cadastrarPeriodo("2026.2");
    assertEquals(1, periodoService.listarPeriodos().size());
    assertEquals("PLANEJADO", periodoService.listarPeriodos().get(0).getStatus());
  }

  @Test(expected = ValidacaoException.class)
  public void testCadastrarPeriodoDuplicadoNoArquivo() throws ValidacaoException {
    periodoService.cadastrarPeriodo("2026.2");
    periodoService.cadastrarPeriodo("2026.2");
  }

  @Test
  public void testAtivarPeriodoPersistido() throws ValidacaoException {
    periodoService.cadastrarPeriodo("2026.2");
    periodoService.activarPeriodo("2026.2");
    assertEquals("INICIADO", periodoService.listarPeriodos().get(0).getStatus());
  }

  @Test(expected = ValidacaoException.class)
  public void testBloqueioAtivarMultiplosPeriodosSimultaneos() throws ValidacaoException {

    periodoService.cadastrarPeriodo("2026.1");
    periodoService.activarPeriodo("2026.1");

    periodoService.cadastrarPeriodo("2026.2");

    periodoService.activarPeriodo("2026.2");
  }

  @Test
  public void testEncerrarPeriodoComSucesso() throws ValidacaoException {
    periodoService.cadastrarPeriodo("2026.1");
    periodoService.activarPeriodo("2026.1");

    periodoService.encerrarPeriodo("2026.1");
    assertEquals("ENCERRADO", periodoService.listarPeriodos().get(0).getStatus());
  }

  @Test(expected = ValidacaoException.class)
  public void testBloqueioEncerrarPeriodoNaoIniciado() throws ValidacaoException {
    periodoService.cadastrarPeriodo("2026.1");

    periodoService.encerrarPeriodo("2026.1");
  }

  @Test(expected = ValidacaoException.class)
  public void testBloqueioAtivarPeriodoJaIniciado() throws ValidacaoException {
    periodoService.cadastrarPeriodo("2026.1");
    periodoService.activarPeriodo("2026.1");

    periodoService.activarPeriodo("2026.1");
  }

  @Test(expected = ValidacaoException.class)
  public void testAtivarPeriodoInexistente() throws ValidacaoException {

    periodoService.activarPeriodo("9999.9");
  }

  @Test(expected = ValidacaoException.class)
  public void testEncerrarPeriodoInexistente() throws ValidacaoException {

    periodoService.encerrarPeriodo("9999.9");
  }

  @Test(expected = ValidacaoException.class)
  public void testCadastrarCodigoNulo() throws ValidacaoException {
    periodoService.cadastrarPeriodo("");
  }

  @Test(expected = ValidacaoException.class)
  public void testAtivarCodigoNulo() throws ValidacaoException {
    periodoService.activarPeriodo("");
  }

  @Test(expected = ValidacaoException.class)
  public void testEncerrarCodigoNulo() throws ValidacaoException {
    periodoService.encerrarPeriodo("");
  }

  @Test(expected = ValidacaoException.class)
  public void testEncerrarPeriodoJaEncerrado() throws ValidacaoException {
    periodoService.cadastrarPeriodo("2026.8");
    periodoService.activarPeriodo("2026.8");
    periodoService.encerrarPeriodo("2026.8");
    periodoService.encerrarPeriodo("2026.8");
  }
}
