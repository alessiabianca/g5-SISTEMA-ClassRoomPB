package br.edu.uepb.classroompb.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import br.edu.uepb.classroompb.model.Aluno;
import br.edu.uepb.classroompb.model.Historico;
import br.edu.uepb.classroompb.model.StatusAcademico;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.repository.HistoricoRepository;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HistoricoConsultaTest {
  private HistoricoRepository historicoRepository;
  private HistoricoService historicoService;

  @Before
  public void setUp() {
    new File("data/historico.txt").delete();
    historicoRepository = new HistoricoRepository();
    historicoService = new HistoricoService(historicoRepository, null, null, null, null);
  }

  @After
  public void tearDown() {
    new File("data/historico.txt").delete();
  }

  @Test
  public void deveRecuperarHistoricoCompletoEmOrdemCronologica() {
    historicoRepository.salvarLote(
        List.of(
            new Historico(
                "ALUNO01", "2026.2", "ES03", "PROF03", 8.0, 100.0, StatusAcademico.APROVADO),
            new Historico(
                "OUTRO_ALUNO", "2025.1", "ES01", "PROF01", 9.0, 100.0, StatusAcademico.APROVADO),
            new Historico(
                "ALUNO01", "2026.1", "ES02", "PROF02", 7.0, 80.0, StatusAcademico.APROVADO),
            new Historico(
                "ALUNO01", "2025.2", "ES01", "PROF01", 6.0, 75.0, StatusAcademico.RECUPERACAO)));

    List<Historico> historico = historicoService.consultarHistorico("ALUNO01");

    assertEquals(3, historico.size());
    assertEquals("2025.2", historico.get(0).getPeriodo());
    assertEquals("2026.1", historico.get(1).getPeriodo());
    assertEquals("2026.2", historico.get(2).getPeriodo());
  }

  @Test
  public void deveFormatarCadaRegistroDoHistoricoCompleto() {
    List<Historico> historico =
        List.of(
            new Historico(
                "ALUNO01", "2026.1", "ES01", "PROF01", 8.5, 87.5, StatusAcademico.APROVADO));

    String painel = historicoService.formatarHistorico(historico);

    assertTrue(painel.contains("2026.1"));
    assertTrue(painel.contains("ES01"));
    assertTrue(painel.contains("PROF01"));
    assertTrue(painel.contains("8.5"));
    assertTrue(painel.contains("87.5%"));
    assertTrue(painel.contains("APROVADO"));
    assertFalse(painel.isBlank());
  }

  @Test
  public void deveConsultarHistoricoDeAlunoExistenteSemExporOutrosAlunos()
      throws ValidacaoException {
    historicoRepository.salvarLote(
        List.of(
            new Historico(
                "ALUNO01", "2026.1", "ES01", "PROF01", 8.0, 90.0, StatusAcademico.APROVADO),
            new Historico(
                "ALUNO02", "2026.1", "ES02", "PROF02", 7.0, 80.0, StatusAcademico.APROVADO)));

    List<Historico> historico =
        historicoService.consultarHistoricoAluno("ALUNO01", repositorioComAluno("ALUNO01"));

    assertEquals(1, historico.size());
    assertEquals("ALUNO01", historico.get(0).getMatriculaAluno());
  }

  @Test
  public void deveRecusarConsultaQuandoMatriculaNaoCorresponderAUmAluno() {
    try {
      historicoService.consultarHistoricoAluno("PROF01", repositorioComProfessor("PROF01"));
      fail("A consulta deveria rejeitar uma matricula que nao pertence a um aluno.");
    } catch (ValidacaoException e) {
      assertEquals("Aluno nao encontrado para a matricula informada.", e.getMessage());
    }
  }

  @Test
  public void deveRecusarConsultaDeAlunoVinculadoAOutroCurso() {
    try {
      historicoService.consultarHistoricoAluno(
          "ALUNO01", repositorioComAluno("ALUNO01", "CC"), "SI");
      fail("A consulta deveria rejeitar alunos vinculados a outro curso.");
    } catch (ValidacaoException e) {
      assertEquals("Acesso negado: aluno nao vinculado ao curso do coordenador.", e.getMessage());
    }
  }

  private UsuarioRepository repositorioComAluno(String matricula) {
    return repositorioComAluno(matricula, null);
  }

  private UsuarioRepository repositorioComAluno(String matricula, String codigoCurso) {
    return new UsuarioRepository() {
      @Override
      public Usuario buscarPorMatricula(String identificador) {
        return matricula.equalsIgnoreCase(identificador)
            ? new Aluno(matricula, "Aluno Teste", "aluno@test.com", "123", codigoCurso)
            : null;
      }
    };
  }

  private UsuarioRepository repositorioComProfessor(String matricula) {
    return new UsuarioRepository() {
      @Override
      public Usuario buscarPorMatricula(String identificador) {
        return matricula.equalsIgnoreCase(identificador)
            ? new br.edu.uepb.classroompb.model.Professor(
                matricula, "Professor Teste", "prof@test.com", "123")
            : null;
      }
    };
  }
}
