// src/test/java/br/edu/uepb/classroompb/service/PeriodoServiceTest.java
package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import static org.junit.Assert.*;

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

    @Test
    public void testLogicaDeEstadoParaMatriculas() throws ValidacaoException {
        periodoService.cadastrarPeriodo("2026.2");
        
        // Todo periodo cadastrado comeca como PLANEJADO, portanto fechado para matriculas
        assertFalse(periodoService.isPeriodoAberto("2026.2"));
        
        // Apos a ativacao, o status muda para INICIADO, liberando as matriculas
        periodoService.activarPeriodo("2026.2");
        assertTrue(periodoService.isPeriodoAberto("2026.2"));
    }

    @Test(expected = ValidacaoException.class)
    public void testVerificarAberturaDePeriodoInexistente() throws ValidacaoException {
        periodoService.isPeriodoAberto("2030.1");
    }
}