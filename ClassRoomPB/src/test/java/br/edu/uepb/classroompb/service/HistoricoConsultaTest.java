package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Historico;
import br.edu.uepb.classroompb.model.StatusAcademico;
import br.edu.uepb.classroompb.repository.HistoricoRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HistoricoConsultaTest {
    private HistoricoRepository historicoRepository;
    private HistoricoService historicoService;

    @Before
    public void setUp() {
        new File("data/historico.txt").delete();
        historicoRepository = new HistoricoRepository();
        historicoService = new HistoricoService(historicoRepository, null, null, null);
    }

    @After
    public void tearDown() {
        new File("data/historico.txt").delete();
    }

    @Test
    public void deveRecuperarHistoricoCompletoEmOrdemCronologica() {
        historicoRepository.salvarLote(List.of(
                new Historico("ALUNO01", "ES03", "2026.2", 8.0, 100.0, StatusAcademico.APROVADO),
                new Historico("OUTRO_ALUNO", "ES01", "2025.1", 9.0, 100.0, StatusAcademico.APROVADO),
                new Historico("ALUNO01", "ES02", "2026.1", 7.0, 80.0, StatusAcademico.APROVADO),
                new Historico("ALUNO01", "ES01", "2025.2", 6.0, 75.0, StatusAcademico.RECUPERACAO)
        ));

        List<Historico> historico = historicoService.consultarHistorico("ALUNO01");

        assertEquals(3, historico.size());
        assertEquals("2025.2", historico.get(0).getPeriodo());
        assertEquals("2026.1", historico.get(1).getPeriodo());
        assertEquals("2026.2", historico.get(2).getPeriodo());
    }

    @Test
    public void deveFormatarCadaRegistroDoHistoricoCompleto() {
        List<Historico> historico = List.of(new Historico(
                "ALUNO01", "ES01", "2026.1", 8.5, 87.5, StatusAcademico.APROVADO));

        String painel = historicoService.formatarHistorico(historico);

        assertTrue(painel.contains("2026.1"));
        assertTrue(painel.contains("ES01"));
        assertTrue(painel.contains("8.5"));
        assertTrue(painel.contains("87.5%"));
        assertTrue(painel.contains("APROVADO"));
        assertFalse(painel.isBlank());
    }
}
