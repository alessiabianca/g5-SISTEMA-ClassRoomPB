package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Frequencia;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class FrequenciaServiceTest {

  private FrequenciaService frequenciaService;
  private TurmaRepository turmaRepository;
  private MatriculaRepository matriculaRepository;
  private FrequenciaRepository frequenciaRepository;
  private NotaRepository notaRepository;

  private static final String FILE_TURMAS = "data/turmas.txt";
  private static final String FILE_MATRICULAS = "data/matriculas.txt";
  private static final String FILE_FREQUENCIAS = "data/frequencias.txt";

  @Before
  public void setUp() throws Exception {

    File fTurmas = new File(FILE_TURMAS);
    if (fTurmas.exists()) fTurmas.delete();

    File fMatriculas = new File(FILE_MATRICULAS);
    if (fMatriculas.exists()) fMatriculas.delete();

    File fFrequencias = new File(FILE_FREQUENCIAS);
    if (fFrequencias.exists()) fFrequencias.delete();

    turmaRepository = new TurmaRepository();
    matriculaRepository = new MatriculaRepository();
    frequenciaRepository = new FrequenciaRepository();
    notaRepository = new NotaRepository();

    frequenciaService =
        new FrequenciaService(
            turmaRepository, matriculaRepository, frequenciaRepository, notaRepository);
  }

  /** FLUXO PRINCIPAL: Valida o lançamento bem-sucedido de uma chamada pelo professor legítimo. */
  @Test
  public void deveRegistrarChamadaEmLoteComSucessoQuandoProfessorForDonoDaTurma() throws Exception {
    String profDono = "PROF_42";
    String disciplina = "ES01";
    String periodo = "2026.1";
    String dataAula = "27/06/2026";

    turmaRepository.salvar(new Turma(disciplina, periodo, 40));

    List<Matricula> alunosAvaliados = new ArrayList<>();

    Matricula m1 =
        new Matricula("2026101", disciplina, periodo, Matricula.StatusMatricula.CONFIRMADA);
    Matricula m2 =
        new Matricula("2026102", disciplina, periodo, Matricula.StatusMatricula.SOLICITADA);

    alunosAvaliados.add(m1);
    alunosAvaliados.add(m2);

    frequenciaService.registrarChamadaLote(
        profDono, disciplina, periodo, dataAula, alunosAvaliados);

    List<Frequencia> gravadas = frequenciaRepository.buscarTodas();
    assertEquals(2, gravadas.size());

    assertEquals(Frequencia.TipoFrequencia.PRESENCA, gravadas.get(0).getStatus());
    assertEquals("2026101", gravadas.get(0).getMatriculaAluno());
  }

  /**
   * BARREIRA DE SEGURANÇA: Impede que um professor tente efetuar ou alterar chamadas em turmas de
   * terceiros.
   */
  @Test
  public void deveLancarExcecaoEAbortarQuandoProfessorNaoForResponsavelPelaTurma()
      throws Exception {
    String profInvasor = "PROF_99";
    String disciplina = "ES01";
    String periodo = "2026.1";

    // Turma sem oferta no período aciona a exceção de validação
    List<Matricula> alunos = new ArrayList<>();
    alunos.add(new Matricula("2026101", disciplina, periodo, Matricula.StatusMatricula.CONFIRMADA));

    assertThrows(
        ValidacaoException.class,
        () -> {
          frequenciaService.registrarChamadaLote(
              profInvasor, disciplina, periodo, "27/06/2026", alunos);
        });

    assertTrue(frequenciaRepository.buscarTodas().isEmpty());
  }

  /** FLUXO ALTERNATIVO: Valida que o pipeline rejeita operações enviadas sem dados. */
  @Test
  public void deveLancarExcecaoSeAListaDeAlunosForEnviadaVazia() throws Exception {
    String prof = "PROF_42";
    String disciplina = "ES01";
    String periodo = "2026.1";

    turmaRepository.salvar(new Turma(disciplina, periodo, 40));

    assertThrows(
        ValidacaoException.class,
        () -> {
          frequenciaService.registrarChamadaLote(
              prof, disciplina, periodo, "27/06/2026", new ArrayList<>());
        });
  }
}