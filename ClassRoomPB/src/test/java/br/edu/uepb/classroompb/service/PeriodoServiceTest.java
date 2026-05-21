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
        // Apaga o arquivo físico de testes antes de cada execução para garantir isolamento limpo
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
        periodoService.cadastrarPeriodo("2026.2"); // Deve lançar exceção de duplicidade
    }

    @Test
    public void testAtivarPeriodoPersistido() throws ValidacaoException {
        periodoService.cadastrarPeriodo("2026.2");
        periodoService.activarPeriodo("2026.2");
        assertEquals("INICIADO", periodoService.listarPeriodos().get(0).getStatus());
    }
}
