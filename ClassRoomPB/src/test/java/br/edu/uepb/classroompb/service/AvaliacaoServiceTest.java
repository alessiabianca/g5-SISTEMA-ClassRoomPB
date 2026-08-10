package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Avaliacao;
import br.edu.uepb.classroompb.model.Diario;
import br.edu.uepb.classroompb.repository.AvaliacaoRepository;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class AvaliacaoServiceTest {
  private AvaliacaoService avaliacaoService;
  private AvaliacaoRepository avaliacaoRepository;
  private DiarioRepository diarioRepository;

  private static final String FILE_AVALIACOES = "data/avaliacoes.txt";
  private static final String FILE_DIARIOS = "data/diarios.txt";

  @Before
  public void setUp() throws Exception {
    File dataDir = new File("data");
    if (!dataDir.exists()) {
      dataDir.mkdirs();
    }
    new File(FILE_AVALIACOES).delete();
    new File(FILE_DIARIOS).delete();

    avaliacaoRepository = new AvaliacaoRepository();
    diarioRepository = new DiarioRepository();
    avaliacaoService = new AvaliacaoService(avaliacaoRepository, diarioRepository);
  }

  @Test
  public void deveCadastrarAvaliacaoComSucesso() throws Exception {
    diarioRepository.salvar(
        new Diario(
            "D01",
            "P1",
            "2026.1",
            "Desc",
            "PROF1",
            "08:00",
            "Sala 1",
            10,
            Diario.SituacaoDiario.ABERTO));
    Avaliacao av = avaliacaoService.cadastrarAvaliacao("PROF1", "D01", "Prova 1", 1, 5.0, 10.0);
    assertNotNull(av.getId());
    assertEquals("D01", av.getCodigoDiario());
    assertEquals(1, av.getEtapa());

    List<Avaliacao> cadastradas = avaliacaoService.listarAvaliacoes("D01");
    assertEquals(1, cadastradas.size());
  }

  @Test(expected = ValidacaoException.class)
  public void deveBloquearCadastroSemDiario() throws Exception {
    avaliacaoService.cadastrarAvaliacao("PROF1", "INEXISTENTE", "Prova 1", 1, 5.0, 10.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveBloquearCadastroProfessorErrado() throws Exception {
    diarioRepository.salvar(
        new Diario(
            "D01",
            "P1",
            "2026.1",
            "Desc",
            "PROF1",
            "08:00",
            "Sala 1",
            10,
            Diario.SituacaoDiario.ABERTO));
    avaliacaoService.cadastrarAvaliacao("OUTRO", "D01", "Prova 1", 1, 5.0, 10.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveBloquearCadastroDiarioFechado() throws Exception {
    diarioRepository.salvar(
        new Diario(
            "D01",
            "P1",
            "2026.1",
            "Desc",
            "PROF1",
            "08:00",
            "Sala 1",
            10,
            Diario.SituacaoDiario.FECHADO));
    avaliacaoService.cadastrarAvaliacao("PROF1", "D01", "Prova 1", 1, 5.0, 10.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveBloquearEtapaInvalidaMenorQueUm() throws Exception {
    diarioRepository.salvar(
        new Diario(
            "D01",
            "P1",
            "2026.1",
            "Desc",
            "PROF1",
            "08:00",
            "Sala 1",
            10,
            Diario.SituacaoDiario.ABERTO));
    avaliacaoService.cadastrarAvaliacao("PROF1", "D01", "Prova 1", 0, 5.0, 10.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveBloquearEtapaInvalidaMaiorQueTres() throws Exception {
    diarioRepository.salvar(
        new Diario(
            "D01",
            "P1",
            "2026.1",
            "Desc",
            "PROF1",
            "08:00",
            "Sala 1",
            10,
            Diario.SituacaoDiario.ABERTO));
    avaliacaoService.cadastrarAvaliacao("PROF1", "D01", "Prova 1", 4, 5.0, 10.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveBloquearPesoZero() throws Exception {
    diarioRepository.salvar(
        new Diario(
            "D01",
            "P1",
            "2026.1",
            "Desc",
            "PROF1",
            "08:00",
            "Sala 1",
            10,
            Diario.SituacaoDiario.ABERTO));
    avaliacaoService.cadastrarAvaliacao("PROF1", "D01", "Prova 1", 1, 0.0, 10.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveBloquearNotaMaximaZero() throws Exception {
    diarioRepository.salvar(
        new Diario(
            "D01",
            "P1",
            "2026.1",
            "Desc",
            "PROF1",
            "08:00",
            "Sala 1",
            10,
            Diario.SituacaoDiario.ABERTO));
    avaliacaoService.cadastrarAvaliacao("PROF1", "D01", "Prova 1", 1, 5.0, 0.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveBloquearNotaMaximaAcimaDeDez() throws Exception {
    diarioRepository.salvar(
        new Diario(
            "D01",
            "P1",
            "2026.1",
            "Desc",
            "PROF1",
            "08:00",
            "Sala 1",
            10,
            Diario.SituacaoDiario.ABERTO));
    avaliacaoService.cadastrarAvaliacao("PROF1", "D01", "Prova 1", 1, 5.0, 11.0);
  }

  @Test(expected = ValidacaoException.class)
  public void deveBloquearMesmaEtapa() throws Exception {
    diarioRepository.salvar(
        new Diario(
            "D01",
            "P1",
            "2026.1",
            "Desc",
            "PROF1",
            "08:00",
            "Sala 1",
            10,
            Diario.SituacaoDiario.ABERTO));
    avaliacaoService.cadastrarAvaliacao("PROF1", "D01", "Prova 1", 1, 5.0, 10.0);
    avaliacaoService.cadastrarAvaliacao("PROF1", "D01", "Prova 2", 1, 5.0, 10.0);
  }
}
