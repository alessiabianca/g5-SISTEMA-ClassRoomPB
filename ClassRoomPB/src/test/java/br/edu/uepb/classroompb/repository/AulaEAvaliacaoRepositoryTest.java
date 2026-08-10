package br.edu.uepb.classroompb.repository;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Aula;
import br.edu.uepb.classroompb.model.Avaliacao;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class AulaEAvaliacaoRepositoryTest {

  private AulaRepository aulaRepository;
  private AvaliacaoRepository avaliacaoRepository;

  @Before
  public void setUp() {
    new File("data/aulas.txt").delete();
    new File("data/avaliacoes.txt").delete();

    aulaRepository = new AulaRepository();
    avaliacaoRepository = new AvaliacaoRepository();
  }

  @Test
  public void devePersistirEBuscarAulasDoDiario() {
    Aula aula =
        new Aula(
            "AULA_01", "DIA_D01_20261", "10/08/2026", "Introdução à Engenharia de Software", 2);
    aulaRepository.salvar(aula);

    List<Aula> aulas = aulaRepository.buscarPorDiario("DIA_D01_20261");
    assertEquals(1, aulas.size());
    assertEquals("Introdução à Engenharia de Software", aulas.get(0).getAssunto());
  }

  @Test
  public void devePersistirEBuscarAvaliacoesDoDiario() {
    Avaliacao avaliacao = new Avaliacao("AVA_01", "DIA_D01_20261", "Prova Escrita 1", 1, 1.0, 10.0);
    avaliacaoRepository.salvar(avaliacao);

    List<Avaliacao> avaliacoes = avaliacaoRepository.buscarTodas();
    assertNotNull(avaliacoes);
    assertFalse(avaliacoes.isEmpty());
    assertEquals("Prova Escrita 1", avaliacoes.get(0).getDescricao());
  }
}
