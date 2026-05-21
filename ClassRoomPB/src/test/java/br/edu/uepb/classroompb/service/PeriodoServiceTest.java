// src/test/java/br/edu/uepb/classroompb/service/PeriodoServiceTest.java
package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PeriodoServiceTest {

    private PeriodoService periodoService;

    @Before
    public void setUp() {
        periodoService = new PeriodoService();
    }

    @Test
    public void testCadastrarPeriodoComoPlanejado() throws ValidacaoException {
        periodoService.cadastrarPeriodo("2026.2");
        assertEquals(1, periodoService.listarPeriodos().size());
        assertEquals("PLANEJADO", periodoService.listarPeriodos().get(0).getStatus());
    }

    @Test(expected = ValidacaoException.class)
    public void testCadastrarPeriodoDuplicado() throws ValidacaoException {
        periodoService.cadastrarPeriodo("2026.2");
        periodoService.cadastrarPeriodo("2026.2");
    }

    @Test
    public void testAtivarPeriodoAlteraStatusParaIniciado() throws ValidacaoException {
        periodoService.cadastrarPeriodo("2026.2");
        periodoService.ativarPeriodo("2026.2");
        assertEquals("INICIADO", periodoService.listarPeriodos().get(0).getStatus());
    }

    @Test
    public void testAtivarNovoPeriodoEncerraOAnterior() throws ValidacaoException {
        periodoService.cadastrarPeriodo("2026.1");
        periodoService.cadastrarPeriodo("2026.2");
        
        periodoService.ativarPeriodo("2026.1");
        periodoService.ativarPeriodo("2026.2"); // Esse deve virar INICIADO e o 2026.1 deve virar ENCERRADO
        
        assertEquals("ENCERRADO", periodoService.listarPeriodos().get(0).getStatus());
        assertEquals("INICIADO", periodoService.listarPeriodos().get(1).getStatus());
    }
}