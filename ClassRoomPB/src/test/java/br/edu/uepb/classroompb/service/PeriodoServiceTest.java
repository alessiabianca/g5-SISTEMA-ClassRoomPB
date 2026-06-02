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

    // --- NOVOS CENÁRIOS ADICIONADOS PARA A TASK 1903 ---

    @Test(expected = ValidacaoException.class)
    public void testBloqueioAtivarMultiplosPeriodosSimultaneos() throws ValidacaoException {
        // Cadastra e ativa o primeiro período
        periodoService.cadastrarPeriodo("2026.1");
        periodoService.activarPeriodo("2026.1");

        // Cadastra o segundo período
        periodoService.cadastrarPeriodo("2026.2");
        
        // Deve lançar ValidacaoException porque o 2026.1 já está INICIADO
        periodoService.activarPeriodo("2026.2"); 
    }

    @Test
    public void testEncerrarPeriodoComSucesso() throws ValidacaoException {
        periodoService.cadastrarPeriodo("2026.1");
        periodoService.activarPeriodo("2026.1");
        
        // Executa o encerramento
        periodoService.encerrarPeriodo("2026.1");
        assertEquals("ENCERRADO", periodoService.listarPeriodos().get(0).getStatus());
    }

    @Test(expected = ValidacaoException.class)
    public void testBloqueioEncerrarPeriodoNaoIniciado() throws ValidacaoException {
        periodoService.cadastrarPeriodo("2026.1"); // Fica como PLANEJADO
        
        // Deve lançar exceção pois não se pode encerrar um período que não foi iniciado
        periodoService.encerrarPeriodo("2026.1");
    }

    @Test(expected = ValidacaoException.class)
    public void testBloqueioAtivarPeriodoJaIniciado() throws ValidacaoException {
        periodoService.cadastrarPeriodo("2026.1");
        periodoService.activarPeriodo("2026.1");
        
        // Deve falhar pois já está ativo
        periodoService.activarPeriodo("2026.1");
    }

    @Test(expected = ValidacaoException.class)
    public void testAtivarPeriodoInexistente() throws ValidacaoException {
        // Tenta ativar um id que nunca foi cadastrado
        periodoService.activarPeriodo("9999.9");
    }

    @Test(expected = ValidacaoException.class)
    public void testEncerrarPeriodoInexistente() throws ValidacaoException {
        // Tenta encerrar um id que nunca foi cadastrado
        periodoService.encerrarPeriodo("9999.9");
    }
}